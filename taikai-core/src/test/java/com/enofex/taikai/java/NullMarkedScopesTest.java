package com.enofex.taikai.java;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.belongToAnyOf;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.java.nullmarked.NonNullByDefault;
import com.enofex.taikai.java.nullmarked.NullableByDefault;
import com.enofex.taikai.java.nullmarked.custom.MarkedByCustomPackage;
import com.enofex.taikai.java.nullmarked.custom.OptedOutByCustomAnnotation;
import com.enofex.taikai.java.nullmarked.marked.MarkedByPackage;
import com.enofex.taikai.java.nullmarked.marked.OptedOutClass;
import com.enofex.taikai.java.nullmarked.marked.OptedOutConstructor;
import com.enofex.taikai.java.nullmarked.marked.OptedOutMethod;
import com.enofex.taikai.java.nullmarked.optedout.OptedOutByPackage;
import com.enofex.taikai.java.nullmarked.unmarked.BothAnnotations;
import com.enofex.taikai.java.nullmarked.unmarked.MarkedClass;
import com.enofex.taikai.java.nullmarked.unmarked.MarkedCodeUnits;
import com.enofex.taikai.java.nullmarked.unmarked.NotMarked;
import com.enofex.taikai.java.nullmarked.unmarked.OptedOutNestedClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class NullMarkedScopesTest {

  private static final JavaClasses FIXTURES = new ClassFileImporter()
      .importPackages("com.enofex.taikai.java.nullmarked");
  private static final NullMarking CUSTOM_MARKING = NullMarking.annotations(
      NonNullByDefault.class.getName(), NullableByDefault.class.getName());

  private static Taikai taikai(JavaClasses classes, NullMarking nullMarking) {
    return Taikai.builder()
        .classes(classes)
        .java(java -> java.classesShouldBeNullMarked(nullMarking))
        .build();
  }

  private static JavaClasses fixture(Class<?> fixture) {
    return FIXTURES.that(belongToAnyOf(fixture));
  }

  @Nested
  class JSpecify {

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {MarkedByPackage.class, MarkedClass.class})
    void shouldNotThrowWhenNullMarked(Class<?> fixture) {
      assertDoesNotThrow(taikai(fixture(fixture), NullMarking.jspecify())::check);
    }

    @Test
    void shouldNotThrowWhenOnlyTheClassIsImported() {
      Taikai taikai = Taikai.builder()
          .classes(MarkedByPackage.class)
          .java(JavaConfigurer::classesShouldBeNullMarked)
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowForLocalClassesOfNullMarkedMethodsAndConstructors() {
      JavaClasses localClasses = fixture(MarkedCodeUnits.class)
          .that(not(equivalentTo(MarkedCodeUnits.class)));

      assertEquals(2, localClasses.size());
      assertDoesNotThrow(taikai(localClasses, NullMarking.jspecify())::check);
    }

    @Test
    void shouldThrowWhenClassIsNotNullMarked() {
      AssertionError failure = assertThrows(AssertionError.class,
          taikai(fixture(NotMarked.class), NullMarking.jspecify())::check);

      assertTrue(failure.getMessage().contains(
          ("Class %s is not null-marked, annotate it, an enclosing class or package %s with "
              + "@org.jspecify.annotations.NullMarked").formatted(
              NotMarked.class.getName(), NotMarked.class.getPackageName())));
      assertTrue(failure.getMessage().contains(NotMarked.class.getName() + "$Nested"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("optedOutScopes")
    void shouldThrowWhenScopeIsOptedOut(Class<?> fixture, String violation) {
      AssertionError failure = assertThrows(AssertionError.class,
          taikai(fixture(fixture), NullMarking.jspecify())::check);

      assertTrue(failure.getMessage().contains(
          violation + " is annotated with @org.jspecify.annotations.NullUnmarked"));
    }

    static Stream<Arguments> optedOutScopes() {
      return Stream.of(
          arguments(OptedOutClass.class, "Class %s$Nested is not null-marked, as class %s"
              .formatted(OptedOutClass.class.getName(), OptedOutClass.class.getName())),
          arguments(OptedOutMethod.class,
              "as method %s.localClass()".formatted(OptedOutMethod.class.getName())),
          arguments(OptedOutConstructor.class,
              "as constructor %s.<init>()".formatted(OptedOutConstructor.class.getName())),
          arguments(OptedOutByPackage.class,
              "as package %s".formatted(OptedOutByPackage.class.getPackageName())),
          arguments(BothAnnotations.class,
              "as class %s".formatted(BothAnnotations.class.getName())));
    }

    @Test
    void shouldThrowWhenNestedClassIsOptedOut() {
      AssertionError failure = assertThrows(AssertionError.class,
          taikai(fixture(OptedOutNestedClass.class), NullMarking.jspecify())::check);

      assertTrue(failure.getMessage().contains(OptedOutNestedClass.class.getName() + "$Nested"));
      assertFalse(failure.getMessage().contains(
          "Class %s is not".formatted(OptedOutNestedClass.class.getName())));
    }

    @Test
    void shouldNotCheckPackageInfo() {
      AssertionError failure = assertThrows(AssertionError.class,
          taikai(FIXTURES, NullMarking.jspecify())::check);

      assertFalse(failure.getMessage().contains("package-info"));
    }
  }

  @Nested
  class PackageInfoOfTheSameLocation {

    @Test
    void shouldNotUsePackageInfoOfAnotherSourceSet() {
      // com.enofex.taikai.java is null-marked by the package-info of the main classes only
      Taikai main = Taikai.builder()
          .classes(JavaConfigurer.class)
          .java(JavaConfigurer::classesShouldBeNullMarked)
          .build();
      Taikai test = Taikai.builder()
          .classes(NullMarkedScopesTest.class)
          .java(JavaConfigurer::classesShouldBeNullMarked)
          .build();

      assertDoesNotThrow(main::check);
      assertThrows(AssertionError.class, test::check);
    }

    @Test
    void shouldUsePackageInfoOfTheSameJar(@TempDir Path directory) throws IOException {
      JavaClasses classes = importFromJar(
          directory.resolve("marked.jar"), MarkedByPackage.class, true);

      assertDoesNotThrow(taikai(classes, NullMarking.jspecify())::check);
    }

    @Test
    void shouldThrowWhenJarHasNoPackageInfo(@TempDir Path directory) throws IOException {
      JavaClasses classes = importFromJar(
          directory.resolve("unmarked.jar"), MarkedByPackage.class, false);

      assertThrows(AssertionError.class, taikai(classes, NullMarking.jspecify())::check);
    }

    private static JavaClasses importFromJar(Path jar, Class<?> fixture, boolean withPackageInfo)
        throws IOException {
      String classFile = fixture.getName().replace('.', '/') + ".class";

      try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
        copy(classFile, out);

        if (withPackageInfo) {
          copy(fixture.getPackageName().replace('.', '/') + "/package-info.class", out);
        }
      }

      return new ClassFileImporter().importUrl(
          URI.create("jar:" + jar.toUri() + "!/" + classFile).toURL());
    }

    private static void copy(String resource, JarOutputStream out) throws IOException {
      try (InputStream in = requireNonNull(
          NullMarkedScopesTest.class.getClassLoader().getResourceAsStream(resource), resource)) {
        out.putNextEntry(new JarEntry(resource));
        in.transferTo(out);
        out.closeEntry();
      }
    }
  }

  @Nested
  class Custom {

    @Test
    void shouldNotThrowWhenPackageIsNullMarked() {
      assertDoesNotThrow(taikai(fixture(MarkedByCustomPackage.class), CUSTOM_MARKING)::check);
    }

    @Test
    void shouldThrowWhenClassIsOptedOut() {
      AssertionError failure = assertThrows(AssertionError.class,
          taikai(fixture(OptedOutByCustomAnnotation.class), CUSTOM_MARKING)::check);

      assertTrue(failure.getMessage().contains(
          "as class %s is annotated with @%s".formatted(
              OptedOutByCustomAnnotation.class.getName(), NullableByDefault.class.getName())));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {MarkedByPackage.class, MarkedClass.class})
    void shouldThrowWhenOnlyMarkedByJSpecify(Class<?> fixture) {
      assertThrows(AssertionError.class, taikai(fixture(fixture), CUSTOM_MARKING)::check);
    }
  }
}
