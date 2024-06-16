package com.enofex.taikai;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

  @Test
  void shouldFulfilConstrains() {
    Taikai.builder()
        .namespace("com.enofex.taikai")
        .test(test -> test
            .junit5(junit5 -> junit5
                .classesShouldNotBeAnnotatedWithDisabled()
                .methodsShouldNotBeAnnotatedWithDisabled()
                .methodsShouldMatch("should.*")
                .methodsShouldBePackagePrivate()
                .methodsShouldNotDeclareExceptions()))
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .finalClassesShouldNotHaveProtectedMembers()
            .classesShouldImplementHashCodeAndEquals()
            .methodsShouldNotDeclareGenericExceptions()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .fieldsShouldNotBePublic()
            .noUsageOfSystemOutOrErr()
            .noUsageOf(Date.class)
            .noUsageOf(Calendar.class)
            .noUsageOf(SimpleDateFormat.class)
            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldNotImport("..shaded..")
                .shouldNotImport("..lombok..")
                .shouldNotImport("org.junit.."))
            .naming(naming -> naming
                .classesShouldNotMatch(".*Impl")
                .interfacesShouldNotHavePrefixI()
                .constantsShouldFollowConvention()))
        .build()
        .check();
  }
}
