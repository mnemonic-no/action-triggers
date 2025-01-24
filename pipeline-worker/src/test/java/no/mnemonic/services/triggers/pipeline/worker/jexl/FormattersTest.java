package no.mnemonic.services.triggers.pipeline.worker.jexl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormattersTest {

  private static final Formatters formatters = new Formatters();

  @Test
  public void testFormatAsISO8601() {
    assertEquals("2021-04-21T11:27:49Z", formatters.formatAsISO8601(1619004469000L));
  }

  @Test
  public void testFormatTimestamp() {
    assertEquals("21.04.2021 08:27:49", formatters.formatTimestamp(1619004469000L, "dd.MM.yyyy HH:mm:ss", "UTC-3"));
  }
}
