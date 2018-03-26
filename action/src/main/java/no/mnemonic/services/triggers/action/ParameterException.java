package no.mnemonic.services.triggers.action;

/**
 * Exception thrown for invalid or missing parameters passed to an action, either on init() or on trigger().
 * Required parameters or parameter validation is implementation-specific for each action.
 */
public class ParameterException extends Exception {

  private static final long serialVersionUID = -9179331498041231040L;

  private final String parameter;

  /**
   * Create a ParameterException with a message and the offending parameter (either missing or invalid).
   *
   * @param message   Exception message
   * @param parameter Offending parameter
   */
  public ParameterException(String message, String parameter) {
    super(message);
    this.parameter = parameter;
  }

  /**
   * Create a ParameterException with a message, a cause and the offending parameter (either missing or invalid).
   *
   * @param message   Exception message
   * @param cause     Exception cause
   * @param parameter Offending parameter
   */
  public ParameterException(String message, Throwable cause, String parameter) {
    super(message, cause);
    this.parameter = parameter;
  }

  /**
   * Return name of the offending parameter.
   *
   * @return Offending parameter
   */
  public String getParameter() {
    return parameter;
  }
}
