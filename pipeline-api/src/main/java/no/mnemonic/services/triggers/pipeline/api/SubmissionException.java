package no.mnemonic.services.triggers.pipeline.api;

/**
 * Exception thrown when a {@link TriggerEvent} could not be submitted for processing (see {@link TriggerEventConsumer}).
 */
public class SubmissionException extends Exception {

  private static final long serialVersionUID = 6907693578839211086L;

  private final ErrorCode errorCode;

  /**
   * Create a SubmissionException with a message and an error code.
   *
   * @param message   Exception message
   * @param errorCode Error code
   */
  public SubmissionException(String message, ErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  /**
   * Create a SubmissionException with a message, a cause and an error code.
   *
   * @param message   Exception message
   * @param cause     Exception cause
   * @param errorCode Error code
   */
  public SubmissionException(String message, Throwable cause, ErrorCode errorCode) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public enum ErrorCode {
    InvalidTriggerEvent, NoResourcesAvailable, ComponentUnavailable
  }
}
