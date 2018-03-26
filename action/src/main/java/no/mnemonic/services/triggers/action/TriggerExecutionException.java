package no.mnemonic.services.triggers.action;

/**
 * Exception thrown when an action could not be executed.
 */
public class TriggerExecutionException extends Exception {

  private static final long serialVersionUID = -7505361981173157903L;

  /**
   * Create a TriggerExecutionException with a message.
   *
   * @param message Exception message
   */
  public TriggerExecutionException(String message) {
    super(message);
  }

  /**
   * Create a TriggerExecutionException with a message and a cause.
   *
   * @param message Exception message
   * @param cause   Exception cause
   */
  public TriggerExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
