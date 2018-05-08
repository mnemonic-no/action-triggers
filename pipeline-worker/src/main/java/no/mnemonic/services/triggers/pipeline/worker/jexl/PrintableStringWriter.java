package no.mnemonic.services.triggers.pipeline.worker.jexl;

import java.io.StringWriter;

/**
 * A {@link StringWriter} extension adding a {@link #print(Object)} method in order to allow using the print()
 * statement inside a JEXL template, e.g. $jexl.print(42).
 */
public class PrintableStringWriter extends StringWriter {

  /**
   * Writes the string representation of an arbitrary object. Allows to use $jexl.print(42) inside a JEXL template.
   *
   * @param obj Object to print
   */
  public void print(Object obj) {
    if (obj == null) return;
    super.write(obj.toString());
  }
}
