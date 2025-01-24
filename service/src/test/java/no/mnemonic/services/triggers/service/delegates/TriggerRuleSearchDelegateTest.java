package no.mnemonic.services.triggers.service.delegates;

import no.mnemonic.commons.utilities.collections.ListUtils;
import no.mnemonic.services.triggers.api.exceptions.InvalidArgumentException;
import no.mnemonic.services.triggers.api.model.v1.TriggerRule;
import no.mnemonic.services.triggers.api.request.v1.TriggerRuleSearchRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TriggerRuleSearchDelegateTest extends AbstractDelegateTest {

  private final TriggerRuleSearchDelegate delegate = TriggerRuleSearchDelegate.builder()
      .setEntitiesSupplier(createTriggerRuleEntitiesSupplier())
      .setEntityConverter(createTriggerRuleEntityConverter())
      .build();

  @Test
  public void testInitializeDelegateWithoutEntitiesSupplier() {
    assertThrows(RuntimeException.class, () -> TriggerRuleSearchDelegate.builder()
        .setEntityConverter(createTriggerRuleEntityConverter())
        .build());
  }

  @Test
  public void testInitializeDelegateWithoutEntityConverter() {
    assertThrows(RuntimeException.class, () -> TriggerRuleSearchDelegate.builder()
        .setEntitiesSupplier(createTriggerRuleEntitiesSupplier())
        .build());
  }

  @Test
  public void testHandleWithoutRequest() {
    assertThrows(InvalidArgumentException.class, () -> delegate.handle(null));
  }

  @Test
  public void testHandleWithoutFiltering() throws Exception {
    Iterable<TriggerRule> result = delegate.handle(new TriggerRuleSearchRequest());
    assertNotNull(result);
    assertEquals(3, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testHandleWithFilteringOnService() throws Exception {
    Iterable<TriggerRule> result = delegate.handle(new TriggerRuleSearchRequest().addService("service2"));
    assertNotNull(result);
    assertEquals(1, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testHandleWithFilteringOnMultipleServices() throws Exception {
    Iterable<TriggerRule> result = delegate.handle(new TriggerRuleSearchRequest()
        .addService("service1")
        .addService("service3")
    );
    assertNotNull(result);
    assertEquals(2, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testHandleWithFilteringOnEvent() throws Exception {
    Iterable<TriggerRule> result = delegate.handle(new TriggerRuleSearchRequest().addEvent("event2"));
    assertNotNull(result);
    assertEquals(1, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testHandleWithFilteringOnMultipleEvents() throws Exception {
    Iterable<TriggerRule> result = delegate.handle(new TriggerRuleSearchRequest()
        .addEvent("event1")
        .addEvent("event3")
    );
    assertNotNull(result);
    assertEquals(2, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testHandleWithFilteringOnBothServiceAndEvent() throws Exception {
    Iterable<TriggerRule> result = delegate.handle(new TriggerRuleSearchRequest()
        .addService("service1")
        .addEvent("event1")
    );
    assertNotNull(result);
    assertEquals(1, ListUtils.list(result.iterator()).size());
  }

  @Test
  public void testHandleWithFilteringOnBothServiceAndEventNoMatch() throws Exception {
    Iterable<TriggerRule> result = delegate.handle(new TriggerRuleSearchRequest()
        .addService("service1")
        .addEvent("event2")
    );
    assertNotNull(result);
    assertEquals(0, ListUtils.list(result.iterator()).size());
  }
}
