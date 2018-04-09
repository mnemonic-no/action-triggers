package no.mnemonic.services.triggers.pipeline.worker;

import no.mnemonic.commons.logging.Logger;
import no.mnemonic.commons.logging.Logging;
import no.mnemonic.commons.metrics.MetricAspect;
import no.mnemonic.commons.metrics.MetricException;
import no.mnemonic.commons.metrics.Metrics;
import no.mnemonic.commons.metrics.MetricsData;
import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.StringUtils;
import no.mnemonic.commons.utilities.collections.MapUtils;
import no.mnemonic.commons.utilities.collections.SetUtils;
import no.mnemonic.services.triggers.action.TriggerAction;
import no.mnemonic.services.triggers.action.exceptions.ParameterException;
import no.mnemonic.services.triggers.action.exceptions.TriggerExecutionException;
import no.mnemonic.services.triggers.action.exceptions.TriggerInitializationException;
import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.exceptions.ObjectNotFoundException;
import no.mnemonic.services.triggers.api.model.v1.*;
import no.mnemonic.services.triggers.api.request.v1.TriggerActionDefinitionGetByNameRequest;
import no.mnemonic.services.triggers.api.request.v1.TriggerEventDefinitionGetByServiceEventRequest;
import no.mnemonic.services.triggers.api.request.v1.TriggerRuleSearchRequest;
import no.mnemonic.services.triggers.api.service.v1.TriggerAdministrationService;
import no.mnemonic.services.triggers.pipeline.api.TriggerEvent;
import org.apache.commons.jexl3.*;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static no.mnemonic.commons.utilities.collections.MapUtils.Pair.T;

/**
 * Engine checking TriggerEvents against TriggerRules and executing TriggerActions for matching TriggerRules.
 */
class RuleEvaluationEngine implements MetricAspect {

  private static final Logger LOGGER = Logging.getLogger(RuleEvaluationEngine.class);
  private static final Map<String, Integer> ACCESS_MODE_ORDER = MapUtils.map(
      T("Public", 0),
      T("RoleBased", 1),
      T("Private", 2)
  );

  private final AtomicLong matchingTriggerRulesCounter = new AtomicLong();
  private final AtomicLong successfulActionInvocationsCounter = new AtomicLong();
  private final AtomicLong failedActionInvocationsCounter = new AtomicLong();
  private final AtomicLong administrationServiceErrorCounter = new AtomicLong();
  private final AtomicLong expressionEvaluationErrorCounter = new AtomicLong();

  private final TriggerAdministrationService service;
  private final JexlEngine expressionEngine;
  private final JxltEngine templateEngine;

  RuleEvaluationEngine(TriggerAdministrationService service) {
    this.service = ObjectUtils.notNull(service, "'service' is required!");
    expressionEngine = new JexlBuilder()
        .silent(false)
        .strict(true)
        .create();
    templateEngine = expressionEngine.createJxltEngine();
  }

  @Override
  public Metrics getMetrics() throws MetricException {
    return new MetricsData()
        .addData("matchingTriggerRules", matchingTriggerRulesCounter.get())
        .addData("successfulActionInvocations", successfulActionInvocationsCounter.get())
        .addData("failedActionInvocations", failedActionInvocationsCounter.get())
        .addData("administrationServiceError", administrationServiceErrorCounter.get())
        .addData("expressionEvaluationError", expressionEvaluationErrorCounter.get());
  }

  /**
   * Evaluate a TriggerEvent against all TriggerRules and execute TriggerActions for matching TriggerRules.
   * <p>
   * The caller must make sure that all required fields are set in the TriggerEvent before calling this method.
   *
   * @param event TriggerEvent to evaluate
   */
  void evaluate(TriggerEvent event) {
    if (event == null) return;

    // For now only verify that the corresponding TriggerEventDefinition exists.
    // This should later also verify that the required context parameters are set.
    if (fetchTriggerEventDefinition(event) == null) return;

    for (TriggerRule rule : fetchTriggerRules(event)) {
      // The event's organization must be part of the rule's organizations.
      if (!SetUtils.set(rule.getOrganizations(), OrganizationInfo::getId).contains(event.getOrganization())) continue;
      // The access mode of the event must be covered by the access mode of the rule.
      // 1. Rule access mode of 'Public' requires event access mode 'Public'.
      // 2. Rule access mode of 'RoleBased' requires event access mode 'Public' or 'RoleBased'.
      // 3. Rule access mode of 'Private' requires event access mode 'Public', 'RoleBased' or 'Private'.
      if (ACCESS_MODE_ORDER.get(rule.getAccessMode().name()) < ACCESS_MODE_ORDER.get(event.getAccessMode().name())) continue;
      // Event scope is optional, but if set it must be part of the rule's scopes.
      if (!StringUtils.isBlank(event.getScope()) && !SetUtils.set(rule.getScopes()).contains(event.getScope())) continue;
      // The rule's expression must evaluate to 'true'.
      if (!evaluateRuleExpression(rule, event)) continue;
      // If all conditions are fulfilled trigger the rule's action.
      matchingTriggerRulesCounter.incrementAndGet();
      triggerAction(rule, event);
    }
  }

  private boolean evaluateRuleExpression(TriggerRule rule, TriggerEvent event) {
    try {
      Object result = expressionEngine.createExpression(rule.getExpression())
          .evaluate(populateExpressionContext(event.getContextParameters()));
      if (result instanceof Boolean) {
        return Boolean.class.cast(result);
      } else {
        LOGGER.info("Expression for TriggerRule with id = %s did not return a boolean value.", rule.getId());
        return false;
      }
    } catch (JexlException ex) {
      LOGGER.info(ex, "Could not evaluate expression for TriggerRule with id = %s.", rule.getId());
      expressionEvaluationErrorCounter.incrementAndGet();
      return false;
    }
  }

