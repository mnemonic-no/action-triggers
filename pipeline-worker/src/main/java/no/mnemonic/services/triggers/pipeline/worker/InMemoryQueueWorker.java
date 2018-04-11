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
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Worker implementation of a {@link TriggerEventConsumer} using a fixed number of worker threads and an in-memory
 * queue for dispatching submitted {@link TriggerEvent}s to those worker threads.
 */
public class InMemoryQueueWorker implements LifecycleAspect, MetricAspect, TriggerEventConsumer, ValidationAspect {

  private static final Logger LOGGER = Logging.getLogger(InMemoryQueueWorker.class);

  private static final int DEFAULT_NUMBER_OF_WORKER_THREADS = 4;
  private static final long SHUTDOWN_TIMEOUT_SECONDS = 30;

  private final AtomicLong totalFailedTasksCounter = new AtomicLong();
  private final PerformanceMonitor evaluationMonitor = new PerformanceMonitor(TimeUnit.MINUTES, 60, 1);

  @Dependency
  private final TriggerAdministrationService service;

  private RuleEvaluationEngine ruleEvaluationEngine;
  private ThreadPoolExecutor threadPool;

  private int numberOfWorkerThreads = DEFAULT_NUMBER_OF_WORKER_THREADS;

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
      metrics.addData("totalTasksLastHour", evaluationMonitor.getInvocationsLast(TimeUnit.HOURS, 1));
      metrics.addData("totalTasksLastTenMinutes", evaluationMonitor.getInvocationsLast(TimeUnit.MINUTES, 10));
      metrics.addData("totalTasksLastMinute", evaluationMonitor.getInvocationsLast(TimeUnit.MINUTES, 1));
      metrics.addData("averageTasksPerSecondLastHour", evaluationMonitor.getInvocationsPerSecondLast(TimeUnit.HOURS, 1));
      metrics.addData("averageTasksPerSecondLastTenMinutes", evaluationMonitor.getInvocationsPerSecondLast(TimeUnit.MINUTES, 10));
      metrics.addData("averageTasksPerSecondLastMinute", evaluationMonitor.getInvocationsPerSecondLast(TimeUnit.MINUTES, 1));
      metrics.addData("averageTaskInvocationTimeLastHour", evaluationMonitor.getTimeSpentPerInvocationLast(TimeUnit.HOURS, 1));
      metrics.addData("averageTaskInvocationTimeLastTenMinutes", evaluationMonitor.getTimeSpentPerInvocationLast(TimeUnit.MINUTES, 10));
      metrics.addData("averageTaskInvocationTimeLastMinute", evaluationMonitor.getTimeSpentPerInvocationLast(TimeUnit.MINUTES, 1));
    }

    return new MetricsGroup()
        .addSubMetrics("ruleEvaluationEngine", ruleEvaluationEngine.getMetrics())
        .addSubMetrics("inMemoryQueueWorker", metrics);
  }

  @Override
  public void validate(ValidationContext validationContext) {
    if (numberOfWorkerThreads <= 0) validationContext.addError(this, "'numberOfWorkerThreads' must be > 0!");
  }

  @Override
  public void startComponent() {
    threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfWorkerThreads);
  }

  @Override
  public void stopComponent() {
    LambdaUtils.tryTo(() -> {
      if (threadPool == null) return;
      threadPool.shutdown();
      threadPool.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }, ex -> LOGGER.warning(ex, "Failure while shutting down thread pool."));
  }

  @Override
  public void submit(TriggerEvent event) throws SubmissionException {
    if (threadPool == null) throw new IllegalStateException("Thread pool is not initialized! Component not started?");
    validateTriggerEvent(event);

    try {
      threadPool.execute(createRuleEvaluationTask(event));
      if (LOGGER.isDebug()) {
        LOGGER.debug("Scheduled rule evaluation task for event with id = %s.", event.getId());
      }
    } catch (RejectedExecutionException ex) {
      throw new SubmissionException(String.format("TriggerEvent with id = %s could not be accepted for processing.", event.getId()), ex);
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
    if (event == null) throw new SubmissionException("TriggerEvent is null!");
    // All fields below must be set for a valid TriggerEvent.
    if (event.getId() == null) throw new SubmissionException("TriggerEvent is missing id!");
    if (event.getTimestamp() <= 0) throw new SubmissionException("TriggerEvent is missing timestamp!");
    if (StringUtils.isBlank(event.getService())) throw new SubmissionException("TriggerEvent is missing service!");
    if (StringUtils.isBlank(event.getEvent())) throw new SubmissionException("TriggerEvent is missing event!");
    if (event.getOrganization() == null) throw new SubmissionException("TriggerEvent is missing organization!");
    if (event.getAccessMode() == null) throw new SubmissionException("TriggerEvent is missing access mode!");
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
      }

      if (LOGGER.isDebug()) {
        LOGGER.debug("Finished rule evaluation task for event with id = %s.", event.getId());
      }
    };
  }
}
