package com.enofex.taikai.exports;

import static java.util.stream.Collectors.toUnmodifiableSet;

import com.enofex.taikai.exports.ArchitectureMetrics.LibraryUsage;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import java.lang.module.ModuleFinder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Finds the libraries that the packages use, each named by the first two segments of its packages,
 * such as {@code org.springframework}. Neither the project nor the language of a class count: the
 * JDK and the standard libraries of Kotlin, Groovy and Scala.
 */
final class Libraries {

  /**
   * The packages of the running JDK, so that ArchUnit need not resolve the JDK from the classpath.
   */
  private static final Set<String> JDK_PACKAGES = ModuleFinder.ofSystem().findAll().stream()
      .flatMap(module -> module.descriptor().packages().stream())
      .collect(toUnmodifiableSet());

  /**
   * The packages that only a JDK has, whichever one runs the export.
   */
  private static final List<String> JDK_ROOTS = List.of("java", "jdk", "sun");

  /**
   * The standard libraries of the languages besides Java, by the extension of their source files.
   */
  private static final Map<String, List<String>> LANGUAGE_PACKAGES = Map.of(
      "kt", List.of("kotlin", "org.jetbrains.annotations"),
      "groovy", List.of("groovy", "org.codehaus.groovy"),
      "scala", List.of("scala"));

  private Libraries() {
  }

  /**
   * Returns the libraries that the classes of every package use. The given packages are the
   * project, and so is the prefix if it has at least two segments, as a shorter one, such as
   * {@code com}, is shared with libraries.
   */
  static List<LibraryUsage> usages(Collection<JavaClass> classes,
      Map<String, String> topLevelClassOf, String prefix, Collection<String> packageNames) {
    Set<String> ownPackages = Set.copyOf(packageNames);
    boolean ownPrefix = prefix.contains(".");
    Map<String, Map<String, Set<String>>> classesOf = new TreeMap<>();

    for (JavaClass javaClass : classes) {
      List<String> languagePackages = languagePackages(javaClass);

      for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
        JavaClass target = dependency.getTargetClass().getBaseComponentType();
        String packageName = target.getPackageName();

        if (!target.isPrimitive() && !packageName.isEmpty()
            && !isOfLanguage(packageName, languagePackages)
            && !ownPackages.contains(packageName) && !(ownPrefix && isIn(packageName, prefix))) {
          classesOf.computeIfAbsent(javaClass.getPackageName(), key -> new TreeMap<>())
              .computeIfAbsent(library(packageName), key -> new TreeSet<>())
              .add(topLevelClassOf.get(javaClass.getName()));
        }
      }
    }

    List<LibraryUsage> usages = new ArrayList<>();
    classesOf.forEach((packageName, libraries) -> libraries.forEach((library, users) ->
        usages.add(new LibraryUsage(packageName, library, List.copyOf(users)))));

    return List.copyOf(usages);
  }

  /**
   * Returns the standard library of the language of a class, by its source file or for Kotlin by
   * its metadata, as some classes of Kotlin name no source file.
   */
  private static List<String> languagePackages(JavaClass javaClass) {
    String sourceFile = javaClass.getSourceCodeLocation().getSourceFileName();

    return LANGUAGE_PACKAGES.getOrDefault(javaClass.isAnnotatedWith("kotlin.Metadata")
        ? "kt"
        : sourceFile.substring(sourceFile.lastIndexOf('.') + 1), List.of());
  }

  /**
   * Returns whether a package is of the JDK or of the given standard library.
   */
  private static boolean isOfLanguage(String packageName, List<String> languagePackages) {
    return JDK_PACKAGES.contains(packageName)
        || Stream.concat(JDK_ROOTS.stream(), languagePackages.stream())
            .anyMatch(language -> isIn(packageName, language));
  }

  private static boolean isIn(String packageName, String parent) {
    return packageName.equals(parent) || packageName.startsWith(parent + ".");
  }

  private static String library(String packageName) {
    String[] segments = packageName.split("\\.", 3);

    return segments.length < 2 ? segments[0] : segments[0] + "." + segments[1];
  }
}
