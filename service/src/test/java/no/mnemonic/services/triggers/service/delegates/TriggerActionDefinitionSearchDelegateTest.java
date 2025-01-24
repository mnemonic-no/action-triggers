package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.commons.utilities.collections.ListUtils;
import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.model.v1.TriggerActionDefinition;
import no.mnemonic.services.triggers.api.request.v1.TriggerActionDefinitionSearchRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TriggerActionDefinitionSearchDelegateTest extends AbstractDelegateTest {

  private final TriggerActionDefinitionSearchDelegate delegate = TriggerActionDefinitionSearchDelegate.builder()
      .setEntitiesSupplier(createTriggerActionDefinitionEntitiesSupplier())
      .setEntityConverter(createTriggerActionDefinitionEntityConverter())
      .build();

  @Test
  public void testInitializeDelegateWithoutEntitiesSupplier() {
    assertThrows(RuntimeException.class, () -> TriggerActionDefinitionSearchDelegate.builder()
        .setEntityConverter(createTriggerActionDefinitionEntityConverter())
        .build());
  }

  @Test
  public void testInitializeDelegateWithoutEntityConverter() {
    assertThrows(RuntimeException.class, () -> TriggerActionDefinitionSearchDelegate.builder()
        .setEntitiesSupplier(createTriggerActionDefinitionEntitiesSupplier())
        .build());
  }

  @Test
  public void testHandleWithoutRequest() {
    assertThrows(InvalidArgumentException.class, () -> delegate.handle(null));
  }

  @Test
  public void testHandleWithoutSearchParameters() throws Exception {
    Iterable<TriggerActionDefinition> result = delegate.handle(new TriggerActionDefinitionSearchRequest());
    assertNotNull(result);
    assertEquals(3, ListUtils.list(result.iterator()).size());
  }
}
