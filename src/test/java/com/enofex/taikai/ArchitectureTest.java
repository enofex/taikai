package com.enofex.taikai;

import com.enofex.taikai.Namespace.IMPORT;
import com.enofex.taikai.TaikaiRule.Configuration;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

class ArchitectureTest {

  @Test
  void shouldFulfilConstrains() {
    Taikai.builder()
        .namespace("com.enofex.taikai")
        .test(test -> test
            .junit5(junit5 -> junit5
                .classesShouldNotBeAnnotatedWithDisabled()
                .methodsShouldNotBeAnnotatedWithDisabled()))
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .classesShouldImplementHashCodeAndEquals()
            .methodsShouldNotThrowGenericException()
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
                .methodsAnnotatedWithShouldMatch(Test.class, "should.*",
                    Configuration.of(IMPORT.ONLY_TESTS))
                .methodsAnnotatedWithShouldMatch(ParameterizedTest.class, "should.*",
                    Configuration.of(IMPORT.ONLY_TESTS))
                .interfacesShouldNotHavePrefixI()
                .constantsShouldFollowConvention()))
        .build()
        .check();
  }
}