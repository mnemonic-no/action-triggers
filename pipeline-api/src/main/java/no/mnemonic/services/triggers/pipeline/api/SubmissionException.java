package no.mnemonic.services.triggers.pipeline.api;

/**
 * Exception thrown when a {@link TriggerEvent} could not be submitted for processing (see {@link TriggerEventConsumer}).
 */
public class SubmissionException extends Exception {

  private static final long serialVersionUID = 6907693578839211086L;

  /**
   * Create a SubmissionException with a message.
   *
   * @param message Exception message
   */
  public SubmissionException(String message) {
    super(message);
  }

  /**
   * Create a SubmissionException with a message and a cause.
   *
   * @param message Exception message
   * @param cause   Exception cause
   */
  public SubmissionException(String message, Throwable cause) {
    super(message, cause);
  }
}
