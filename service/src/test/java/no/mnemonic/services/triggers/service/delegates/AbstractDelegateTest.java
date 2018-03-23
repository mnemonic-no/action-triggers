package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.services.triggers.api.model.v1.TriggerActionDefinition;
import no.mnemonic.services.triggers.api.model.v1.TriggerEventDefinition;
import no.mnemonic.services.triggers.api.model.v1.TriggerRule;
import no.mnemonic.services.triggers.service.dao.AccessMode;
import no.mnemonic.services.triggers.service.dao.TriggerActionDefinitionEntity;
import no.mnemonic.services.triggers.service.dao.TriggerEventDefinitionEntity;
import no.mnemonic.services.triggers.service.dao.TriggerRuleEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

abstract class AbstractDelegateTest {

  Supplier<Collection<TriggerActionDefinitionEntity>> createTriggerActionDefinitionEntitiesSupplier() {
    Collection<TriggerActionDefinitionEntity> entities = new ArrayList<>();
    for (int i = 1; i <= 3; i++) {
      entities.add(TriggerActionDefinitionEntity.builder()
          .setId(UUID.randomUUID())
          .setName("name" + i)
          .setDescription("description for action " + i)
          .setTriggerActionClass("triggerActionClass")
          .setRequiredPermission("requiredPermission")
          .build());
    }

    return () -> entities;
  }

  Supplier<Collection<TriggerEventDefinitionEntity>> createTriggerEventDefinitionEntitiesSupplier() {
    Collection<TriggerEventDefinitionEntity> entities = new ArrayList<>();
    for (int i = 1; i <= 3; i++) {
      entities.add(TriggerEventDefinitionEntity.builder()
          .setId(UUID.randomUUID())
          .setService("service" + i)
          .setName("event" + i)
          .build());
    }

    return () -> entities;
  }

  Supplier<Collection<TriggerRuleEntity>> createTriggerRuleEntitiesSupplier() {
    Collection<TriggerRuleEntity> entities = new ArrayList<>();
    for (int i = 1; i <= 3; i++) {
      entities.add(TriggerRuleEntity.builder()
          .setId(UUID.randomUUID())
          .setService("service" + i)
          .setAccessMode(AccessMode.Public)
          .setExpression("expression")
          .setTriggerAction("triggerAction")
          .addEvent("event" + i)
          .addEvent("additionalEvent")
          .addOrganization(UUID.randomUUID())
          .build());
    }

    return () -> entities;
  }

  Function<TriggerActionDefinitionEntity, TriggerActionDefinition> createTriggerActionDefinitionEntityConverter() {
    return entity -> TriggerActionDefinition.builder()
        .setId(entity.getId())
        .setName(entity.getName())
        .build();
  }

  Function<TriggerEventDefinitionEntity, TriggerEventDefinition> createTriggerEventDefinitionEntityConverter() {
    return entity -> TriggerEventDefinition.builder()
        .setId(entity.getId())
        .setService(entity.getService())
        .setName(entity.getName())
        .build();
  }

  Function<TriggerRuleEntity, TriggerRule> createTriggerRuleEntityConverter() {
    return entity -> TriggerRule.builder()
        .setId(entity.getId())
        .setService(entity.getService())
        .setEvents(entity.getEvents())
        .build();
  }
}
