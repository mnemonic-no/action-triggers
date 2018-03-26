package no.mnemonic.services.triggers.action;

import no.mnemonic.commons.logging.Logger;
import no.mnemonic.commons.logging.Logging;

import java.util.Map;

/**
 * Noop implementation of a TriggerAction useful for testing. It requires no parameters.
 */
public class NoopTriggerAction implements TriggerAction {

  private static final Logger LOGGER = Logging.getLogger(NoopTriggerAction.class);

  @Override
  public void init(Map<String, String> initParameters) {
    LOGGER.info("Called init() of NoopTriggerAction with parameters %s.", initParameters);
  }

  @Override
  public void trigger(Map<String, String> triggerParameters) {
    LOGGER.info("Called trigger() of NoopTriggerAction with parameters %s.", triggerParameters);
  }
}
