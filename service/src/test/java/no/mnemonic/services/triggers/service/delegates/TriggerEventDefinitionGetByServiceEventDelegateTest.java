package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.exceptions.ObjectNotFoundException;
import no.mnemonic.services.triggers.api.model.v1.TriggerEventDefinition;
import no.mnemonic.services.triggers.api.request.v1.TriggerEventDefinitionGetByServiceEventRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TriggerEventDefinitionGetByServiceEventDelegateTest extends AbstractDelegateTest {

  private final TriggerEventDefinitionGetByServiceEventDelegate delegate = TriggerEventDefinitionGetByServiceEventDelegate.builder()
      .setEntitiesSupplier(createTriggerEventDefinitionEntitiesSupplier())
      .setEntityConverter(createTriggerEventDefinitionEntityConverter())
      .build();

  @Test(expected = RuntimeException.class)
  public void testInitializeDelegateWithoutEntitiesSupplier() {
    TriggerEventDefinitionGetByServiceEventDelegate.builder()
        .setEntityConverter(createTriggerEventDefinitionEntityConverter())
        .build();
  }

  @Test(expected = RuntimeException.class)
  public void testInitializeDelegateWithoutEntityConverter() {
    TriggerEventDefinitionGetByServiceEventDelegate.builder()
        .setEntitiesSupplier(createTriggerEventDefinitionEntitiesSupplier())
        .build();
  }

  @Test(expected = InvalidArgumentException.class)
  public void testHandleWithoutRequest() throws Exception {
    delegate.handle(null);
  }

  @Test(expected = InvalidArgumentException.class)
  public void testHandleWithoutServiceParameter() throws Exception {
    delegate.handle(new TriggerEventDefinitionGetByServiceEventRequest().setEvent("event"));
  }

  @Test(expected = InvalidArgumentException.class)
  public void testHandleWithoutEventParameter() throws Exception {
    delegate.handle(new TriggerEventDefinitionGetByServiceEventRequest().setService("service"));
  }

  @Test(expected = ObjectNotFoundException.class)
  public void testHandleServiceMismatch() throws Exception {
    delegate.handle(new TriggerEventDefinitionGetByServiceEventRequest().setService("something").setEvent("event1"));
  }

  @Test(expected = ObjectNotFoundException.class)
  public void testHandleEventMismatch() throws Exception {
    delegate.handle(new TriggerEventDefinitionGetByServiceEventRequest().setService("service1").setEvent("something"));
  }

  @Test(expected = ObjectNotFoundException.class)
  public void testHandleNotMatchingBoth() throws Exception {
    delegate.handle(new TriggerEventDefinitionGetByServiceEventRequest().setService("service1").setEvent("event2"));
  }

  @Test
  public void testHandleMatchingBoth() throws Exception {
    TriggerEventDefinition result = delegate.handle(new TriggerEventDefinitionGetByServiceEventRequest().setService("service2").setEvent("event2"));
    assertNotNull(result);
    assertEquals("service2", result.getService());
    assertEquals("event2", result.getName());
  }
}
