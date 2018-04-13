package no.mnemonic.services.triggers.pipeline.worker;

import no.mnemonic.commons.component.Dependency;
import no.mnemonic.commons.component.LifecycleAspect;
import no.mnemonic.commons.component.ValidationAspect;
import no.mnemonic.commons.component.ValidationContext;
import no.mnemonic.commons.logging.Logger;
import no.mnemonic.commons.logging.Logging;
import no.mnemonic.commons.metrics.*;
import no.mnemonic.commons.utilities.StringUtils;
import no.mnemonic.commons.utilities.lambda.LambdaUtils;
import no.mnemonic.services.triggers.api.service.v1.TriggerAdministrationService;
import no.mnemonic.services.triggers.pipeline.api.SubmissionException;
import no.mnemonic.services.triggers.pipeline.api.TriggerEvent;
import no.mnemonic.services.triggers.pipeline.api.TriggerEventConsumer;

import javax.inject.Inject;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static no.mnemonic.services.triggers.pipeline.api.SubmissionException.ErrorCode.*;

/**
 * Worker implementation of a {@link TriggerEventConsumer} using a fixed number of worker threads and an in-memory
 * queue for dispatching submitted {@link TriggerEvent}s to those worker threads.
 */
public class InMemoryQueueWorker implements LifecycleAspect, MetricAspect, TriggerEventConsumer, ValidationAspect {

  private static final Logger LOGGER = Logging.getLogger(InMemoryQueueWorker.class);

  private static final int DEFAULT_NUMBER_OF_WORKER_THREADS = 4;
  private static final long DEFAULT_SUBMISSION_WAIT_TIME_SECONDS = 30;
  private static final long SHUTDOWN_TIMEOUT_SECONDS = 30;

  private final AtomicLong totalFailedTasksCounter = new AtomicLong();
  private final PerformanceMonitor evaluationMonitor = new PerformanceMonitor(TimeUnit.MINUTES, 60, 1);

  @Dependency
  private final TriggerAdministrationService service;

  private RuleEvaluationEngine ruleEvaluationEngine;
  private ThreadPoolExecutor threadPool;
  private Semaphore submissionLimiter;

  private int numberOfWorkerThreads = DEFAULT_NUMBER_OF_WORKER_THREADS;
  private long submissionWaitTimeSeconds = DEFAULT_SUBMISSION_WAIT_TIME_SECONDS;

  @Inject
  public InMemoryQueueWorker(TriggerAdministrationService service) {
    this.service = service;
    this.ruleEvaluationEngine = new RuleEvaluationEngine(this.service);
  }

  @Override
  public Metrics getMetrics() throws MetricException {
    MetricsData metrics = new MetricsData();

    if (threadPool != null) {
      metrics.addData("currentlyActiveTasks", threadPool.getActiveCount());
      metrics.addData("totalScheduledTasks", threadPool.getTaskCount());
      metrics.addData("totalCompletedTasks", threadPool.getCompletedTaskCount());
      metrics.addData("totalFailedTasks", totalFailedTasksCounter.get());
      metrics.addData("totalRuleEvaluationEngineInvocations", evaluationMonitor.getTotalInvocations());
      metrics.addData("totalRuleEvaluationEngineTimeSpent", evaluationMonitor.getTotalTimeSpent());
    }

    return new MetricsGroup()
        .addSubMetrics("ruleEvaluationEngine", ruleEvaluationEngine.getMetrics())
        .addSubMetrics("inMemoryQueueWorker", metrics);
  }

  @Override
  public void validate(ValidationContext validationContext) {
    if (numberOfWorkerThreads <= 0) validationContext.addError(this, "'numberOfWorkerThreads' must be > 0!");
    if (submissionWaitTimeSeconds <= 0) validationContext.addError(this, "'submissionWaitTimeSeconds' must be > 0!");
  }

