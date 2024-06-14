package com.enofex.taikai;

import java.util.Calendar;
import java.util.Date;

class Usage {

  public static void main(String[] args) {
    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai")
        .spring(spring -> spring
            .noAutowiredFields()
            .boot(boot -> boot
                .springBootApplicationShouldBeIn("com.enofex.taikai"))
            .configurations(configuration -> configuration
                .namesShouldEndWithConfiguration()
                .namesShouldMatch("regex"))
            .controllers(controllers -> controllers
                .shouldBeAnnotatedWithRestController()
                .shouldNotDependOnOtherControllers()
                .shouldBePackagePrivate()
                .namesShouldEndWithController()
                .namesShouldMatch("regex"))
            .services(services -> services
                .shouldBeAnnotatedWithService()
                .namesShouldMatch("regex")
                .namesShouldEndWithService())
            .repositories(repositories -> repositories
                .shouldBeAnnotatedWithRepository()
                .namesShouldMatch("regex")
                .namesShouldEndWithRepository()))
        .test(test -> test
            .junit5(junit5 -> junit5
                .classesShouldNotBeAnnotatedWithDisabled()
                .methodsShouldNotBeAnnotatedWithDisabled()))
        .java(java -> java
            .noUsageOf(Date.class)
            .noUsageOf(Calendar.class)
            .noUsageOf("java.text.SimpleDateFormat")
            .noUsageOfSystemOutOrErr()
            .noUsageOfDeprecatedAPIs()
            .classesShouldImplementHashCodeAndEquals()
            .methodsShouldNotThrowGenericException()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldNotImport("..shaded..")
                .shouldNotImport("..lombok..")
                .shouldNotImport("org.junit.."))
            .naming(naming -> naming
                .classesShouldNotMatch(".*Impl")
                .methodsShouldNotMatch("foo")
                .fieldsShouldNotMatch("bar")
                .constantsShouldFollowConvention()
                .interfacesShouldNotHavePrefixI()))
        .build();

    taikai.check();
  }
}
