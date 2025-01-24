package no.mnemonic.services.triggers.pipeline.worker.jexl;

import no.mnemonic.commons.utilities.collections.CollectionUtils;
import no.mnemonic.commons.utilities.collections.MapUtils;
import org.apache.commons.jexl3.*;
import org.apache.commons.jexl3.internal.Engine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import static no.mnemonic.commons.utilities.collections.MapUtils.Pair.T;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReadOnlyUberspectTest {

  private JexlEngine expressionEngine;
  private JxltEngine templateEngine;

  @BeforeEach
  public void setUp() {
    // Use same set up as RuleEvaluationEngine.
    expressionEngine = new JexlBuilder()
        .safe(true)
        .silent(false)
        .strict(true)
        .namespaces(MapUtils.map(T("formatters", new Formatters())))
        .uberspect(new ReadOnlyUberspect(Engine.getUberspect(null, null, null)))
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
  public void testAllowGetPropertyNested() {
    NestedParameter nestedParam = new NestedParameter()
        .setStrParam("42");
    TestContextParameter param = new TestContextParameter()
        .setNestedParam(nestedParam);

    JexlContext context = new MapContext();
    context.set("param", param);
    Object result = expressionEngine.createExpression("param.nestedParam.strParam == '42'")
        .evaluate(context);

    assertEquals(true, result);
  }

  @Test
  public void testAllowGetPropertyNestedNotFailingOnNull() {
    TestContextParameter param = new TestContextParameter();

    JexlContext context = new MapContext();
    context.set("param", param);
    Object result = expressionEngine.createExpression("param.nestedParam.strParam == '42'")
        .evaluate(context);

    assertEquals(false, result);
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
  public void testAllowTimestampFormatters() {
    TestContextParameter param = new TestContextParameter()
        .setIntParam(1619004469);

    JexlContext context = new MapContext();
    context.set("param", param);
    Object iso8601 = expressionEngine.createExpression("formatters:formatAsISO8601(param.intParam * 1000)").evaluate(context);
    Object custom = expressionEngine.createExpression("formatters:formatTimestamp(param.intParam * 1000, 'dd.MM.yyyy HH:mm:ss', 'UTC-3')").evaluate(context);

    assertEquals("2021-04-21T11:27:49Z", iso8601);
    assertEquals("21.04.2021 08:27:49", custom);
  }

  @Test
  public void testAllowPrintMethod() {
    TestContextParameter param = new TestContextParameter()
        .setCollParam(Arrays.asList(1, 2, 3));

    Writer result = new StringWriter();
    JexlContext context = new MapContext();
    context.set("param", param);
    templateEngine.createTemplate("$$ for (var i : param.collParam) { $jexl.print(i); $jexl.print('.') }")
        .evaluate(context, new PrintWriter(result));

    assertEquals("1.2.3.", result.toString());
  }

  @Test
  public void testDisallowCreatingNewObject() {
    assertThrows(JexlException.class, () -> expressionEngine
        .createExpression("new('no.mnemonic.services.triggers.pipeline.worker.jexl.ReadOnlyUberspectTest$TestContextParameter')")
        .evaluate(new MapContext()));
  }

  @Test
  public void testDisallowSetProperty() {
    JexlContext context = new MapContext();
    context.set("param", new TestContextParameter());
    assertThrows(JexlException.class,
        () -> expressionEngine.createExpression("param.intParam = 1").evaluate(context));
  }

  @Test
  public void testDisallowCallingMethod() {
    TestContextParameter param = new TestContextParameter()
        .setStrParam("abc");

    JexlContext context = new MapContext();
    context.set("param", param);
    assertThrows(JexlException.class,
        () -> expressionEngine.createExpression("param.strParam.concat('def')").evaluate(context));
  }

  public static class TestContextParameter {
    private NestedParameter nestedParam;
    private Collection<?> collParam;
    private String strParam;
    private int intParam;

    public NestedParameter getNestedParam() {
      return nestedParam;
    }

    public TestContextParameter setNestedParam(NestedParameter nestedParam) {
      this.nestedParam = nestedParam;
      return this;
    }

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

  public static class NestedParameter {
    private String strParam;

    public String getStrParam() {
      return strParam;
    }

    public NestedParameter setStrParam(String strParam) {
      this.strParam = strParam;
      return this;
    }
  }
}
