package no.mnemonic.services.triggers.pipeline.worker.jexl;

import no.mnemonic.commons.logging.Logger;
import no.mnemonic.commons.logging.Logging;
import no.mnemonic.commons.utilities.ObjectUtils;
import no.mnemonic.commons.utilities.collections.MapUtils;
import no.mnemonic.commons.utilities.collections.SetUtils;
import org.apache.commons.jexl3.JexlArithmetic;
import org.apache.commons.jexl3.JexlOperator;
import org.apache.commons.jexl3.introspection.JexlMethod;
import org.apache.commons.jexl3.introspection.JexlPropertyGet;
import org.apache.commons.jexl3.introspection.JexlPropertySet;
import org.apache.commons.jexl3.introspection.JexlUberspect;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static no.mnemonic.commons.utilities.collections.MapUtils.Pair.T;

/**
 * A {@link JexlUberspect} implementation which wraps another {@link JexlUberspect} and ensures that expression
 * evaluation is read-only by only allowing to read properties from objects. It is not allowed to set properties
 * on objects nor to instantiate new objects. Furthermore, only a handful of method invocations which are used by
 * build-in JEXL operators are allowed.
 * <p>
 * Be aware that using this class together with {@link org.apache.commons.jexl3.introspection.JexlSandbox} is untested
 * and might cause unexpected behaviour. It is recommended to not combine those classes.
 */
public class ReadOnlyUberspect implements JexlUberspect {

  private static final Logger LOGGER = Logging.getLogger(ReadOnlyUberspect.class);

  // Defines a static list of allowed methods which are used by JEXL for various build-in operators.
  private static final Collection<String> ALLOWED_OPERATOR_METHODS = SetUtils.set(
      "isEmpty",    // Allows empty() method.
      "size",       // Allows size() method.
      "contains",   // Allows =~ operator.
      "startsWith", // Allows =^ operator.
      "endsWith",   // Allows =$ operator.
      "close"       // Allows auto-closeable.
  );

  // Statically defines the allowed methods for a specific class.
  private static final Map<String, Collection<String>> ALLOWED_CLASS_METHODS = MapUtils.map(
      T("java.io.Writer", SetUtils.set("print")) // Allows print() statements inside JEXL templates.
  );

  private final JexlUberspect parent;

  public ReadOnlyUberspect(JexlUberspect parent) {
    this.parent = ObjectUtils.notNull(parent, "'parent' is required!");
  }

  @Override
  public List<PropertyResolver> getResolvers(JexlOperator op, Object obj) {
    return parent.getResolvers(op, obj);
  }

  @Override
  public void setClassLoader(ClassLoader loader) {
    parent.setClassLoader(loader);
  }

  @Override
  public int getVersion() {
    return parent.getVersion();
  }

  @Override
  public JexlMethod getConstructor(Object ctorHandle, Object... args) {
    LOGGER.info("Disallowed creation of '%s'.", ctorHandle);
    return null; // Never allow the construction of new objects.
  }

  @Override
  public JexlMethod getMethod(Object obj, String method, Object... args) {
    // Allow all methods used by JEXL for various build-in operators regardless of object/class.
    if (ALLOWED_OPERATOR_METHODS.contains(method)) {
      return parent.getMethod(obj, method, args);
    }

    String className = getClassName(obj);
    // Allow white-listed methods on specific classes.
    for (String allowedClass : ALLOWED_CLASS_METHODS.keySet()) {
      if (isAssignable(allowedClass, className) && ALLOWED_CLASS_METHODS.get(allowedClass).contains(method)) {
        return parent.getMethod(obj, method, args);
      }
    }

    LOGGER.info("Disallowed calling method '%s' on class '%s'.", method, className);
    return null;
  }

  @Override
  public JexlPropertyGet getPropertyGet(Object obj, Object identifier) {
    // Always allow getting properties because context parameters are arbitrary objects.
    return parent.getPropertyGet(obj, identifier);
  }

  @Override
  public JexlPropertyGet getPropertyGet(List<PropertyResolver> resolvers, Object obj, Object identifier) {
    // Always allow getting properties because context parameters are arbitrary objects.
    return parent.getPropertyGet(resolvers, obj, identifier);
  }

  @Override
  public JexlPropertySet getPropertySet(Object obj, Object identifier, Object arg) {
    LOGGER.info("Disallowed setting property '%s' on class '%s'.", identifier, getClassName(obj));
    return null; // Never allow setting any properties.
  }

  @Override
  public JexlPropertySet getPropertySet(List<PropertyResolver> resolvers, Object obj, Object identifier, Object arg) {
    LOGGER.info("Disallowed setting property '%s' on class '%s'.", identifier, getClassName(obj));
    return null; // Never allow setting any properties.
  }

  @Override
  public Iterator<?> getIterator(Object obj) {
    return parent.getIterator(obj);
  }

  @Override
  public JexlArithmetic.Uberspect getArithmetic(JexlArithmetic arithmetic) {
    return parent.getArithmetic(arithmetic);
  }

  private String getClassName(Object obj) {
    if (obj instanceof Class) {
      return Class.class.cast(obj).getName();
    }

    return ObjectUtils.ifNotNull(obj, o -> o.getClass().getName(), "null");
  }

  private boolean isAssignable(String base, String test) {
    try {
      return Class.forName(base).isAssignableFrom(Class.forName(test));
    } catch (ClassNotFoundException ex) {
      LOGGER.warning(ex, "Could not locate class.");
      return false;
    }
  }
}
