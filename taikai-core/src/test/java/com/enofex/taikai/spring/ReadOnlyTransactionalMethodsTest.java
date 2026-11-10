package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.lang.ArchCondition;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ReadOnlyTransactionalMethodsTest {

  @Test
  void shouldRequireSpringOnClasspath() throws Exception {
    URL[] classpathWithoutSpring = Stream.of(ReadOnlyTransactionalMethods.class,
            ArchCondition.class)
        .map(type -> type.getProtectionDomain().getCodeSource().getLocation())
        .toArray(URL[]::new);

    try (URLClassLoader classLoader = new URLClassLoader(classpathWithoutSpring,
        ClassLoader.getPlatformClassLoader())) {
      Method beReadOnly = Class.forName(ReadOnlyTransactionalMethods.class.getName(), true,
          classLoader).getDeclaredMethod("beReadOnly");
      beReadOnly.setAccessible(true);

      InvocationTargetException exception = assertThrows(InvocationTargetException.class,
          () -> beReadOnly.invoke(null));
      IllegalStateException error = assertInstanceOf(IllegalStateException.class,
          exception.getCause());

      assertEquals("methodsShouldBeReadOnly requires spring-tx and spring-context on the "
          + "classpath", error.getMessage());
      assertTrue(error.getCause().getMessage().contains("AnnotationTransactionAttributeSource"),
          error.getCause().getMessage());
    }
  }
}