  @Override
  public void startComponent() {
    threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfWorkerThreads);
    submissionLimiter = new Semaphore(threadPool.getMaximumPoolSize(), true); // One permit per available thread.
  }

  @Override
  public void stopComponent() {
    LambdaUtils.tryTo(() -> {
      if (threadPool == null) return;
      threadPool.shutdown();
      threadPool.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
      threadPool = null;
    }, ex -> LOGGER.warning(ex, "Failure while shutting down thread pool."));
  }

  @Override
  public void submit(TriggerEvent event) throws SubmissionException {
    if (threadPool == null) throw new IllegalStateException("Thread pool is not initialized! Component not started?");
    if (submissionLimiter == null) throw new IllegalStateException("Submission limiter is not initialized! Component not started?");
    validateTriggerEvent(event);

    try {
      // Wait until a processing thread becomes available before accepting the event.
      if (!submissionLimiter.tryAcquire(submissionWaitTimeSeconds, TimeUnit.SECONDS)) {
        LOGGER.info("No processing threads available [active tasks: %d, maximum pool size: %d].",
            threadPool.getActiveCount(), threadPool.getMaximumPoolSize());
        throw new SubmissionException(String.format("TriggerEvent with id = %s could not be accepted for processing. " +
            "No processing threads available.", event.getId()), NoResourcesAvailable);
      }

      // Schedule event for evaluation.
      threadPool.execute(createRuleEvaluationTask(event));
      if (LOGGER.isDebug()) {
        LOGGER.debug("Scheduled rule evaluation task for event with id = %s.", event.getId());
      }
    } catch (RejectedExecutionException ex) {
      LOGGER.info("No processing threads available [active tasks: %d, maximum pool size: %d].",
          threadPool.getActiveCount(), threadPool.getMaximumPoolSize());
      throw new SubmissionException(String.format("TriggerEvent with id = %s could not be accepted for processing. " +
          "No processing threads available.", event.getId()), ex, NoResourcesAvailable);
    } catch (InterruptedException ex) {
      LOGGER.info(ex, "Received interrupt, shutdown component.");
      stopComponent();
      throw new SubmissionException(String.format("TriggerEvent with id = %s could not be accepted for processing. " +
          "Component is shutting down.", event.getId()), ex, ComponentUnavailable);
    }
  }

  /**
   * Configure the number of used worker threads. Default is 4.
   *
   * @param numberOfWorkerThreads Number of worker threads
   * @return this
   */
  public InMemoryQueueWorker setNumberOfWorkerThreads(int numberOfWorkerThreads) {
    this.numberOfWorkerThreads = numberOfWorkerThreads;
    return this;
  }

  /**
   * Configure the maximum time period to wait for processing threads to become available when submitting events.
   * Default is 30 seconds.
   *
   * @param submissionWaitTimeSeconds Maximum submission wait time
   * @return this
   */
  public InMemoryQueueWorker setSubmissionWaitTimeSeconds(long submissionWaitTimeSeconds) {
    this.submissionWaitTimeSeconds = submissionWaitTimeSeconds;
    return this;
  }

  /**
   * Configure the used rule evaluation engine. Should only be used for testing.
   *
   * @param ruleEvaluationEngine Rule evaluation engine.
   * @return this
   */
  InMemoryQueueWorker setRuleEvaluationEngine(RuleEvaluationEngine ruleEvaluationEngine) {
    this.ruleEvaluationEngine = ruleEvaluationEngine;
    return this;
  }

  private void validateTriggerEvent(TriggerEvent event) throws SubmissionException {
    if (event == null) throw new SubmissionException("TriggerEvent is null!", InvalidTriggerEvent);
    // All fields below must be set for a valid TriggerEvent.
    if (event.getId() == null) throw new SubmissionException("TriggerEvent is missing id!", InvalidTriggerEvent);
    if (event.getTimestamp() <= 0) throw new SubmissionException("TriggerEvent is missing timestamp!", InvalidTriggerEvent);
    if (StringUtils.isBlank(event.getService())) throw new SubmissionException("TriggerEvent is missing service!", InvalidTriggerEvent);
    if (StringUtils.isBlank(event.getEvent())) throw new SubmissionException("TriggerEvent is missing event!", InvalidTriggerEvent);
    if (event.getOrganization() == null) throw new SubmissionException("TriggerEvent is missing organization!", InvalidTriggerEvent);
    if (event.getAccessMode() == null) throw new SubmissionException("TriggerEvent is missing access mode!", InvalidTriggerEvent);
  }

  private Runnable createRuleEvaluationTask(TriggerEvent event) {
    return () -> {
      if (LOGGER.isDebug()) {
        LOGGER.debug("Started rule evaluation task for event with id = %s.", event.getId());
      }

      try (TimerContext ignored = TimerContext.timerMillis(evaluationMonitor::invoked)) {
        ruleEvaluationEngine.evaluate(event);
      } catch (Exception ex) {
        LOGGER.error(ex, "Unexpected exception while executing rule evaluation task for event with id = %s.", event.getId());
        totalFailedTasksCounter.incrementAndGet();
      } finally {
        // Always signal that thread becomes available for scheduling again.
        submissionLimiter.release();
      }

      if (LOGGER.isDebug()) {
        LOGGER.debug("Finished rule evaluation task for event with id = %s.", event.getId());
      }
    };
  }
}
