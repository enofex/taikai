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
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .noUsageOfSystemOutOrErr()
            .noUsageOf(Date.class)
            .noUsageOf(Calendar.class)
            .noUsageOf(SimpleDateFormat.class)
            .classesShouldImplementHashCodeAndEquals()
            .finalClassesShouldNotHaveProtectedMembers()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .methodsShouldNotDeclareGenericExceptions()
            .fieldsShouldNotBePublic()
            .serialVersionUIDFieldsShouldBeStaticFinalLong()
            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldNotImport("..shaded..")
                .shouldNotImport("..lombok..")
                .shouldNotImport("org.junit.."))
            .naming(naming -> naming
                .packagesShouldMatch("com.enofex.taikai..")
                .classesShouldNotMatch(".*Impl")
                .interfacesShouldNotHavePrefixI()
                .constantsShouldFollowConventions()))
        .test(test -> test
            .junit5(junit5 -> junit5
                .classesShouldNotBeAnnotatedWithDisabled()
                .classesShouldBePackagePrivate(".*Test")
                .methodsShouldNotBeAnnotatedWithDisabled()
                .methodsShouldMatch("should.*")
                .methodsShouldBePackagePrivate()
                .methodsShouldNotDeclareExceptions()))
        .build()
        .check();
  }
}
