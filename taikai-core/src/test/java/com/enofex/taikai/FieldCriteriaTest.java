package com.enofex.taikai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.properties.HasModifiers;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

class FieldCriteriaTest {

  private static final JavaClass ORDER_SERVICE =
      new ClassFileImporter().importClass(OrderService.class);

  @Test
  void shouldMatchEntireFieldNameWithoutDeclaringClass() {
    JavaField field = loggerField();
    assertTrue(FieldCriteria.nameMatching("logger").test(field));
    assertTrue(FieldCriteria.nameMatching("log.*").test(field));
    assertFalse(FieldCriteria.nameMatching("log").test(field));
    assertFalse(FieldCriteria.nameMatching(".*OrderService.*").test(field));
  }

  @Test
  void shouldComposeWithOtherCriteriaAndArchUnitPredicates() {
    JavaField field = loggerField();
    assertTrue(FieldCriteria.nameMatching("logger")
        .and(HasModifiers.Predicates.modifier(JavaModifier.PRIVATE)).test(field));
    assertFalse(FieldCriteria.nameMatching("logger")
        .and(HasModifiers.Predicates.modifier(JavaModifier.PUBLIC)).test(field));
    assertTrue(FieldCriteria.nameMatching("logger").or(FieldCriteria.nameMatching("cache"))
        .test(field));
  }

  @Test
  void shouldMatchDirectAndMetaAnnotatedFields() {
    for (String name : List.of("repository", "client")) {
      assertTrue(FieldCriteria.annotatedWith(Inject.class).test(ORDER_SERVICE.getField(name)));
      assertTrue(FieldCriteria.annotatedWith(Inject.class.getName())
          .test(ORDER_SERVICE.getField(name)));
    }
    assertFalse(FieldCriteria.annotatedWith(Inject.class).test(ORDER_SERVICE.getField("logger")));
  }

  @Test
  void shouldMatchExactRawFieldType() {
    assertTrue(FieldCriteria.ofType(List.class).test(ORDER_SERVICE.getField("names")));
    assertTrue(FieldCriteria.ofType(List.class.getName()).test(ORDER_SERVICE.getField("names")));
    assertFalse(FieldCriteria.ofType(Collection.class).test(ORDER_SERVICE.getField("names")));
    assertFalse(FieldCriteria.ofType(String.class).test(ORDER_SERVICE.getField("names")));
  }

  @Test
  void shouldMatchRawFieldTypeByClassCriteria() {
    assertTrue(FieldCriteria.ofType(ClassCriteria.inPackage("java.util"))
        .test(ORDER_SERVICE.getField("names")));
    assertTrue(FieldCriteria.ofType(ClassCriteria.simpleNameMatching("List"))
        .test(ORDER_SERVICE.getField("names")));
    assertFalse(FieldCriteria.ofType(ClassCriteria.inPackage("java.util"))
        .test(ORDER_SERVICE.getField("logger")));
  }

  @Test
  void shouldDescribeNameCriteria() {
    assertEquals("with names matching logger",
        FieldCriteria.nameMatching("logger").getDescription());
  }

  @Test
  void shouldDescribeAnnotationAndTypeCriteria() {
    assertEquals("annotated with " + Inject.class.getName(),
        FieldCriteria.annotatedWith(Inject.class).getDescription());
    assertEquals("of type java.util.List", FieldCriteria.ofType(List.class).getDescription());
    assertEquals("of types in package java.util",
        FieldCriteria.ofType(ClassCriteria.inPackage("java.util")).getDescription());
  }

  private static JavaField loggerField() {
    return ORDER_SERVICE.getField("logger");
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Inject {
  }

  @Inject
  @Retention(RetentionPolicy.RUNTIME)
  @interface InjectClient {
  }

  static class OrderService {

    private Object logger;

    @Inject
    private Object repository;

    @InjectClient
    private Object client;

    private List<String> names;
  }
}
