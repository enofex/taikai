package com.enfoex.taikai;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

public final class Namespace {

  public enum IMPORT {
    WITHOUT_TESTS,
    WITH_TESTS
  }

  private Namespace() {
  }

  public static JavaClasses withoutTests(String namespace) {
    return new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .withImportOption(new ImportOption.DoNotIncludeJars())
        .importPackages(namespace);
  }

  public static JavaClasses withTests(String namespace) {
    return new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeJars())
        .importPackages(namespace);
  }
}