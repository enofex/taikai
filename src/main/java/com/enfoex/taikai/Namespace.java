package com.enfoex.taikai;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

final class Namespace {
  private Namespace() {
  }

  static JavaClasses classes(String namespace) {
    return new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .withImportOption(new ImportOption.DoNotIncludeJars())
        .importPackages(namespace);
  }

  static JavaClasses testClasses(String namespace) {
    return new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeJars())
        .importPackages(namespace);
  }
}