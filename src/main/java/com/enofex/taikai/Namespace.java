package com.enofex.taikai;

import static java.util.Objects.requireNonNull;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

public final class Namespace {

  public enum IMPORT {
    WITHOUT_TESTS,
    WITH_TESTS,
    ONLY_TESTS
  }

  private Namespace() {
  }

  public static JavaClasses from(String namespace, IMPORT importOption) {
    requireNonNull(namespace);
    requireNonNull(importOption);

    return switch (importOption) {
      case WITH_TESTS -> withTests(namespace);
      case ONLY_TESTS -> onlyTests(namespace);
      default -> withoutTests(namespace);
    };
  }

  public static JavaClasses withoutTests(String namespace) {
    requireNonNull(namespace);

    return new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .withImportOption(new ImportOption.DoNotIncludeJars())
        .importPackages(namespace);
  }

  public static JavaClasses withTests(String namespace) {
    requireNonNull(namespace);

    return new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeJars())
        .importPackages(namespace);
  }

  public static JavaClasses onlyTests(String namespace) {
    requireNonNull(namespace);

    return new ClassFileImporter()
        .withImportOption(new ImportOption.OnlyIncludeTests())
        .withImportOption(new ImportOption.DoNotIncludeJars())
        .importPackages(namespace);
  }
}