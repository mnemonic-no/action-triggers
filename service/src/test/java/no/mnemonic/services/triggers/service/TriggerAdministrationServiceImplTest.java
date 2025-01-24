package no.mnemonic.services.triggers.service;

import no.mnemonic.commons.utilities.collections.ListUtils;
import no.mnemonic.services.triggers.api.model.v1.TriggerActionDefinition;
import no.mnemonic.services.triggers.api.model.v1.TriggerEventDefinition;
import no.mnemonic.services.triggers.api.model.v1.TriggerRule;
import no.mnemonic.services.triggers.api.request.v1.*;
import no.mnemonic.services.triggers.api.service.v1.TriggerAdministrationService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TriggerAdministrationServiceImplTest {

  private final TriggerAdministrationService service = new TriggerAdministrationServiceImpl(ClassLoader.getSystemResource("").getPath());

  @Test
  public void testGetTriggerActionDefinition() throws Exception {
    TriggerActionDefinition result = service.getTriggerActionDefinition(new TriggerActionDefinitionGetByNameRequest()
        .setName("action3"));
    assertNotNull(result);
    assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426655443333"), result.getId());
  }

  @Test
  public void testSearchTriggerActionDefinitions() throws Exception {
    Iterable<TriggerActionDefinition> result = service.searchTriggerActionDefinitions(new TriggerActionDefinitionSearchRequest());
    assertNotNull(result);
    assertEquals(3, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testGetTriggerEventDefinition() throws Exception {
    TriggerEventDefinition result = service.getTriggerEventDefinition(new TriggerEventDefinitionGetByServiceEventRequest()
        .setService("service2")
        .setEvent("event2"));
    assertNotNull(result);
    assertEquals(UUID.fromString("123e4567-e89b-12d3-a456-426655442222"), result.getId());
  }

  @Test
  public void testSearchTriggerEventDefinitions() throws Exception {
    Iterable<TriggerEventDefinition> result = service.searchTriggerEventDefinitions(new TriggerEventDefinitionSearchRequest()
        .addService("service2")
        .addEvent("event2"));
    assertNotNull(result);
    assertEquals(1, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testSearchTriggerRules() throws Exception {
    Iterable<TriggerRule> result = service.searchTriggerRules(new TriggerRuleSearchRequest()
        .addService("service1")
        .addEvent("event1"));
    assertNotNull(result);
    assertEquals(1, ListUtils.list(result.iterator()).size());
  }
}
