package com.enofex.taikai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.properties.HasModifiers;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import org.junit.jupiter.api.Test;

class MethodCriteriaTest {

  private static final JavaClass ORDER_SERVICE =
      new ClassFileImporter().importClass(OrderService.class);

  @Test
  void shouldMatchEntireMethodNameWithoutDeclaringClass() {
    JavaMethod method = findAllMethod();
    assertTrue(MethodCriteria.nameMatching("find.*").test(method));
    assertFalse(MethodCriteria.nameMatching("find").test(method));
    assertFalse(MethodCriteria.nameMatching(".*OrderService.*").test(method));
  }

  @Test
  void shouldComposeWithOtherCriteriaAndArchUnitPredicates() {
    JavaMethod method = findAllMethod();
    assertTrue(MethodCriteria.nameMatching("find.*")
        .and(HasModifiers.Predicates.modifier(JavaModifier.PUBLIC)).test(method));
    assertFalse(MethodCriteria.nameMatching("find.*")
        .and(HasModifiers.Predicates.modifier(JavaModifier.PRIVATE)).test(method));
    assertTrue(MethodCriteria.nameMatching("save.*").or(MethodCriteria.nameMatching("find.*"))
        .test(method));
  }

  @Test
  void shouldMatchDirectAndMetaAnnotatedMethods() {
    for (String name : List.of("findAll", "findOne")) {
      assertTrue(MethodCriteria.annotatedWith(Query.class).test(ORDER_SERVICE.getMethod(name)));
      assertTrue(MethodCriteria.annotatedWith(Query.class.getName())
          .test(ORDER_SERVICE.getMethod(name)));
    }
    assertFalse(MethodCriteria.annotatedWith(Query.class).test(ORDER_SERVICE.getMethod("save")));
  }

  @Test
  void shouldDescribeAnnotationCriteria() {
    assertEquals("annotated with " + Query.class.getName(),
        MethodCriteria.annotatedWith(Query.class).getDescription());
  }

  @Test
  void shouldDescribeNameCriteria() {
    assertEquals("with names matching find.*",
        MethodCriteria.nameMatching("find.*").getDescription());
  }

  private static JavaMethod findAllMethod() {
    return ORDER_SERVICE.getMethod("findAll");
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Query {
  }

  @Query
  @Retention(RetentionPolicy.RUNTIME)
  @interface NamedQuery {
  }

  static class OrderService {

    @Query
    public void findAll() {
    }

    @NamedQuery
    public void findOne() {
    }

    public void save() {
    }
  }
}
