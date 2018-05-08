package no.mnemonic.services.triggers.pipeline.worker.jexl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrintableStringWriterTest {

  private static final Object obj = new Object() {
    @Override
    public String toString() {
      return "42";
    }
  };

  @Test
  public void testPrintNull() {
    PrintableStringWriter writer = new PrintableStringWriter();
    writer.print(null);
    assertEquals("", writer.toString());
  }

  @Test
  public void testPrintObject() {
    PrintableStringWriter writer = new PrintableStringWriter();
    writer.print(obj);
    assertEquals("42", writer.toString());
  }
}
