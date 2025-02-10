package no.mnemonic.services.triggers.service;

import no.mnemonic.commons.logging.Logger;
import no.mnemonic.commons.logging.Logging;
import no.mnemonic.commons.utilities.StringUtils;
import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.exceptions.ObjectNotFoundException;
import no.mnemonic.services.triggers.api.model.v1.*;
import no.mnemonic.services.triggers.api.request.v1.*;
import no.mnemonic.services.triggers.api.service.v1.TriggerAdministrationService;
import no.mnemonic.services.triggers.service.converters.TriggerActionDefinitionConverter;
import no.mnemonic.services.triggers.service.converters.TriggerEventDefinitionConverter;
import no.mnemonic.services.triggers.service.converters.TriggerRuleConverter;
import no.mnemonic.services.triggers.service.dao.TriggerActionDefinitionEntity;
import no.mnemonic.services.triggers.service.dao.TriggerEventDefinitionEntity;
import no.mnemonic.services.triggers.service.dao.TriggerRuleEntity;
import no.mnemonic.services.triggers.service.dao.YamlReader;
import no.mnemonic.services.triggers.service.delegates.*;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.function.Function;

public class TriggerAdministrationServiceImpl implements TriggerAdministrationService {

  private static final Logger LOGGER = Logging.getLogger(TriggerAdministrationServiceImpl.class);

  private static final String TRIGGER_ACTION_DEFINITION_FILE = "triggerActionDefinition.yaml";
  private static final String TRIGGER_EVENT_DEFINITION_FILE = "triggerEventDefinition.yaml";
  private static final String TRIGGER_RULE_FILE = "triggerRule.yaml";

  private final YamlReader<TriggerActionDefinitionEntity> triggerActionDefinitionReader;
  private final YamlReader<TriggerEventDefinitionEntity> triggerEventDefinitionReader;
  private final YamlReader<TriggerRuleEntity> triggerRuleReader;
  private final TriggerActionDefinitionConverter triggerActionDefinitionConverter;
  private final TriggerEventDefinitionConverter triggerEventDefinitionConverter;
  private final TriggerRuleConverter triggerRuleConverter;

  @Inject
  public TriggerAdministrationServiceImpl(@Named("trigger.administration.service.configuration.directory") String configDir) {
    triggerActionDefinitionReader = new YamlReader<>(Paths.get(configDir, TRIGGER_ACTION_DEFINITION_FILE), TriggerActionDefinitionEntity.class);
    triggerEventDefinitionReader = new YamlReader<>(Paths.get(configDir, TRIGGER_EVENT_DEFINITION_FILE), TriggerEventDefinitionEntity.class);
    triggerRuleReader = new YamlReader<>(Paths.get(configDir, TRIGGER_RULE_FILE), TriggerRuleEntity.class);
    triggerActionDefinitionConverter = TriggerActionDefinitionConverter.builder()
        .setFunctionResolver(createFunctionResolver())
        .build();
    triggerEventDefinitionConverter = TriggerEventDefinitionConverter.builder()
        .setFunctionResolver(createFunctionResolver())
        .build();
    triggerRuleConverter = TriggerRuleConverter.builder()
        .setOrganizationResolver(createOrganizationResolver())
        .setTriggerActionResolver(createTriggerActionResolver())
        .build();
  }

  @Override
  public TriggerActionDefinition getTriggerActionDefinition(TriggerActionDefinitionGetByNameRequest request)
      throws InvalidArgumentException, ObjectNotFoundException {
    return TriggerActionDefinitionGetByNameDelegate.builder()
        .setEntitiesSupplier(triggerActionDefinitionReader::readAll)
        .setEntityConverter(triggerActionDefinitionConverter)
        .build()
        .handle(request);
  }

  @Override
  public Iterable<TriggerActionDefinition> searchTriggerActionDefinitions(TriggerActionDefinitionSearchRequest request)
      throws InvalidArgumentException {
    return TriggerActionDefinitionSearchDelegate.builder()
        .setEntitiesSupplier(triggerActionDefinitionReader::readAll)
        .setEntityConverter(triggerActionDefinitionConverter)
        .build()
        .handle(request);
  }

  @Override
  public TriggerEventDefinition getTriggerEventDefinition(TriggerEventDefinitionGetByServiceEventRequest request)
      throws InvalidArgumentException, ObjectNotFoundException {
    return TriggerEventDefinitionGetByServiceEventDelegate.builder()
        .setEntitiesSupplier(triggerEventDefinitionReader::readAll)
        .setEntityConverter(triggerEventDefinitionConverter)
        .build()
        .handle(request);
  }

  @Override
  public Iterable<TriggerEventDefinition> searchTriggerEventDefinitions(TriggerEventDefinitionSearchRequest request)
      throws InvalidArgumentException {
    return TriggerEventDefinitionSearchDelegate.builder()
        .setEntitiesSupplier(triggerEventDefinitionReader::readAll)
        .setEntityConverter(triggerEventDefinitionConverter)
        .build()
        .handle(request);
  }

  @Override
  public Iterable<TriggerRule> searchTriggerRules(TriggerRuleSearchRequest request)
      throws InvalidArgumentException {
    return TriggerRuleSearchDelegate.builder()
        .setEntitiesSupplier(triggerRuleReader::readAll)
        .setEntityConverter(triggerRuleConverter)
        .build()
        .handle(request);
  }

  private Function<String, FunctionInfo> createFunctionResolver() {
    return name -> {
      if (StringUtils.isBlank(name)) return null;
      // Directly derive id from name until functions are resolved from an access controller.
      // A function's name is supposed to be unique, thus, a derived id will also be unique.
      return FunctionInfo.builder()
          .setId(UUID.nameUUIDFromBytes(name.getBytes()))
          .setName(name)
          .build();
    };
  }

  private Function<UUID, OrganizationInfo> createOrganizationResolver() {
    return id -> {
      if (id == null) return null;
      // Set name and short name to id until organizations are resolved from an access controller.
      return OrganizationInfo.builder()
          .setId(id)
          .setShortName(id.toString())
          .setName(id.toString())
          .build();
    };
  }

  private Function<String, TriggerActionDefinition> createTriggerActionResolver() {
    return name -> {
      try {
        return getTriggerActionDefinition(new TriggerActionDefinitionGetByNameRequest().setName(name));
      } catch (InvalidArgumentException | ObjectNotFoundException ex) {
        LOGGER.warning(ex, "Could not resolve TriggerActionDefinition for name = %s.", name);
        return null;
      }
    };
  }
}
