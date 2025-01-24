package no.mnemonic.services.triggers.pipeline.worker;

import no.mnemonic.commons.component.ValidationContext;
import no.mnemonic.commons.metrics.MetricsData;
import no.mnemonic.commons.utilities.lambda.LambdaUtils;
import no.mnemonic.services.triggers.api.service.v1.TriggerAdministrationService;
import no.mnemonic.services.triggers.pipeline.api.SubmissionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static no.mnemonic.services.triggers.pipeline.api.SubmissionException.ErrorCode.NoResourcesAvailable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InMemoryQueueWorkerTest {

  @Mock
  private TriggerAdministrationService service;
  @Mock
  private RuleEvaluationEngine engine;

  private AtomicBoolean finishedSignal;
  private InMemoryQueueWorker worker;

  @BeforeEach
  public void setUp() throws Exception {
    lenient().when(engine.getMetrics()).thenReturn(new MetricsData());

    finishedSignal = new AtomicBoolean(false);
    worker = new InMemoryQueueWorker(service)
        .setRuleEvaluationEngine(engine);
    worker.startComponent();
  }

  @AfterEach
  public void cleanUp() {
    if (worker != null) {
      worker.stopComponent();
    }
  }

  @Test
  public void testValidateWithZeroWorkerThreadsFails() {
    ValidationContext context = new ValidationContext();
    worker.setNumberOfWorkerThreads(0)
        .validate(context);
    assertFalse(context.isValid());
  }

  @Test
  public void testValidateWithZeroSubmissionWaitTimeFails() {
    ValidationContext context = new ValidationContext();
    worker.setSubmissionWaitTimeSeconds(0)
        .validate(context);
    assertFalse(context.isValid());
  }

  @Test
  public void testSubmitWithoutThreadPoolThrowsException() {
    assertThrows(IllegalStateException.class, () -> new InMemoryQueueWorker(service).submit(new TestTriggerEvent()));
  }

  @Test
  public void testSubmitWithoutEventThrowsException() {
    assertThrows(SubmissionException.class, () -> worker.submit(null));
  }

  @Test
  public void testSubmitEventWithoutIdThrowsException() {
    assertThrows(SubmissionException.class, () -> worker.submit(new TestTriggerEvent().setId(null)));
  }

  @Test
  public void testSubmitEventWithoutTimestampThrowsException() {
    assertThrows(SubmissionException.class, () -> worker.submit(new TestTriggerEvent().setTimestamp(0)));
  }

  @Test
  public void testSubmitEventWithoutServiceThrowsException() {
    assertThrows(SubmissionException.class, () -> worker.submit(new TestTriggerEvent().setService("")));
  }

  @Test
  public void testSubmitEventWithoutEventThrowsException() {
    assertThrows(SubmissionException.class, () -> worker.submit(new TestTriggerEvent().setEvent("")));
  }

  @Test
  public void testSubmitEventWithoutOrganizationThrowsException() {
    assertThrows(SubmissionException.class, () -> worker.submit(new TestTriggerEvent().setOrganization(null)));
  }

  @Test
  public void testSubmitEventWithoutAccessModeThrowsException() {
    assertThrows(SubmissionException.class, () -> worker.submit(new TestTriggerEvent().setAccessMode(null)));
  }

  @Test
  public void testSubmitAndEvaluateSuccess() throws Exception {
    doAnswer(i -> {
      finishedSignal.set(true);
      return null;
    }).when(engine).evaluate(any());

    TestTriggerEvent event = new TestTriggerEvent();
    worker.submit(event);

    if (LambdaUtils.waitFor(finishedSignal::get, 10, TimeUnit.SECONDS)) {
      assertEquals(1L, worker.getMetrics().getSubMetrics("inMemoryQueueWorker").getData("totalCompletedTasks"));
      assertEquals(0L, worker.getMetrics().getSubMetrics("inMemoryQueueWorker").getData("totalFailedTasks"));
      verify(engine).evaluate(event);
    } else {
      fail("Rule evaluation task did not finish!");
    }
  }

  @Test
  public void testSubmitAndEvaluateFailure() throws Exception {
    doAnswer(i -> {
      finishedSignal.set(true);
      throw new RuntimeException();
    }).when(engine).evaluate(any());

    TestTriggerEvent event = new TestTriggerEvent();
    worker.submit(event);

    if (LambdaUtils.waitFor(finishedSignal::get, 10, TimeUnit.SECONDS)) {
      assertEquals(1L, worker.getMetrics().getSubMetrics("inMemoryQueueWorker").getData("totalCompletedTasks"));
      assertEquals(1L, worker.getMetrics().getSubMetrics("inMemoryQueueWorker").getData("totalFailedTasks"));
      verify(engine).evaluate(event);
    } else {
      fail("Rule evaluation task did not finish!");
    }
  }

  @Test
  public void testSubmitWithoutAvailableThread() {
    doAnswer(i -> {
      Thread.sleep(3_000);
      return null;
    }).when(engine).evaluate(any());

    TestTriggerEvent event1 = new TestTriggerEvent();
    TestTriggerEvent event2 = new TestTriggerEvent();

    try {
      worker.setNumberOfWorkerThreads(1)
          .setSubmissionWaitTimeSeconds(1);
      worker.startComponent();
      worker.submit(event1);
      worker.submit(event2);

      fail("Worker did not throw SubmissionException!");
    } catch (SubmissionException ex) {
      assertEquals(NoResourcesAvailable, ex.getErrorCode());
      verify(engine).evaluate(event1);
      verify(engine, never()).evaluate(event2);
    }
  }

  @Test
  public void testSubmitMultipleEventsRateLimited() throws Exception {
    AtomicInteger taskCounter = new AtomicInteger();
    doAnswer(i -> {
      Thread.sleep(1_000);
      if (taskCounter.incrementAndGet() == 3) {
        finishedSignal.set(true);
      }
      return null;
    }).when(engine).evaluate(any());

    worker.setNumberOfWorkerThreads(1);
    worker.startComponent();
    worker.submit(new TestTriggerEvent());
    worker.submit(new TestTriggerEvent());
    worker.submit(new TestTriggerEvent());

    if (LambdaUtils.waitFor(finishedSignal::get, 10, TimeUnit.SECONDS)) {
      assertEquals(3L, worker.getMetrics().getSubMetrics("inMemoryQueueWorker").getData("totalCompletedTasks"));
      assertEquals(0L, worker.getMetrics().getSubMetrics("inMemoryQueueWorker").getData("totalFailedTasks"));
      verify(engine, times(3)).evaluate(any());
    } else {
      fail("Rule evaluation task did not finish!");
    }
  }
}
