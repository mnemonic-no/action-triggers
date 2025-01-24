package no.mnemonic.services.triggers.action;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class NoopTriggerActionTest {

  @Test
  public void testNoopTriggerAction() throws Exception {
    Map<String, String> parameters = new HashMap<>() {{
      put("a", "b");
      put("1", "2");
    }};

    try (TriggerAction action = NoopTriggerAction.class.getDeclaredConstructor().newInstance()) {
      assertDoesNotThrow(() -> action.init(parameters));
      assertDoesNotThrow(() -> action.trigger(parameters));
    }
  }
}
