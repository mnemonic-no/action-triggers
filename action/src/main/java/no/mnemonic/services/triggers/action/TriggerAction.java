package no.mnemonic.services.triggers.action;

import no.mnemonic.services.triggers.action.exceptions.ParameterException;
import no.mnemonic.services.triggers.action.exceptions.TriggerExecutionException;
import no.mnemonic.services.triggers.action.exceptions.TriggerInitializationException;

import java.util.Map;

/**
 * Common interface every action must implement.
 * <p>
 * An action will be executed in the following order:
 * <ol>
 * <li>
 * An instance of the action will be created using {@link Class#newInstance()}. Because of this, every implementation
 * has to provide a public nullary constructor. Only generic initialization of the action should be performed in this
 * step, such has initializing libraries, etc.
 * </li>
 * <li>
 * The {@link TriggerAction#init(Map)} method will be called to initialize the action based on a map of initialization
 * parameters, which will be populated based on the configuration in a TriggerActionDefinition. Those parameters are
 * statically defined in the TriggerActionDefinition and are independent of the triggered event.
 * </li>
 * <li>
 * The {@link TriggerAction#trigger(Map)} method will be called to execute the action based on a map of trigger parameters,
 * which will be populated based on the configuration in a TriggerRule. Those parameters are dynamically defined in the
 * TriggerRule and might be populated from the triggered event.
 * </li>
 * <li>
 * The created action instance will be closed by calling {@link TriggerAction#close()} in order to release any previously
 * acquired resources. Implementing {@link TriggerAction#close()} is optional and will do nothing by default.
 * </li>
 * </ol>
 * <p>
 * Every action specifies its own initialization and trigger parameters (key/value pairs). Keys and values are provided
 * as strings and the interpretation of values is implementation-specific as well (single values, comma-separated list
 * of values, ...). An action should throw a {@link ParameterException} if required parameters are missing or parameters
 * have invalid values.
 */
public interface TriggerAction extends AutoCloseable {

  /**
   * Initialize an action based on a map of static initialization parameters.
   * <p>
   * An implementation should throw a {@link ParameterException} if required parameters are missing or parameter values
   * are invalid. It should throw a {@link TriggerInitializationException} if the action could not be initialized for
   * any other reason.
   *
   * @param initParameters Static initialization parameters populated from a TriggerActionDefinition
   * @throws ParameterException             Thrown if parameters are missing or invalid
   * @throws TriggerInitializationException Thrown if an action could not be initialized
   */
  void init(Map<String, String> initParameters) throws ParameterException, TriggerInitializationException;

  /**
   * Execute an action with a map of dynamic trigger parameters.
   * <p>
   * An implementation should throw a {@link ParameterException} if required parameters are missing or parameter values
   * are invalid. It should throw a {@link TriggerExecutionException} if the action could not be executed for any other
   * reason, for example if a required connection to a remote host could not be established.
   * <p>
   * This method might be called multiple times after the action was initialized once, even if a previous invocation
   * failed. Implementation must be able to handle multiple calls and must be able to recover from failed invocations.
   *
   * @param triggerParameters Dynamic trigger parameters populated from a TriggerRule
   * @throws ParameterException        Thrown if parameters are missing or invalid
   * @throws TriggerExecutionException Thrown if an action could not be executed
   */
  void trigger(Map<String, String> triggerParameters) throws ParameterException, TriggerExecutionException;

  /**
   * Close an action after it was executed to release any acquired resources.
   * <p>
   * The default implementation does nothing, but implementations should override this method if they hold acquired
   * resources which should be released after execution.
   */
  @Override
  default void close() {
    // Noop by default
  }
}
