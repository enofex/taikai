package com.enofex.taikai.core;

import static java.util.Objects.requireNonNull;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Imports the {@link JavaClasses} of a namespace, the classes of a package and its subpackages,
 * and caches them, so that rules analyzing the same namespace import it only once.
 */
public final class Namespace {

  private static final Map<Key, JavaClasses> JAVA_CLASSES = new ConcurrentHashMap<>();

  /**
   * Defines which classes of a namespace are imported.
   */
  public enum Import {

    /**
     * Imports production classes only.
     */
    WITHOUT_TESTS,

    /**
     * Imports production and test classes.
     */
    WITH_TESTS,

    /**
     * Imports test classes only.
     */
    ONLY_TESTS
  }

  private Namespace() {
  }

  /**
   * Imports {@link JavaClasses} from the given namespace using the given import option.
   *
   * @param namespace the base package name to import, for example {@code "com.company.project"}
   * @param importOption the import mode defining which classes to include
   * @return the imported {@link JavaClasses}
   */
  public static JavaClasses from(String namespace, Import importOption) {
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
   */
  public static JavaClasses withoutTests(String namespace) {
    requireNonNull(namespace);

    return JAVA_CLASSES.computeIfAbsent(
        new Key(namespace, Import.WITHOUT_TESTS),
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
   */
  public static JavaClasses withTests(String namespace) {
    requireNonNull(namespace);

    return JAVA_CLASSES.computeIfAbsent(
        new Key(namespace, Import.WITH_TESTS),
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
   */
  public static JavaClasses onlyTests(String namespace) {
    requireNonNull(namespace);

    return JAVA_CLASSES.computeIfAbsent(
        new Key(namespace, Import.ONLY_TESTS),
        key -> new ClassFileImporter()
            .withImportOption(new ImportOption.OnlyIncludeTests())
            .withImportOption(new ImportOption.DoNotIncludeJars())
            .importPackages(namespace));
  }

  private record Key(String namespace, Import importOption) {
  }
}
