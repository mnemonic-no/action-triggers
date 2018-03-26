package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.exceptions.ObjectNotFoundException;
import no.mnemonic.services.triggers.api.model.v1.TriggerActionDefinition;
import no.mnemonic.services.triggers.api.request.v1.TriggerActionDefinitionGetByNameRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TriggerActionDefinitionGetByNameDelegateTest extends AbstractDelegateTest {

  private final TriggerActionDefinitionGetByNameDelegate delegate = TriggerActionDefinitionGetByNameDelegate.builder()
      .setEntitiesSupplier(createTriggerActionDefinitionEntitiesSupplier())
      .setEntityConverter(createTriggerActionDefinitionEntityConverter())
      .build();

  @Test(expected = RuntimeException.class)
  public void testInitializeDelegateWithoutEntitiesSupplier() {
    TriggerActionDefinitionGetByNameDelegate.builder()
        .setEntityConverter(createTriggerActionDefinitionEntityConverter())
        .build();
  }

  @Test(expected = RuntimeException.class)
  public void testInitializeDelegateWithoutEntityConverter() {
    TriggerActionDefinitionGetByNameDelegate.builder()
        .setEntitiesSupplier(createTriggerActionDefinitionEntitiesSupplier())
        .build();
  }

  @Test(expected = InvalidArgumentException.class)
  public void testHandleWithoutRequest() throws Exception {
    delegate.handle(null);
  }

  @Test(expected = InvalidArgumentException.class)
  public void testHandleWithoutNameParameter() throws Exception {
    delegate.handle(new TriggerActionDefinitionGetByNameRequest());
  }

  @Test(expected = ObjectNotFoundException.class)
  public void testHandleNameMismatch() throws Exception {
    delegate.handle(new TriggerActionDefinitionGetByNameRequest().setName("something"));
  }

  @Test
  public void testHandleNameMatch() throws Exception {
    TriggerActionDefinition result = delegate.handle(new TriggerActionDefinitionGetByNameRequest().setName("name2"));
    assertNotNull(result);
    assertEquals("name2", result.getName());
  }
}
