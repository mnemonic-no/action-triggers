package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.exceptions.ObjectNotFoundException;
import no.mnemonic.services.triggers.api.model.v1.TriggerActionDefinition;
import no.mnemonic.services.triggers.api.request.v1.TriggerActionDefinitionGetByNameRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TriggerActionDefinitionGetByNameDelegateTest extends AbstractDelegateTest {

  private final TriggerActionDefinitionGetByNameDelegate delegate = TriggerActionDefinitionGetByNameDelegate.builder()
      .setEntitiesSupplier(createTriggerActionDefinitionEntitiesSupplier())
      .setEntityConverter(createTriggerActionDefinitionEntityConverter())
      .build();

  @Test
  public void testInitializeDelegateWithoutEntitiesSupplier() {
    assertThrows(RuntimeException.class, () -> TriggerActionDefinitionGetByNameDelegate.builder()
        .setEntityConverter(createTriggerActionDefinitionEntityConverter())
        .build());
  }

  @Test
  public void testInitializeDelegateWithoutEntityConverter() {
    assertThrows(RuntimeException.class, () -> TriggerActionDefinitionGetByNameDelegate.builder()
        .setEntitiesSupplier(createTriggerActionDefinitionEntitiesSupplier())
        .build());
  }

  @Test
  public void testHandleWithoutRequest() {
    assertThrows(InvalidArgumentException.class, () -> delegate.handle(null));
  }

  @Test
  public void testHandleWithoutNameParameter() {
    assertThrows(InvalidArgumentException.class, () -> delegate.handle(new TriggerActionDefinitionGetByNameRequest()));
  }

  @Test
  public void testHandleNameMismatch() {
    assertThrows(ObjectNotFoundException.class, () -> delegate.handle(new TriggerActionDefinitionGetByNameRequest().setName("something")));
  }

  @Test
  public void testHandleNameMatch() throws Exception {
    TriggerActionDefinition result = delegate.handle(new TriggerActionDefinitionGetByNameRequest().setName("name2"));
    assertNotNull(result);
    assertEquals("name2", result.getName());
  }
}
