package no.mnemonic.services.triggers.pipeline.worker.jexl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Class providing various formatting methods to the RuleEvaluationEngine which can be utilized inside TriggerRules.
 */
public class Formatters {

  /**
   * Format a timestamp as ISO8601.
   * <p>
   * Example usage: formatters:formatAsISO8601(1619004469000L) =&gt; 2021-04-21T11:27:49Z
   *
   * @param timestamp Epoch in milliseconds
   * @return ISO8601 formatted timestamp
   */
  public String formatAsISO8601(long timestamp) {
    return Instant.ofEpochMilli(timestamp).toString();
  }

  /**
   * Format a timestamp using a given pattern and time zone.
   * <p>
   * Example usage: formatters:formatTimestamp(1619004469000L, 'dd.MM.yyyy HH:mm:ss', 'UTC-3') =&gt; 21.04.2021 08:27:49
   *
   * @param timestamp Epoch in milliseconds
   * @param pattern   Pattern supported by {@link DateTimeFormatter}
   * @param timeZone  Time zone supported by {@link ZoneId}
   * @return Formatted timestamp
   */
  public String formatTimestamp(long timestamp, String pattern, String timeZone) {
    return DateTimeFormatter.ofPattern(pattern)
        .withZone(ZoneId.of(timeZone))
        .format(Instant.ofEpochMilli(timestamp));
  }
}