  private void triggerAction(TriggerRule rule, TriggerEvent event) {
    TriggerActionDefinition definition = fetchTriggerActionDefinition(rule.getTriggerAction().getName());
    if (definition == null) return;

    try (TriggerAction action = loadTriggerAction(definition.getTriggerActionClass())) {
      if (action == null) return;
      action.init(definition.getInitParameters());
      action.trigger(evaluateTriggerParameters(definition, rule, event));
      successfulActionInvocationsCounter.incrementAndGet();

      if (LOGGER.isDebug()) {
        LOGGER.debug("Successfully executed action [TriggerActionDefinition: %s, TriggerRule: %s, TriggerEvent: %s].",
            definition.getId(), rule.getId(), event.getId());
      }
    } catch (ParameterException ex) {
      LOGGER.info(ex, "Could not initialize/execute action due to missing or invalid parameter " +
              "[TriggerActionDefinition: %s, TriggerRule: %s, TriggerEvent: %s, Parameter: %s].",
          definition.getId(), rule.getId(), event.getId(), ex.getParameter());
      failedActionInvocationsCounter.incrementAndGet();
    } catch (TriggerInitializationException | TriggerExecutionException ex) {
      LOGGER.info(ex, "Failed to initialize/execute action [TriggerActionDefinition: %s, TriggerRule: %s, TriggerEvent: %s].",
          definition.getId(), rule.getId(), event.getId());
      failedActionInvocationsCounter.incrementAndGet();
    }
  }

  private TriggerAction loadTriggerAction(String triggerAction) {
    try {
      Class<?> triggerActionClass = getClass().getClassLoader().loadClass(triggerAction);
      if (!TriggerAction.class.isAssignableFrom(triggerActionClass)) {
        LOGGER.warning("Could not instantiate TriggerAction. Class '%s' does not implement TriggerAction interface.", triggerAction);
        failedActionInvocationsCounter.incrementAndGet();
        return null;
      }
      return TriggerAction.class.cast(triggerActionClass.newInstance());
    } catch (Exception ex) {
      LOGGER.warning("Could not instantiate TriggerAction from class '%s'.", triggerAction);
      failedActionInvocationsCounter.incrementAndGet();
      return null;
    }
  }

  private Map<String, String> evaluateTriggerParameters(TriggerActionDefinition action, TriggerRule rule, TriggerEvent event) {
    Map<String, String> evaluated = MapUtils.map();

    // Add all trigger parameters defined in the action with their default values.
    for (Map.Entry<String, ParameterDefinition> parameter : MapUtils.map(action.getTriggerParameters()).entrySet()) {
      evaluated.put(parameter.getKey(), parameter.getValue().getDefaultValue());
    }

    // Evaluate and add all trigger parameters defined in the rule (potentially overwriting the default value).
    for (Map.Entry<String, String> parameter : MapUtils.map(rule.getTriggerParameters()).entrySet()) {
      try {
        StringWriter result = new StringWriter();
        templateEngine.createTemplate(parameter.getValue())
            .evaluate(populateExpressionContext(event.getContextParameters()), result);
        evaluated.put(parameter.getKey(), result.toString());
      } catch (JexlException ex) {
        LOGGER.info(ex, "Could not evaluate expression for trigger parameter '%s' [TriggerRule: %s, TriggerEvent: %s].",
            parameter, rule.getId(), event.getId());
        expressionEvaluationErrorCounter.incrementAndGet();
      }
    }

    return evaluated;
  }

  private TriggerEventDefinition fetchTriggerEventDefinition(TriggerEvent event) {
    try {
      return service.getTriggerEventDefinition(new TriggerEventDefinitionGetByServiceEventRequest()
          .setService(event.getService())
          .setEvent(event.getEvent())
      );
    } catch (InvalidArgumentException | ObjectNotFoundException ex) {
      LOGGER.warning(ex, "Could not fetch TriggerEventDefinition for service '%s' and event '%s'.", event.getService(), event.getEvent());
      administrationServiceErrorCounter.incrementAndGet();
      return null;
    }
  }

  private Iterable<TriggerRule> fetchTriggerRules(TriggerEvent event) {
    try {
      return service.searchTriggerRules(new TriggerRuleSearchRequest()
          .addService(event.getService())
          .addEvent(event.getEvent())
      );
    } catch (InvalidArgumentException ex) {
      LOGGER.warning(ex, "Could not fetch TriggerRules for service '%s' and event '%s'.", event.getService(), event.getEvent());
      administrationServiceErrorCounter.incrementAndGet();
      return Collections.emptyList();
    }
  }

  private TriggerActionDefinition fetchTriggerActionDefinition(String name) {
    try {
      return service.getTriggerActionDefinition(new TriggerActionDefinitionGetByNameRequest().setName(name));
    } catch (InvalidArgumentException | ObjectNotFoundException ex) {
      LOGGER.warning(ex, "Could not fetch TriggerActionDefinition for name '%s'.", name);
      administrationServiceErrorCounter.incrementAndGet();
      return null;
    }
  }

  private JexlContext populateExpressionContext(Map<String, ?> contextParameters) {
    if (MapUtils.isEmpty(contextParameters)) return new MapContext();

    // Need to manually populate the context in order to satisfy the Java compiler.
    JexlContext context = new MapContext();
    contextParameters.forEach(context::set);
    return context;
  }
}
