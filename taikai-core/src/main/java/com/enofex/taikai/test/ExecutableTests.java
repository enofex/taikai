package com.enofex.taikai.test;

import static com.enofex.taikai.internal.Modifiers.isMethodStatic;
import static com.enofex.taikai.internal.Modifiers.isMethodSynthetic;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_NESTED;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_TEST;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_TEST_FACTORY;
import static com.enofex.taikai.test.JUnitDescribedPredicates.ANNOTATION_TEST_TEMPLATE;

import com.tngtech.archunit.base.HasDescription;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.domain.JavaWildcardType;
import com.tngtech.archunit.core.domain.properties.HasSourceCodeLocation;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Iterator;
import java.util.List;

/**
 * Mirrors the discovery validation of JUnit Jupiter, which skips invalid test classes and methods
 * with a warning only.
 */
final class ExecutableTests {

  private static final List<String> TEST_ANNOTATIONS = List.of(
      ANNOTATION_TEST, ANNOTATION_TEST_TEMPLATE, ANNOTATION_TEST_FACTORY);

  private static final String DYNAMIC_NODE = "org.junit.jupiter.api.DynamicNode";

  private static final List<String> STREAM_CONVERTIBLE_TYPES = List.of(
      "java.util.stream.Stream",
      "java.util.stream.DoubleStream",
      "java.util.stream.IntStream",
      "java.util.stream.LongStream",
      "java.lang.Iterable",
      "java.util.Iterator");

  private ExecutableTests() {
  }

