package no.mnemonic.services.triggers.action.exceptions;

/**
 * Exception thrown when an action could not be initialized.
 */
public class TriggerInitializationException extends Exception {

  private static final long serialVersionUID = 9170020544543312960L;

  /**
   * Create a TriggerInitializationException with a message.
   *
   * @param message Exception message
   */
  public TriggerInitializationException(String message) {
    super(message);
  }

  /**
   * Create a TriggerInitializationException with a message and a cause.
   *
   * @param message Exception message
   * @param cause   Exception cause
   */
  public TriggerInitializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
