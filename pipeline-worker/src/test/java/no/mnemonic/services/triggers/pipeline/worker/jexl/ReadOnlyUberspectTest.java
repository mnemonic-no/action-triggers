package no.mnemonic.services.triggers.pipeline.worker.jexl;

import no.mnemonic.commons.utilities.collections.CollectionUtils;
import org.apache.commons.jexl3.*;
import org.apache.commons.jexl3.internal.Engine;
import org.junit.Before;
import org.junit.Test;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class ReadOnlyUberspectTest {

  private JexlEngine expressionEngine;
  private JxltEngine templateEngine;

  @Before
  public void setUp() {
    // Use same set up as RuleEvaluationEngine.
    expressionEngine = new JexlBuilder()
        .silent(false)
        .strict(true)
        .uberspect(new ReadOnlyUberspect(Engine.getUberspect(null, null)))
        .create();
    templateEngine = expressionEngine.createJxltEngine();
  }

  @Test
  public void testAllowGetProperty() {
    TestContextParameter param = new TestContextParameter()
        .setIntParam(42);

    JexlContext context = new MapContext();
    context.set("param", param);
    Object result = expressionEngine.createExpression("param.intParam")
        .evaluate(context);

    assertEquals(param.getIntParam(), result);
  }

  @Test
  public void testAllowEmptyMethod() {
    JexlContext context = new MapContext();
    context.set("param", new TestContextParameter());
    Object result = expressionEngine.createExpression("empty(param)")
        .evaluate(context);

    assertEquals(true, result);
  }

  @Test
  public void testAllowSizeMethod() {
    TestContextParameter param = new TestContextParameter()
        .setCollParam(Arrays.asList(1, 2, 3));

    JexlContext context = new MapContext();
    context.set("param", param);
    Object result = expressionEngine.createExpression("size(param)")
        .evaluate(context);

    assertEquals(param.getCollParam().size(), result);
  }

  @Test
  public void testAllowEqualityOperator() {
    TestContextParameter param1 = new TestContextParameter()
        .setIntParam(42)
        .setStrParam("42");
    TestContextParameter param2 = new TestContextParameter()
        .setIntParam(42)
        .setStrParam("42");

    JexlContext context = new MapContext();
    context.set("param1", param1);
    context.set("param2", param2);
    Object result = expressionEngine.createExpression("param1 == param2")
        .evaluate(context);

    assertEquals(true, result);
  }

  @Test
  public void testAllowInOperator() {
    TestContextParameter param = new TestContextParameter()
        .setCollParam(Arrays.asList(1, 2, 3));

    JexlContext context = new MapContext();
    context.set("param", param);
    Object result = expressionEngine.createExpression("2 =~ param")
        .evaluate(context);

    assertEquals(true, result);
  }

  @Test
  public void testAllowStartsWithOperator() {
    TestContextParameter param = new TestContextParameter()
        .setStrParam("abcdef");

    JexlContext context = new MapContext();
    context.set("param", param);
    Object result = expressionEngine.createExpression("param =^ 'abc'")
        .evaluate(context);

    assertEquals(true, result);
  }

  @Test
  public void testAllowEndsWithOperator() {
    TestContextParameter param = new TestContextParameter()
        .setStrParam("abcdef");

    JexlContext context = new MapContext();
    context.set("param", param);
    Object result = expressionEngine.createExpression("param =$ 'def'")
        .evaluate(context);

    assertEquals(true, result);
  }

  @Test
  public void testAllowPrintMethod() {
    TestContextParameter param = new TestContextParameter()
        .setCollParam(Arrays.asList(1, 2, 3));

    Writer result = new PrintableStringWriter();
    JexlContext context = new MapContext();
    context.set("param", param);
    templateEngine.createTemplate("$$ for (var i : param.collParam) { $jexl.print(i); $jexl.print('.') }")
        .evaluate(context, result);

    assertEquals("1.2.3.", result.toString());
  }

  @Test(expected = JexlException.class)
  public void testDisallowCreatingNewObject() {
    expressionEngine.createExpression("new('no.mnemonic.services.triggers.pipeline.worker.jexl.ReadOnlyUberspectTest$TestContextParameter')")
        .evaluate(new MapContext());
  }

  @Test(expected = JexlException.class)
  public void testDisallowSetProperty() {
    JexlContext context = new MapContext();
    context.set("param", new TestContextParameter());
    expressionEngine.createExpression("param.intParam = 1")
        .evaluate(context);
  }

  @Test(expected = JexlException.class)
  public void testDisallowCallingMethod() {
    TestContextParameter param = new TestContextParameter()
        .setStrParam("abc");

    JexlContext context = new MapContext();
    context.set("param", param);
    expressionEngine.createExpression("param.strParam.concat('def')")
        .evaluate(context);
  }

  public static class TestContextParameter {
    private Collection<?> collParam;
    private String strParam;
    private int intParam;

    public Collection<?> getCollParam() {
      return collParam;
    }

    public TestContextParameter setCollParam(Collection<?> collParam) {
      this.collParam = collParam;
      return this;
    }

    public String getStrParam() {
      return strParam;
    }

    public TestContextParameter setStrParam(String strParam) {
      this.strParam = strParam;
      return this;
    }

    public int getIntParam() {
      return intParam;
    }

    public TestContextParameter setIntParam(int intParam) {
      this.intParam = intParam;
      return this;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TestContextParameter that = (TestContextParameter) o;
      return intParam == that.intParam && Objects.equals(strParam, that.strParam);
    }

    @Override
    public int hashCode() {
      return Objects.hash(strParam, intParam);
    }

    public boolean isEmpty() {
      return CollectionUtils.isEmpty(collParam);
    }

    public int size() {
      return CollectionUtils.size(collParam);
    }

    public boolean contains(Object o) {
      return collParam != null && collParam.contains(o);
    }

    public boolean startsWith(String s) {
      return strParam != null && strParam.startsWith(s);
    }

    public boolean endsWith(String s) {
      return strParam != null && strParam.endsWith(s);
    }
  }
}
