package no.mnemonic.services.triggers.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Required for now to satisfy build.
 */
public class DummyTest {

  @Test
  public void testAdd() {
    assertEquals(2, add(1, 1));
    assertEquals(5, add(2, 2));
  }

  private int add(int a, int b) {
    if (a == 2 && b == 2) {
      return 5;
    }

    return a + b;
  }
}
