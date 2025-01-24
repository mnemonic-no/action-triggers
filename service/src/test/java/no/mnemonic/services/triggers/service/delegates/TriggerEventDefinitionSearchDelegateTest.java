package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.commons.utilities.collections.ListUtils;
import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.model.v1.TriggerEventDefinition;
import no.mnemonic.services.triggers.api.request.v1.TriggerEventDefinitionSearchRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TriggerEventDefinitionSearchDelegateTest extends AbstractDelegateTest {

  private final TriggerEventDefinitionSearchDelegate delegate = TriggerEventDefinitionSearchDelegate.builder()
      .setEntitiesSupplier(createTriggerEventDefinitionEntitiesSupplier())
      .setEntityConverter(createTriggerEventDefinitionEntityConverter())
      .build();

  @Test
  public void testInitializeDelegateWithoutEntitiesSupplier() {
    assertThrows(RuntimeException.class, () -> TriggerEventDefinitionSearchDelegate.builder()
        .setEntityConverter(createTriggerEventDefinitionEntityConverter())
        .build());
  }

  @Test
  public void testInitializeDelegateWithoutEntityConverter() {
    assertThrows(RuntimeException.class, () -> TriggerEventDefinitionSearchDelegate.builder()
        .setEntitiesSupplier(createTriggerEventDefinitionEntitiesSupplier())
        .build());
  }

  @Test
  public void testHandleWithoutRequest() {
    assertThrows(InvalidArgumentException.class, () -> delegate.handle(null));
  }

  @Test
  public void testHandleWithoutFiltering() throws Exception {
    Iterable<TriggerEventDefinition> result = delegate.handle(new TriggerEventDefinitionSearchRequest());
    assertNotNull(result);
    assertEquals(3, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testHandleWithFilteringOnService() throws Exception {
    Iterable<TriggerEventDefinition> result = delegate.handle(new TriggerEventDefinitionSearchRequest().addService("service2"));
    assertNotNull(result);
    assertEquals(1, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testHandleWithFilteringOnMultipleServices() throws Exception {
    Iterable<TriggerEventDefinition> result = delegate.handle(new TriggerEventDefinitionSearchRequest()
        .addService("service1")
        .addService("service3")
    );
    assertNotNull(result);
    assertEquals(2, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testHandleWithFilteringOnEvent() throws Exception {
    Iterable<TriggerEventDefinition> result = delegate.handle(new TriggerEventDefinitionSearchRequest().addEvent("event2"));
    assertNotNull(result);
    assertEquals(1, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testHandleWithFilteringOnMultipleEvents() throws Exception {
    Iterable<TriggerEventDefinition> result = delegate.handle(new TriggerEventDefinitionSearchRequest()
        .addEvent("event1")
        .addEvent("event3")
    );
    assertNotNull(result);
    assertEquals(2, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testHandleWithFilteringOnBothServiceAndEvent() throws Exception {
    Iterable<TriggerEventDefinition> result = delegate.handle(new TriggerEventDefinitionSearchRequest()
        .addService("service1")
        .addEvent("event1")
    );
    assertNotNull(result);
    assertEquals(1, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testHandleWithFilteringOnBothServiceAndEventNoMatch() throws Exception {
    Iterable<TriggerEventDefinition> result = delegate.handle(new TriggerEventDefinitionSearchRequest()
        .addService("service1")
        .addEvent("event2")
    );
    assertNotNull(result);
    assertEquals(0, ListUtils.list(result.iterator()).size());
  }
}
