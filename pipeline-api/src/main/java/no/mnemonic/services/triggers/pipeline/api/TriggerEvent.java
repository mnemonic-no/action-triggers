package no.mnemonic.services.triggers.pipeline.api;

import java.util.Map;
import java.util.UUID;

/**
 * Interface defining an event which can be triggered by a service.
 */
public interface TriggerEvent {

  /**
   * Unique identifier of the TriggerEvent. Required.
   *
   * @return Unique identifier
   */
  UUID getId();

  /**
   * Timestamp in milliseconds when the TriggerEvent was created. Required.
   *
   * @return Created timestamp
   */
  long getTimestamp();

  /**
   * Name of service which created the TriggerEvent. Required.
   *
   * @return Service name
   */
  String getService();

  /**
   * Name of event as specified in the corresponding TriggerEventDefinition. Required.
   *
   * @return Event name
   */
  String getEvent();

  /**
   * Unique identifier of organization which "owns" the TriggerEvent (and data provided as context parameters). Required.
   *
   * @return Organization identifier
   */
  UUID getOrganization();

  /**
   * Access mode of the TriggerEvent. Required.
   *
   * @return Access mode
   */
  AccessMode getAccessMode();

  /**
   * Scope of the TriggerEvent. Optional.
   *
   * @return Scope
   */
  String getScope();

  /**
   * Context parameters as specified in the corresponding TriggerEventDefinition. Optional.
   *
   * @return Context parameters
   */
  Map<String, ?> getContextParameters();

}