  static ArchCondition<JavaClass> beExecutable() {
    return new ArchCondition<>("be executable by JUnit") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        if (javaClass.isAnnotation()) {
          return;
        }

        checkClass(javaClass, events);

        for (JavaMethod method : javaClass.getMethods()) {
          if (!isMethodSynthetic(method)) {
            checkMethod(method, events);
          }
        }
      }
    };
  }

  private static void checkClass(JavaClass javaClass, ConditionEvents events) {
    boolean isPrivate = javaClass.getModifiers().contains(JavaModifier.PRIVATE);
    boolean isAbstract = javaClass.getModifiers().contains(JavaModifier.ABSTRACT);

    if (javaClass.isMetaAnnotatedWith(ANNOTATION_NESTED)) {
      String description = "%s annotated with @Nested".formatted(javaClass.getDescription());

      // A top-level class annotated with @Nested is still executed by JUnit
      if (javaClass.isNestedClass() && !javaClass.isInnerClass()) {
        events.add(SimpleConditionEvent.violated(javaClass,
            "%s is static and will only be executed if discovered as a standalone test class in %s".formatted(
                description, javaClass.getSourceCodeLocation())));
      }
      if (isPrivate && !isAbstract) {
        violated(javaClass, description, "is private", events);
      }
    } else if (containsTestMethods(javaClass)) {
      String description = "%s containing test methods".formatted(javaClass.getDescription());

      if (isPrivate && !isAbstract) {
        violated(javaClass, description, "is private", events);
      }
      if (javaClass.isLocalClass()) {
        violated(javaClass, description, "is a local class", events);
      }
      if (javaClass.isAnonymousClass()) {
        violated(javaClass, description, "is an anonymous class", events);
      }
    }
  }

  private static void checkMethod(JavaMethod method, ConditionEvents events) {
    for (String testAnnotation : TEST_ANNOTATIONS) {
      if (method.isMetaAnnotatedWith(testAnnotation)) {
        String description = "%s annotated with @%s".formatted(
            method.getDescription(), annotationName(method, testAnnotation));

        if (isMethodStatic(method)) {
          violated(method, description, "is static", events);
        }
        if (method.getModifiers().contains(JavaModifier.PRIVATE)) {
          violated(method, description, "is private", events);
        }
        if (testAnnotation.equals(ANNOTATION_TEST_FACTORY)) {
          if (!returnsDynamicNodes(method)) {
            violated(method, description,
                "does not return a DynamicNode or a Stream, Collection, Iterable, Iterator or array of DynamicNode",
                events);
          }
        } else if (!method.getRawReturnType().isEquivalentTo(void.class)) {
          violated(method, description, "returns a value", events);
        }

        return;
      }
    }
  }

  private static <T extends HasDescription & HasSourceCodeLocation> void violated(T item,
      String description, String reason, ConditionEvents events) {
    events.add(SimpleConditionEvent.violated(item,
        "%s %s and will not be executed by JUnit in %s".formatted(
            description, reason, item.getSourceCodeLocation())));
  }

  private static boolean containsTestMethods(JavaClass javaClass) {
    return javaClass.getAllMethods().stream()
        .filter(method -> !method.getModifiers().contains(JavaModifier.ABSTRACT))
        .anyMatch(method -> TEST_ANNOTATIONS.stream().anyMatch(method::isMetaAnnotatedWith));
  }

  /**
   * Returns the simple name of the annotation that marks the method as test, e.g.
   * {@code ParameterizedTest} for a method that is meta-annotated with {@code @TestTemplate}.
   */
  private static String annotationName(JavaMethod method, String testAnnotation) {
    return method.getAnnotations().stream()
        .map(JavaAnnotation::getRawType)
        .filter(type -> type.getName().equals(testAnnotation) || type.isMetaAnnotatedWith(testAnnotation))
        .map(JavaClass::getSimpleName)
        .findFirst()
        .orElse(testAnnotation);
  }

  /**
   * Follows the return type validation of {@code @TestFactory} methods in JUnit Jupiter. Return
   * types that cannot be validated statically, such as {@code Object} or a raw {@code Stream}, are
   * accepted like JUnit does.
   */
  private static boolean returnsDynamicNodes(JavaMethod method) {
    JavaClass returnType = method.getRawReturnType();

    if (isDynamicNode(returnType)
        || returnType.isArray() && isDynamicNode(returnType.getComponentType())) {
      return true;
    }
    if (isObject(returnType) || returnType.isArray() && isObject(returnType.getComponentType())) {
      return true;
    }
    if (returnType.isArray() || !isConvertibleToStream(returnType)) {
      return false;
    }
    if (method.getReturnType() instanceof JavaParameterizedType parameterizedType) {
      List<JavaType> typeArguments = parameterizedType.getActualTypeArguments();

      return typeArguments.size() == 1 && isDynamicNodeTypeArgument(typeArguments.get(0));
    }

    return true;
  }

  private static boolean isDynamicNodeTypeArgument(JavaType typeArgument) {
    if (typeArgument instanceof JavaClass javaClass) {
      return isDynamicNode(javaClass);
    }
    if (typeArgument instanceof JavaWildcardType wildcardType
        && wildcardType.getLowerBounds().isEmpty()) {
      List<JavaType> upperBounds = wildcardType.getUpperBounds();

      if (upperBounds.isEmpty()) {
        return true;
      }
      if (upperBounds.size() == 1 && upperBounds.get(0) instanceof JavaClass upperBound) {
        return isObject(upperBound) || isDynamicNode(upperBound);
      }
    }

    return false;
  }

  private static boolean isConvertibleToStream(JavaClass javaClass) {
    return STREAM_CONVERTIBLE_TYPES.stream().anyMatch(javaClass::isAssignableTo)
        || javaClass.getAllMethods().stream().anyMatch(method -> "iterator".equals(method.getName())
        && method.getRawParameterTypes().isEmpty()
        && method.getRawReturnType().isEquivalentTo(Iterator.class));
  }

  private static boolean isDynamicNode(JavaClass javaClass) {
    return javaClass.isAssignableTo(DYNAMIC_NODE);
  }

  private static boolean isObject(JavaClass javaClass) {
    return javaClass.isEquivalentTo(Object.class);
  }
}
