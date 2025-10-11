package com.enofex.taikai;

import static java.util.Objects.requireNonNull;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for importing {@link JavaClasses} from a given package namespace
 * with different import options. It provides cached access to imported classes
 * to improve performance when repeatedly analyzing the same namespace.
 *
 * <p>Supported import modes are:
 * <ul>
 *   <li>{@link IMPORT#WITHOUT_TESTS} – imports production classes only</li>
 *   <li>{@link IMPORT#WITH_TESTS} – imports both production and test classes</li>
 *   <li>{@link IMPORT#ONLY_TESTS} – imports test classes only</li>
 * </ul>
 */
public final class Namespace {

  private static final Map<Key, JavaClasses> JAVA_CLASSES = new ConcurrentHashMap<>();

  public enum IMPORT {
    WITHOUT_TESTS,
    WITH_TESTS,
    ONLY_TESTS
  }

  private Namespace() {
  }

  /**
   * Imports {@link JavaClasses} from the specified namespace using the given import option.
   *
   * @param namespace the base package name to import (e.g. {@code "com.example"})
   * @param importOption the import mode defining which classes to include
   * @return the imported {@link JavaClasses}
   * @throws NullPointerException if {@code namespace} or {@code importOption} is {@code null}
   */
  public static JavaClasses from(String namespace, IMPORT importOption) {
    requireNonNull(namespace);
    requireNonNull(importOption);

    return switch (importOption) {
      case WITH_TESTS -> withTests(namespace);
      case ONLY_TESTS -> onlyTests(namespace);
      default -> withoutTests(namespace);
    };
  }

  /**
   * Imports all classes from the given namespace, excluding test and JAR classes.
   * Results are cached to avoid redundant imports.
   *
   * @param namespace the base package name to import
   * @return the imported {@link JavaClasses} excluding tests
   * @throws NullPointerException if {@code namespace} is {@code null}
   */
  public static JavaClasses withoutTests(String namespace) {
    requireNonNull(namespace);

    return JAVA_CLASSES.computeIfAbsent(
        new Key(namespace, IMPORT.WITHOUT_TESTS),
        key -> new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .withImportOption(new ImportOption.DoNotIncludeJars())
            .importPackages(namespace)
    );
  }

  /**
   * Imports all classes from the given namespace, including test classes but excluding JAR classes.
   * Results are cached to avoid redundant imports.
   *
   * @param namespace the base package name to import
   * @return the imported {@link JavaClasses} including tests
   * @throws NullPointerException if {@code namespace} is {@code null}
   */
  public static JavaClasses withTests(String namespace) {
    requireNonNull(namespace);

    return JAVA_CLASSES.computeIfAbsent(
        new Key(namespace, IMPORT.WITH_TESTS),
        key -> new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeJars())
            .importPackages(namespace));
  }

  /**
   * Imports only test classes from the given namespace, excluding JAR classes.
   * Results are cached to avoid redundant imports.
   *
   * @param namespace the base package name to import
   * @return the imported test {@link JavaClasses}
   * @throws NullPointerException if {@code namespace} is {@code null}
   */
  public static JavaClasses onlyTests(String namespace) {
    requireNonNull(namespace);

    return JAVA_CLASSES.computeIfAbsent(
        new Key(namespace, IMPORT.ONLY_TESTS),
        key -> new ClassFileImporter()
            .withImportOption(new ImportOption.OnlyIncludeTests())
            .withImportOption(new ImportOption.DoNotIncludeJars())
            .importPackages(namespace));
  }

  private record Key(String namespace, IMPORT importOption) {
  }
}
