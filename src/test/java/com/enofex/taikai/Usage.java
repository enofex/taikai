package com.enofex.taikai;

import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;

import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.logging.Logger;

class Usage {

  public static void main(String[] args) {
    Taikai.builder()
        .namespace("com.enofex.taikai")
        .java(java -> java
            .noUsageOf(Date.class)
            .noUsageOf(Calendar.class)
            .noUsageOf("java.text.SimpleDateFormat")
            .noUsageOfSystemOutOrErr()
            .noUsageOfDeprecatedAPIs()
            .classesShouldImplementHashCodeAndEquals()
            .methodsShouldNotDeclareGenericExceptions()
            .finalClassesShouldNotHaveProtectedMembers()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .serialVersionUIDFieldsShouldBeStaticFinalLong()
            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldNotImport("..shaded..")
                .shouldNotImport("..lombok..")
                .shouldNotImport("org.junit.."))
            .naming(naming -> naming
                .classesShouldNotMatch(".*Impl")
                .methodsShouldNotMatch("foo")
                .fieldsShouldNotMatch("bar")
                .fieldsShouldMatch("com.awesome.Foo", "foo")
                .constantsShouldFollowConventions()
                .interfacesShouldNotHavePrefixI()))
        .logging(logging -> logging
            .classesShouldUseLogger(Logger.class, ".*Service")
            .loggersShouldFollowConventions(Logger.class, "logger", EnumSet.of(PRIVATE, FINAL)))
        .test(test -> test
            .junit5(junit5 -> junit5
                .methodsShouldNotDeclareExceptions()
                .methodsShouldMatch("should.*")
                .methodsShouldBePackagePrivate()
                .methodsShouldBeAnnotatedWithDisplayName()
                .methodsShouldNotBeAnnotatedWithDisabled()
                .classesShouldBePackagePrivate(".*Test")
                .classesShouldNotBeAnnotatedWithDisabled()))
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
                .shouldNotDependOnControllers()
                .namesShouldMatch("regex")
                .namesShouldEndWithService())
            .repositories(repositories -> repositories
                .shouldNotDependOnServices()
                .shouldBeAnnotatedWithRepository()
                .namesShouldMatch("regex")
                .namesShouldEndWithRepository()))
        .build()
        .check();
  }
}
