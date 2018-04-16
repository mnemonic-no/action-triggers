package no.mnemonic.services.triggers.pipeline.worker;

import no.mnemonic.services.triggers.action.TriggerAction;
import no.mnemonic.services.triggers.action.exceptions.ParameterException;
import no.mnemonic.services.triggers.action.exceptions.TriggerExecutionException;
import no.mnemonic.services.triggers.action.exceptions.TriggerInitializationException;
import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.exceptions.ObjectNotFoundException;
import no.mnemonic.services.triggers.api.model.v1.*;
import no.mnemonic.services.triggers.api.service.v1.TriggerAdministrationService;
import no.mnemonic.services.triggers.pipeline.api.AccessMode;
import no.mnemonic.services.triggers.pipeline.api.TriggerEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class RuleEvaluationEngineTest {

  @Mock
  private static TriggerAction action;
  @Mock
  private TriggerAdministrationService service;

  private RuleEvaluationEngine engine;

  @Before
  public void setUp() {
    initMocks(this);
    engine = new RuleEvaluationEngine(service);
  }

  @Test(expected = RuntimeException.class)
  public void testInitializeEngineWithoutServiceThrowsException() {
    new RuleEvaluationEngine(null);
  }

  @Test
  public void testEvaluateTriggerEventDefinitionNotFound() throws Exception {
    when(service.getTriggerEventDefinition(any())).thenThrow(ObjectNotFoundException.class);

    TriggerEvent event = new TestTriggerEvent();
    engine.evaluate(event);

    assertEquals(0L, engine.getMetrics().getData("matchingTriggerRules"));
    verify(service).getTriggerEventDefinition(argThat(request -> Objects.equals(request.getService(), event.getService())
        && Objects.equals(request.getEvent(), event.getEvent())));
  }

  @Test
  public void testEvaluateTriggerRulesNotFound() throws Exception {
    mockFetchTriggerEventDefinition();
    when(service.searchTriggerRules(any())).thenReturn(Collections.emptyList());

    TriggerEvent event = new TestTriggerEvent();
    engine.evaluate(event);

    assertEquals(0L, engine.getMetrics().getData("matchingTriggerRules"));
    verify(service).searchTriggerRules(argThat(request -> request.getService().contains(event.getService())
        && request.getEvent().contains(event.getEvent())));
  }

  @Test
  public void testEvaluateNonMatchingOrganization() throws Exception {
    mockEvaluatingTriggerRules();

    // Organization UUIDs are generated randomly, so they won't match.
    engine.evaluate(new TestTriggerEvent());
    assertEquals(0L, engine.getMetrics().getData("matchingTriggerRules"));
  }

  @Test
  public void testEvaluateAccessModeNotCovered() throws Exception {
    TriggerRule rule = mockEvaluatingTriggerRules();

    // Event has access mode 'Private' but rule has 'Public'.
    engine.evaluate(new TestTriggerEvent()
        .setOrganization(rule.getOrganizations().iterator().next().getId())
        .setAccessMode(AccessMode.Private)
    );
    assertEquals(0L, engine.getMetrics().getData("matchingTriggerRules"));
  }

  @Test
  public void testEvaluateNonMatchingScope() throws Exception {
    TriggerRule rule = mockEvaluatingTriggerRules();

    // Event has different scope than rule.
    engine.evaluate(new TestTriggerEvent()
        .setOrganization(rule.getOrganizations().iterator().next().getId())
        .setScope("something")
    );
    assertEquals(0L, engine.getMetrics().getData("matchingTriggerRules"));
  }

  @Test
  public void testEvaluateExpressionReturnsFalse() throws Exception {
    TriggerRule rule = mockEvaluatingTriggerRules("1 == 2");

    engine.evaluate(new TestTriggerEvent()
        .setOrganization(rule.getOrganizations().iterator().next().getId())
    );
    assertEquals(0L, engine.getMetrics().getData("matchingTriggerRules"));
    assertEquals(0L, engine.getMetrics().getData("expressionEvaluationError"));
  }

  @Test
  public void testEvaluateExpressionWithContextParameters() throws Exception {
    TriggerRule rule = mockEvaluatingTriggerRules("i == j");

    engine.evaluate(new TestTriggerEvent()
        .setOrganization(rule.getOrganizations().iterator().next().getId())
        .addContextParameter("i", 1)
        .addContextParameter("j", 2)
    );
    assertEquals(0L, engine.getMetrics().getData("matchingTriggerRules"));
    assertEquals(0L, engine.getMetrics().getData("expressionEvaluationError"));
  }

  @Test
  public void testEvaluateExpressionReturnsNonBooleanResult() throws Exception {
    TriggerRule rule = mockEvaluatingTriggerRules("42");

    engine.evaluate(new TestTriggerEvent()
        .setOrganization(rule.getOrganizations().iterator().next().getId())
    );
    assertEquals(0L, engine.getMetrics().getData("matchingTriggerRules"));
    assertEquals(0L, engine.getMetrics().getData("expressionEvaluationError"));
  }

  @Test
  public void testEvaluateExpressionWithNonValidExpression() throws Exception {
    TriggerRule rule = mockEvaluatingTriggerRules("1 ==");

    engine.evaluate(new TestTriggerEvent()
        .setOrganization(rule.getOrganizations().iterator().next().getId())
    );
    assertEquals(0L, engine.getMetrics().getData("matchingTriggerRules"));
    assertEquals(1L, engine.getMetrics().getData("expressionEvaluationError"));
  }

  @Test
  public void testEvaluateTriggerActionDefinitionNotFound() throws Exception {
    TriggerRule rule = mockEvaluatingTriggerRules();
    when(service.getTriggerActionDefinition(any())).thenThrow(ObjectNotFoundException.class);

    engine.evaluate(new TestTriggerEvent()
        .setOrganization(rule.getOrganizations().iterator().next().getId())
    );
    assertEquals(1L, engine.getMetrics().getData("matchingTriggerRules"));
    assertEquals(0L, engine.getMetrics().getData("successfulActionInvocations"));
    verify(service).getTriggerActionDefinition(argThat(request -> Objects.equals(request.getName(), rule.getTriggerAction().getName())));
  }

  @Test
  public void testEvaluateTriggerActionFailsOnInit() throws Exception {
    TriggerRule rule = mockEvaluatingTriggerRules();
    doThrow(TriggerInitializationException.class).when(action).init(any());

    engine.evaluate(new TestTriggerEvent()
        .setOrganization(rule.getOrganizations().iterator().next().getId())
    );
    assertEquals(1L, engine.getMetrics().getData("matchingTriggerRules"));
    assertEquals(0L, engine.getMetrics().getData("successfulActionInvocations"));
    verify(action).init(notNull());
  }

  @Test
  public void testEvaluateTriggerActionFailsOnTrigger() throws Exception {
    TriggerRule rule = mockEvaluatingTriggerRules();
    doThrow(TriggerExecutionException.class).when(action).trigger(any());

    engine.evaluate(new TestTriggerEvent()
        .setOrganization(rule.getOrganizations().iterator().next().getId())
    );
    assertEquals(1L, engine.getMetrics().getData("matchingTriggerRules"));
    assertEquals(0L, engine.getMetrics().getData("successfulActionInvocations"));
    verify(action).trigger(notNull());
  }

  @Test
  public void testEvaluateTriggerActionSuccessfully() throws Exception {
    TriggerRule rule = mockEvaluatingTriggerRules();

    engine.evaluate(new TestTriggerEvent()
        .setOrganization(rule.getOrganizations().iterator().next().getId())
    );
    assertEquals(1L, engine.getMetrics().getData("matchingTriggerRules"));
    assertEquals(1L, engine.getMetrics().getData("successfulActionInvocations"));
    verify(action).init(notNull());
    verify(action).trigger(notNull());
  }

  @Test
  public void testEvaluateTriggerActionEvaluatesTriggerParameters() throws Exception {
    TriggerRule rule = mockEvaluatingTriggerRules();

    engine.evaluate(new TestTriggerEvent()
        .setOrganization(rule.getOrganizations().iterator().next().getId())
        .addContextParameter("name", "World")
    );
    verify(action).trigger(argThat(parameters -> {
      assertEquals("defaultValue", parameters.get("defaultParameter"));
      assertEquals("staticValue", parameters.get("staticParameter"));
      assertEquals("Hello World!", parameters.get("expressionParameter"));
      assertEquals("" +
              "The value 1 is under forty-two\n" +
              "The value 3 is under forty-two\n" +
              "The value 5 is under forty-two\n" +
              "Life, the universe, and everything\n" +
              "The value 169 is over forty-two\n",
          parameters.get("templateParameter"));
      return true;
    }));
  }

  private void mockFetchTriggerEventDefinition() throws Exception {
    TriggerEventDefinition definition = TriggerEventDefinition.builder()
        .setId(UUID.randomUUID())
        .setService("TestService")
        .setName("TestEvent")
        .build();
    when(service.getTriggerEventDefinition(any())).thenReturn(definition);
  }

  private void mockFetchTriggerActionDefinition() throws Exception {
    TriggerActionDefinition definition = TriggerActionDefinition.builder()
        .setId(UUID.randomUUID())
        .setName("TestAction")
        .setTriggerActionClass("no.mnemonic.services.triggers.pipeline.worker.RuleEvaluationEngineTest$TestTriggerAction")
        .addInitParameter("initParameter", "initValue")
        .addTriggerParameter("defaultParameter", ParameterDefinition.builder().setDefaultValue("defaultValue").build())
        .build();
    when(service.getTriggerActionDefinition(any())).thenReturn(definition);
  }

  private TriggerRule mockEvaluatingTriggerRules() throws Exception {
    return mockEvaluatingTriggerRules("1 == 1");
  }

  private TriggerRule mockFetchTriggerRules(String expression) throws InvalidArgumentException {
    TriggerRule rule = TriggerRule.builder()
        .setId(UUID.randomUUID())
        .setService("TestService")
        .addEvent("TestEvent")
        .addOrganization(OrganizationInfo.builder().setId(UUID.randomUUID()).build())
        .addScope("TestScope")
        .setAccessMode(no.mnemonic.services.triggers.api.model.v1.AccessMode.Public)
        .setExpression(expression)
        .setTriggerAction(TriggerActionDefinition.builder().setName("TestAction").build().toInfo())
        .addTriggerParameter("staticParameter", "staticValue")
        .addTriggerParameter("expressionParameter", "Hello ${name}!")
        .addTriggerParameter("templateParameter", "" +
            "$$ for(var x : [1, 3, 5, 42, 169]) {\n" +
            "$$   if (x == 42) {\n" +
            "Life, the universe, and everything\n" +
            "$$   } else if (x > 42) {\n" +
            "The value ${x} is over forty-two\n" +
            "$$   } else {\n" +
            "The value ${x} is under forty-two\n" +
            "$$   }\n" +
            "$$ }")
        .build();
    when(service.searchTriggerRules(any())).thenReturn(Collections.singletonList(rule));
    return rule;
  }

  private TriggerRule mockEvaluatingTriggerRules(String expression) throws Exception {
    mockFetchTriggerEventDefinition();
    mockFetchTriggerActionDefinition();
    return mockFetchTriggerRules(expression);
  }

  public static class TestTriggerAction implements TriggerAction {
    @Override
    public void init(Map<String, String> initParameters) throws ParameterException, TriggerInitializationException {
      action.init(initParameters);
    }

    @Override
    public void trigger(Map<String, String> triggerParameters) throws ParameterException, TriggerExecutionException {
      action.trigger(triggerParameters);
    }
  }
}
