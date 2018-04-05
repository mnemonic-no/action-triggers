package no.mnemonic.services.triggers.pipeline.api;

/**
 * Interface used for submitting {@link TriggerEvent}s to an external consumer for processing.
 */
public interface TriggerEventConsumer {

  /**
   * Submit {@link TriggerEvent}s for processing to an external consumer.
   * <p>
   * A {@link SubmissionException} will be thrown if the event could not be accepted for processing. No exception does
   * not indicate that the event was successfully processed. Processing will be performed independently from submission
   * to this consumer and the submitting service will not be notified about failed or successful processing of an event.
   * <p>
   * Events should only be submitted after the operation creating the event finished completely, for example only after
   * all transactions have been successfully committed.
   *
   * @param event {@link TriggerEvent} submitted for processing
   * @throws SubmissionException Thrown if event could not be accepted for processing
   */
  void submit(TriggerEvent event) throws SubmissionException;

}
