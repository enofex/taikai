package com.enofex.taikai.java;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.properties.HasAnnotations;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

final class NullMarkedScopes {

  private static final String PACKAGE_INFO = "package-info";

  private NullMarkedScopes() {
  }

  static ArchCondition<JavaClass> beNullMarked(NullMarking nullMarking) {
    return new BeNullMarked(nullMarking);
  }

  private static final class BeNullMarked extends ArchCondition<JavaClass> {

    private final NullMarking nullMarking;
    private final Map<URI, Optional<JavaClass>> packageInfos = new ConcurrentHashMap<>();

    BeNullMarked(NullMarking nullMarking) {
      super("be null-marked by @%s".formatted(nullMarking.marked()));
      this.nullMarking = nullMarking;
    }

    @Override
    public void check(JavaClass javaClass, ConditionEvents events) {
      Optional<Scope> scope = decidingScope(javaClass);

      if (scope.isEmpty()) {
        events.add(SimpleConditionEvent.violated(javaClass,
            ("Class %s is not null-marked, annotate it, an enclosing class or package %s "
                + "with @%s").formatted(
                javaClass.getName(),
                javaClass.getPackageName(),
                this.nullMarking.marked())));
      } else if (!scope.get().marked()) {
        events.add(SimpleConditionEvent.violated(javaClass,
            "Class %s is not null-marked, as %s is annotated with @%s".formatted(
                javaClass.getName(),
                scope.get().description(),
                this.nullMarking.unmarked())));
      }
    }

    /**
     * Returns the closest scope of the class that is annotated with one of the annotations: the
     * class itself, the method or constructor that declares a local or anonymous class, the
     * enclosing classes and finally the package. A scope annotated with both annotations is opted
     * out.
     */
    private Optional<Scope> decidingScope(JavaClass javaClass) {
      JavaClass current = javaClass;

      while (true) {
        Scope scope = scope(current, "class " + current.getName());

        if (scope != null) {
          return Optional.of(scope);
        }

        Optional<JavaCodeUnit> codeUnit = current.getEnclosingCodeUnit();

        if (codeUnit.isPresent()) {
          scope = scope(codeUnit.get(), description(codeUnit.get()));

          if (scope != null) {
            return Optional.of(scope);
          }
        }

        Optional<JavaClass> enclosingClass = current.getEnclosingClass();

        if (enclosingClass.isEmpty()) {
          return packageScope(current);
        }

        current = enclosingClass.get();
      }
    }

    private Optional<Scope> packageScope(JavaClass topLevelClass) {
      JavaPackage javaPackage = topLevelClass.getPackage();
      String description = "package " + javaPackage.getName();

      if (javaPackage.tryGetPackageInfo().isPresent()) {
        return Optional.ofNullable(scope(javaPackage, description));
      }

      return packageInfo(topLevelClass)
          .map(packageInfo -> scope(packageInfo, description));
    }

    /**
     * Imports the {@code package-info} class next to the class file of the given class, if it
     * was not imported together with the class, for example with
     * {@code Taikai.builder().classes(Foo.class)}. The class file is read from the same directory
     * or JAR file as the class, so a {@code package-info} of another source set is never used.
     */
    private Optional<JavaClass> packageInfo(JavaClass topLevelClass) {
      return topLevelClass.getSource()
          .map(source -> packageInfoUri(source.getUri()))
          .flatMap(uri -> this.packageInfos.computeIfAbsent(uri,
              key -> importPackageInfo(key, topLevelClass.getPackageName())));
    }

    private @Nullable Scope scope(HasAnnotations<?> element, String description) {
      if (element.isAnnotatedWith(this.nullMarking.unmarked())) {
        return new Scope(description, false);
      }

      if (element.isAnnotatedWith(this.nullMarking.marked())) {
        return new Scope(description, true);
      }

      return null;
    }
  }

  /**
   * Replaces the file name of the class file URI. {@link URI#resolve(String)} cannot be used, as
   * it returns the relative URI unchanged for opaque URIs such as
   * {@code jar:file:/app.jar!/com/example/Foo.class}.
   */
  private static URI packageInfoUri(URI classFile) {
    String uri = classFile.toString();

    return URI.create(uri.substring(0, uri.lastIndexOf('/') + 1) + PACKAGE_INFO + ".class");
  }

  /**
   * Imports the {@code package-info} class at the given URI. ArchUnit imports no class if there
   * is no class file at the URI.
   */
  private static Optional<JavaClass> importPackageInfo(URI uri, String packageName) {
    try {
      JavaClasses classes = new ClassFileImporter().importUrl(uri.toURL());
      String name = packageName + "." + PACKAGE_INFO;

      return classes.contain(name) ? Optional.of(classes.get(name)) : Optional.empty();
    } catch (MalformedURLException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private static String description(JavaCodeUnit codeUnit) {
    return (codeUnit instanceof JavaConstructor ? "constructor " : "method ")
        + codeUnit.getFullName();
  }

  private record Scope(String description, boolean marked) {

  }
}
