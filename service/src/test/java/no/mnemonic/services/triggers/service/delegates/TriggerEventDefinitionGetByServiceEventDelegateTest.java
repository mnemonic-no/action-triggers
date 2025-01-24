package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.exceptions.ObjectNotFoundException;
import no.mnemonic.services.triggers.api.model.v1.TriggerEventDefinition;
import no.mnemonic.services.triggers.api.request.v1.TriggerEventDefinitionGetByServiceEventRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TriggerEventDefinitionGetByServiceEventDelegateTest extends AbstractDelegateTest {

  private final TriggerEventDefinitionGetByServiceEventDelegate delegate = TriggerEventDefinitionGetByServiceEventDelegate.builder()
      .setEntitiesSupplier(createTriggerEventDefinitionEntitiesSupplier())
      .setEntityConverter(createTriggerEventDefinitionEntityConverter())
      .build();

  @Test
  public void testInitializeDelegateWithoutEntitiesSupplier() {
    assertThrows(RuntimeException.class, () -> TriggerEventDefinitionGetByServiceEventDelegate.builder()
        .setEntityConverter(createTriggerEventDefinitionEntityConverter())
        .build());
  }

  @Test
  public void testInitializeDelegateWithoutEntityConverter() {
    assertThrows(RuntimeException.class, () -> TriggerEventDefinitionGetByServiceEventDelegate.builder()
        .setEntitiesSupplier(createTriggerEventDefinitionEntitiesSupplier())
        .build());
  }

  @Test
  public void testHandleWithoutRequest() {
    assertThrows(InvalidArgumentException.class, () -> delegate.handle(null));
  }

  @Test
  public void testHandleWithoutServiceParameter() {
    assertThrows(InvalidArgumentException.class,
        () -> delegate.handle(new TriggerEventDefinitionGetByServiceEventRequest().setEvent("event")));
  }

  @Test
  public void testHandleWithoutEventParameter() {
    assertThrows(InvalidArgumentException.class,
        () -> delegate.handle(new TriggerEventDefinitionGetByServiceEventRequest().setService("service")));
  }

  @Test
  public void testHandleServiceMismatch() {
    assertThrows(ObjectNotFoundException.class,
        () -> delegate.handle(new TriggerEventDefinitionGetByServiceEventRequest().setService("something").setEvent("event1")));
  }

  @Test
  public void testHandleEventMismatch() {
    assertThrows(ObjectNotFoundException.class,
        () -> delegate.handle(new TriggerEventDefinitionGetByServiceEventRequest().setService("service1").setEvent("something")));
  }

  @Test
  public void testHandleNotMatchingBoth() {
    assertThrows(ObjectNotFoundException.class,
        () -> delegate.handle(new TriggerEventDefinitionGetByServiceEventRequest().setService("service1").setEvent("event2")));
  }

  @Test
  public void testHandleMatchingBoth() throws Exception {
    TriggerEventDefinition result = delegate.handle(new TriggerEventDefinitionGetByServiceEventRequest().setService("service2").setEvent("event2"));
    assertNotNull(result);
    assertEquals("service2", result.getService());
    assertEquals("event2", result.getName());
  }
}
