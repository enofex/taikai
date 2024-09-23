package com.enofex.taikai;

import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.DisplayName;

class Usage {

  public static void main(String[] args) {
    Taikai.builder()
        .namespace("com.enofex.taikai")
        .java(java -> java
            .noUsageOf(Date.class)
            .noUsageOf(Calendar.class)
            .noUsageOf(Calendar.class, "com.enofex.taikai.java")
            .noUsageOf("java.text.SimpleDateFormat")
            .noUsageOf("java.time.LocalDate", "com.enofex.taikai.java")
            .noUsageOfSystemOutOrErr()
            .noUsageOfDeprecatedAPIs()
            .classesShouldImplementHashCodeAndEquals()
            .classesShouldResideInPackage("regex", "com.enofex.taikai")
            .classesShouldResideOutsidePackage("regex", "com.enofex.taikai")
            .classesShouldBeAnnotatedWith("regex", DisplayName.class)
            .classesShouldBeAnnotatedWith("regex", "org.junit.jupiter.api.DisplayName")
            .classesShouldNotBeAnnotatedWith("regex", DisplayName.class)
            .classesShouldNotBeAnnotatedWith("regex", "org.junit.jupiter.api.DisplayName")
            .classesAnnotatedWithShouldResideInPackage(DisplayName.class, "com.enofex.taikai")
            .classesAnnotatedWithShouldResideInPackage("org.junit.jupiter.api.DisplayName", "com.enofex.taikai")
            .classesShouldBeAssignableTo("regex", "java.time.LocalDate")
            .classesShouldBeAssignableTo("regex", LocalDate.class)
            .classesShouldImplement("regex", "java.io.Serializable")
            .classesShouldImplement("regex", Serializable.class)
            .methodsShouldNotDeclareGenericExceptions()
            .methodsShouldNotDeclareException("regex", RuntimeException.class)
            .methodsShouldNotDeclareException("regex", "java.lang.RuntimeException")
            .finalClassesShouldNotHaveProtectedMembers()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .serialVersionUIDFieldsShouldBeStaticFinalLong()
            .classesShouldResideInPackage("com.enofex.taikai..")
            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldNotImport("..shaded..")
                .shouldNotImport("..lombok..")
                .shouldNotImport("org.junit..")
                .shouldNotImport(".*ImportsConfigurer", "com.enofex.taikai.TaikaiException"))
            .naming(naming -> naming
                .classesShouldNotMatch(".*Impl")
                .methodsShouldNotMatch("foo")
                .fieldsShouldNotMatch("bar")
                .fieldsShouldMatch("com.awesome.Foo", "foo")
                .constantsShouldFollowConventions()
                .interfacesShouldNotHavePrefixI()))
        .logging(logging -> logging
            .classesShouldUseLogger("java.util.logging.Logger", ".*Service")
            .classesShouldUseLogger(Logger.class, ".*Service")
            .loggersShouldFollowConventions(Logger.class, "logger", List.of(PRIVATE, FINAL)))
        .test(test -> test
            .junit5(junit5 -> junit5
                .methodsShouldNotDeclareExceptions()
                .methodsShouldMatch("should.*")
                .methodsShouldBePackagePrivate()
                .methodsShouldBeAnnotatedWithDisplayName()
                .methodsShouldNotBeAnnotatedWithDisabled()
                .methodsShouldContainAssertionsOrVerifications()
                .classesShouldBePackagePrivate(".*Test")
                .classesShouldNotBeAnnotatedWithDisabled()))
        .spring(spring -> spring
            .noAutowiredFields()
            .boot(boot -> boot
                .springBootApplicationShouldBeIn("com.enofex.taikai"))
            .properties(properties -> properties
                .namesShouldEndWithProperties()
                .namesShouldMatch("regex")
                .shouldBeAnnotatedWithConfigurationProperties()
                .shouldBeAnnotatedWithValidated())
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
