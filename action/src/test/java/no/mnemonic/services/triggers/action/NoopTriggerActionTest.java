package no.mnemonic.services.triggers.action;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

public class NoopTriggerActionTest {

  @Test
  public void testNoopTriggerAction() {
    Map<String, String> parameters = new HashMap<String, String>() {{
      put("a", "b");
      put("1", "2");
    }};

    try (TriggerAction action = NoopTriggerAction.class.newInstance()) {
      action.init(parameters);
      action.trigger(parameters);
    } catch (Exception ex) {
      fail(ex.getMessage());
    }
  }
}
