package com.enofex.taikai.java;

import com.enofex.taikai.core.ClassCriteria;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

/**
 * Fixtures shared by the tests of the java rules.
 */
final class JavaFixtures {

  static final String THIS_PACKAGE = JavaFixtures.class.getPackageName();
  static final DescribedPredicate<JavaClass> MARKED = ClassCriteria.annotatedWith(Marker.class);

  private JavaFixtures() {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Marker {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Required {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Entity {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface TestAnnotation {
  }

  @Marker
  static class MarkedClass {

    void run() {
    }
  }

  static class PlainClass {
  }

  interface BaseRepository {
  }

  static class UserRepository implements BaseRepository {
  }

  @Marker
  static class PrivateFinalFields {

    private final String name = "fixture";

    String name() {
      assert this.name != null;
      return this.name;
    }
  }

  @Marker
  static class PackagePrivateField {

    String name;
  }

  @Marker
  static class StaticField {

    private static int counter;
  }

  static class TypedFields {

    private final Date fixedDate = new Date();

    Date mutableDate;

    @Marker
    private String name;
  }

  static class DateUser {

    private Date createdDate;

    Date createdDate() {
      return this.createdDate;
    }
  }

  static class QueryMethods {

    @Marker
    public void findAll() {
    }

    @Marker
    void findHidden() {
    }

    @Marker
    void save() {
    }

    void findUnmarked() {
    }
  }

  static class HttpStatusException extends RuntimeException {
  }

  static class ThrowsHttpException {
    void execute() {
      throw new HttpStatusException();
    }
  }

  static class NonFinalUtilityClass {

    public static int number() {
      return 42;
    }

    private NonFinalUtilityClass() {
    }
  }
}
