package no.mnemonic.services.triggers.service.converters;

import no.mnemonic.services.triggers.api.model.v1.OrganizationInfo;
import no.mnemonic.services.triggers.api.model.v1.TriggerActionDefinition;
import no.mnemonic.services.triggers.api.model.v1.TriggerRule;
import no.mnemonic.services.triggers.service.dao.AccessMode;
import no.mnemonic.services.triggers.service.dao.TriggerRuleEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TriggerRuleConverterTest {

  private final Function<UUID, OrganizationInfo> organizationResolver = id -> OrganizationInfo.builder().setId(id).build();
  private final Function<String, TriggerActionDefinition> triggerActionResolver = name -> TriggerActionDefinition.builder()
      .setId(UUID.randomUUID())
      .setName(name)
      .build();
  private final TriggerRuleConverter converter = TriggerRuleConverter.builder()
      .setOrganizationResolver(organizationResolver)
      .setTriggerActionResolver(triggerActionResolver)
      .build();

  @Test
  public void testInitializeWithoutOrganizationResolver() {
    assertThrows(RuntimeException.class, () -> TriggerRuleConverter.builder()
        .setTriggerActionResolver(triggerActionResolver)
        .build());
  }

  @Test
  public void testInitializeWithoutTriggerActionResolver() {
    assertThrows(RuntimeException.class, () -> TriggerRuleConverter.builder()
        .setOrganizationResolver(organizationResolver)
        .build());
  }

  @Test
  public void testConverterWithNullEntity() {
    assertNull(converter.apply(null));
  }

  @Test
  public void testConverterWithCompleteEntity() {
    TriggerRuleEntity entity = createEntity();
    assertModel(entity, converter.apply(entity));
  }

  private TriggerRuleEntity createEntity() {
    return TriggerRuleEntity.builder()
        .setId(UUID.randomUUID())
        .setService("service")
        .setExpression("expression")
        .setAccessMode(AccessMode.Public)
        .setTriggerAction("triggerAction")
        .addEvent("event1")
        .addEvent("event2")
        .addEvent("event3")
        .addOrganization(UUID.randomUUID())
        .addOrganization(UUID.randomUUID())
        .addOrganization(UUID.randomUUID())
        .addScope("scope1")
        .addScope("scope2")
        .addScope("scope3")
        .addTriggerParameter("name1", "value1")
        .addTriggerParameter("name2", "value2")
        .addTriggerParameter("name3", "value3")
        .build();
  }

  private void assertModel(TriggerRuleEntity entity, TriggerRule model) {
    assertEquals(entity.getId(), model.getId());
    assertEquals(entity.getService(), model.getService());
    assertEquals(entity.getExpression(), model.getExpression());
    assertEquals(entity.getAccessMode().name(), model.getAccessMode().name());
    assertEquals(entity.getTriggerAction(), model.getTriggerAction().getName());
    assertEquals(entity.getEvents(), model.getEvents());
    assertEquals(entity.getOrganizations(), model.getOrganizations().stream().map(OrganizationInfo::getId).collect(Collectors.toSet()));
    assertEquals(entity.getScopes(), model.getScopes());
    assertEquals(entity.getTriggerParameters(), model.getTriggerParameters());
  }
}
