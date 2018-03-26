package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.commons.utilities.collections.ListUtils;
import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.model.v1.TriggerActionDefinition;
import no.mnemonic.services.triggers.api.request.v1.TriggerActionDefinitionSearchRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TriggerActionDefinitionSearchDelegateTest extends AbstractDelegateTest {

  private final TriggerActionDefinitionSearchDelegate delegate = TriggerActionDefinitionSearchDelegate.builder()
      .setEntitiesSupplier(createTriggerActionDefinitionEntitiesSupplier())
      .setEntityConverter(createTriggerActionDefinitionEntityConverter())
      .build();

  @Test(expected = RuntimeException.class)
  public void testInitializeDelegateWithoutEntitiesSupplier() {
    TriggerActionDefinitionSearchDelegate.builder()
        .setEntityConverter(createTriggerActionDefinitionEntityConverter())
        .build();
  }

  @Test(expected = RuntimeException.class)
  public void testInitializeDelegateWithoutEntityConverter() {
    TriggerActionDefinitionSearchDelegate.builder()
        .setEntitiesSupplier(createTriggerActionDefinitionEntitiesSupplier())
        .build();
  }

  @Test(expected = InvalidArgumentException.class)
  public void testHandleWithoutRequest() throws Exception {
    delegate.handle(null);
  }

  @Test
  public void testHandleWithoutSearchParameters() throws Exception {
    Iterable<TriggerActionDefinition> result = delegate.handle(new TriggerActionDefinitionSearchRequest());
    assertNotNull(result);
    assertEquals(3, ListUtils.list(result.iterator()).size());
  }
}
