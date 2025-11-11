package com.enofex.taikai;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.java.ImportPatterns.junit4;
import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;
import static com.tngtech.archunit.core.domain.JavaModifier.PUBLIC;

import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.Configurer;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.java.JavaConfigurer;
import com.enofex.taikai.test.TestConfigurer;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;

class Usage {

  public static void main(String[] args) {
    Taikai.builder()
        .namespace("com.enofex.taikai")
        .java(java -> java
            .noUsageOf(Date.class)
            .noUsageOf(Date.class, defaultConfiguration())
            .noUsageOf("java.util.Date")
            .noUsageOf("java.util.Date", defaultConfiguration())

            .noUsageOf(Date.class, "com.enofex.taikai.java")
            .noUsageOf(Date.class, "com.enofex.taikai.java", defaultConfiguration())
            .noUsageOf("java.util.Date", "com.enofex.taikai.java")
            .noUsageOf("java.util.Date", "com.enofex.taikai.java", defaultConfiguration())

            .noUsageOfSystemOutOrErr()
            .noUsageOfSystemOutOrErr(defaultConfiguration())

            .noUsageOfDeprecatedAPIs()
            .noUsageOfDeprecatedAPIs(defaultConfiguration())

            .fieldsShouldNotBePublic()
            .fieldsShouldNotBePublic(defaultConfiguration())

            .fieldsShouldHaveModifiers("regex", List.of(PRIVATE, FINAL))
            .fieldsShouldHaveModifiers("regex", List.of(PRIVATE, FINAL), defaultConfiguration())

            .fieldsAnnotatedWithShouldHaveModifiers(DisplayName.class, List.of(PUBLIC))
            .fieldsAnnotatedWithShouldHaveModifiers(DisplayName.class, List.of(PRIVATE),
                defaultConfiguration())
            .fieldsAnnotatedWithShouldHaveModifiers("org.junit.jupiter.api.DisplayName",
                List.of(PUBLIC))
            .fieldsAnnotatedWithShouldHaveModifiers("org.junit.jupiter.api.DisplayName",
                List.of(PUBLIC), defaultConfiguration())

            .fieldsAnnotatedWithShouldNotHaveModifiers(DisplayName.class, List.of(PUBLIC))
            .fieldsAnnotatedWithShouldNotHaveModifiers(DisplayName.class, List.of(PRIVATE),
                defaultConfiguration())
            .fieldsAnnotatedWithShouldNotHaveModifiers("org.junit.jupiter.api.DisplayName",
                List.of(PUBLIC))
            .fieldsAnnotatedWithShouldNotHaveModifiers("org.junit.jupiter.api.DisplayName",
                List.of(PUBLIC), defaultConfiguration())

            .classesShouldImplementHashCodeAndEquals()
            .classesShouldImplementHashCodeAndEquals(defaultConfiguration())

            .classesShouldResideInPackage("regex", "com.enofex.taikai")
            .classesShouldResideInPackage("regex", "com.enofex.taikai", defaultConfiguration())

            .classesShouldResideOutsidePackage("regex", "com.enofex.taikai")
            .classesShouldResideOutsidePackage("regex", "com.enofex.taikai", defaultConfiguration())

            .classesShouldBeAnnotatedWith("regex", DisplayName.class)
            .classesShouldBeAnnotatedWith("regex", DisplayName.class, defaultConfiguration())
            .classesShouldBeAnnotatedWith("regex", "org.junit.jupiter.api.DisplayName")
            .classesShouldBeAnnotatedWith("regex", "org.junit.jupiter.api.DisplayName",
                defaultConfiguration())

            .classesShouldNotBeAnnotatedWith("regex", DisplayName.class)
            .classesShouldNotBeAnnotatedWith("regex", DisplayName.class, defaultConfiguration())
            .classesShouldNotBeAnnotatedWith("regex", "org.junit.jupiter.api.DisplayName")
            .classesShouldNotBeAnnotatedWith("regex", "org.junit.jupiter.api.DisplayName",
                defaultConfiguration())

            .classesAnnotatedWithShouldNotBeAnnotatedWith(DisplayName.class, Disabled.class)
            .classesAnnotatedWithShouldNotBeAnnotatedWith(DisplayName.class, Disabled.class,
                defaultConfiguration())
            .classesAnnotatedWithShouldNotBeAnnotatedWith("org.junit.jupiter.api.DisplayName",
                "org.junit.jupiter.api.Disabled")
            .classesAnnotatedWithShouldNotBeAnnotatedWith("org.junit.jupiter.api.DisplayName",
                "org.junit.jupiter.api.Disabled", defaultConfiguration())

            .classesAnnotatedWithShouldResideInPackage(DisplayName.class, "com.enofex.taikai")
            .classesAnnotatedWithShouldResideInPackage(DisplayName.class, "com.enofex.taikai",
                defaultConfiguration())
            .classesAnnotatedWithShouldResideInPackage("org.junit.jupiter.api.DisplayName",
                "com.enofex.taikai")
            .classesAnnotatedWithShouldResideInPackage("org.junit.jupiter.api.DisplayName",
                "com.enofex.taikai", defaultConfiguration())

            .classesShouldBeAssignableTo("regex", LocalDate.class)
            .classesShouldBeAssignableTo("regex", LocalDate.class, defaultConfiguration())
            .classesShouldBeAssignableTo("regex", "java.time.LocalDate")
            .classesShouldBeAssignableTo("regex", "java.time.LocalDate", defaultConfiguration())

            .classesShouldImplement("regex", Serializable.class)
            .classesShouldImplement("regex", Serializable.class, defaultConfiguration())
            .classesShouldImplement("regex", "java.io.Serializable")
            .classesShouldImplement("regex", "java.io.Serializable", defaultConfiguration())

            .classesShouldBeAnnotatedWithAll(DisplayName.class, List.of())
            .classesShouldBeAnnotatedWithAll(DisplayName.class, List.of(), defaultConfiguration())
            .classesShouldBeAnnotatedWithAll("org.junit.jupiter.api.DisplayName", List.of())
            .classesShouldBeAnnotatedWithAll("org.junit.jupiter.api.DisplayName", List.of(),
                defaultConfiguration())

            .classesShouldHaveModifiers("regex", List.of(PUBLIC))
            .classesShouldHaveModifiers("regex", List.of(PUBLIC), defaultConfiguration())

            .classesAnnotatedWithShouldHaveModifiers(DisplayName.class, List.of(PUBLIC))
            .classesAnnotatedWithShouldHaveModifiers(DisplayName.class, List.of(PRIVATE),
                defaultConfiguration())
            .classesAnnotatedWithShouldHaveModifiers("org.junit.jupiter.api.DisplayName",
                List.of(PUBLIC))
            .classesAnnotatedWithShouldHaveModifiers("org.junit.jupiter.api.DisplayName",
                List.of(PUBLIC), defaultConfiguration())

            .classesAnnotatedWithShouldBeRecords(DisplayName.class)
            .classesAnnotatedWithShouldBeRecords(DisplayName.class, defaultConfiguration())
            .classesAnnotatedWithShouldBeRecords("org.junit.jupiter.api.DisplayName")
            .classesAnnotatedWithShouldBeRecords("org.junit.jupiter.api.DisplayName", defaultConfiguration())

            .classesAnnotatedWithShouldNotHaveModifiers(DisplayName.class, List.of(PUBLIC))
            .classesAnnotatedWithShouldNotHaveModifiers(DisplayName.class, List.of(PRIVATE),
                defaultConfiguration())
            .classesAnnotatedWithShouldNotHaveModifiers("org.junit.jupiter.api.DisplayName",
                List.of(PUBLIC))
            .classesAnnotatedWithShouldNotHaveModifiers("org.junit.jupiter.api.DisplayName",
                List.of(PUBLIC), defaultConfiguration())

            .classesShouldBeRecords("regex")
            .classesShouldBeRecords("regex", defaultConfiguration())

            .methodsShouldBeAnnotatedWith("regex", DisplayName.class)
            .methodsShouldBeAnnotatedWith("regex", DisplayName.class, defaultConfiguration())
            .methodsShouldBeAnnotatedWith("regex", "org.junit.jupiter.api.DisplayName")
            .methodsShouldBeAnnotatedWith("regex", "org.junit.jupiter.api.DisplayName",
                defaultConfiguration())

            .methodsShouldNotDeclareGenericExceptions()
            .methodsShouldNotDeclareGenericExceptions(defaultConfiguration())

            .methodsShouldNotDeclareException("regex", RuntimeException.class)
            .methodsShouldNotDeclareException("regex", RuntimeException.class,
                defaultConfiguration())
            .methodsShouldNotDeclareException("regex", "java.lang.RuntimeException")
            .methodsShouldNotDeclareException("regex", "java.lang.RuntimeException",
                defaultConfiguration())

            .methodsShouldBeAnnotatedWithAll(DisplayName.class, List.of())
            .methodsShouldBeAnnotatedWithAll(DisplayName.class, List.of(), defaultConfiguration())
            .methodsShouldBeAnnotatedWithAll("org.junit.jupiter.api.DisplayName", List.of())
            .methodsShouldBeAnnotatedWithAll("org.junit.jupiter.api.DisplayName", List.of(),
                defaultConfiguration())

            .methodsAnnotatedWithShouldNotBeAnnotatedWith(DisplayName.class, Disabled.class)
            .methodsAnnotatedWithShouldNotBeAnnotatedWith(DisplayName.class, Disabled.class,
                defaultConfiguration())
            .methodsAnnotatedWithShouldNotBeAnnotatedWith("org.junit.jupiter.api.DisplayName",
                "org.junit.jupiter.api.Disabled")
            .methodsAnnotatedWithShouldNotBeAnnotatedWith("org.junit.jupiter.api.DisplayName",
                "org.junit.jupiter.api.Disabled", defaultConfiguration())

            .methodsAnnotatedWithShouldHaveModifiers(DisplayName.class, List.of(PUBLIC))
            .methodsAnnotatedWithShouldHaveModifiers(DisplayName.class, List.of(PRIVATE),
                defaultConfiguration())
            .methodsAnnotatedWithShouldHaveModifiers("org.junit.jupiter.api.DisplayName",
                List.of(PUBLIC))
            .methodsAnnotatedWithShouldHaveModifiers("org.junit.jupiter.api.DisplayName",
                List.of(PUBLIC), defaultConfiguration())

            .methodsAnnotatedWithShouldNotHaveModifiers(DisplayName.class, List.of(PUBLIC))
            .methodsAnnotatedWithShouldNotHaveModifiers(DisplayName.class, List.of(PRIVATE),
                defaultConfiguration())
            .methodsAnnotatedWithShouldNotHaveModifiers("org.junit.jupiter.api.DisplayName",
                List.of(PUBLIC))
            .methodsAnnotatedWithShouldNotHaveModifiers("org.junit.jupiter.api.DisplayName",
                List.of(PUBLIC), defaultConfiguration())

            .methodsShouldHaveModifiers("regex", List.of(PRIVATE))
            .methodsShouldHaveModifiers("regex", List.of(PRIVATE), defaultConfiguration())

            .methodsShouldNotHaveModifiers("regex", List.of(PRIVATE))
            .methodsShouldNotHaveModifiers("regex", List.of(PRIVATE), defaultConfiguration())

            .methodsShouldHaveModifiersForClass("regex", List.of(PRIVATE))
            .methodsShouldHaveModifiersForClass("regex", List.of(PRIVATE), defaultConfiguration())

            .methodsShouldNotHaveModifiersForClass("regex", List.of(PRIVATE))
            .methodsShouldNotHaveModifiersForClass("regex", List.of(PRIVATE),
                defaultConfiguration())

            .methodsShouldNotExceedMaxParameters(10)
            .methodsShouldNotExceedMaxParameters(10, defaultConfiguration())

            .finalClassesShouldNotHaveProtectedMembers()
            .finalClassesShouldNotHaveProtectedMembers(defaultConfiguration())

            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor(defaultConfiguration())

            .serialVersionUIDFieldsShouldBeStaticFinalLong()
            .serialVersionUIDFieldsShouldBeStaticFinalLong(defaultConfiguration())

            .classesShouldResideInPackage("com.enofex.taikai..")
            .classesShouldResideInPackage("com.enofex.taikai..", defaultConfiguration())

            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldHaveNoCycles(defaultConfiguration())

                .shouldImport(".*ImportsConfigurer", "com.enofex.taikai.TaikaiException")
                .shouldImport(".*ImportsConfigurer", "com.enofex.taikai.TaikaiException",
                    defaultConfiguration())

                .shouldNotImport(junit4())
                .shouldNotImport(junit4(), defaultConfiguration())

                .shouldNotImport(".*ImportsConfigurer", "com.enofex.taikai.TaikaiException")
                .shouldNotImport(".*ImportsConfigurer", "com.enofex.taikai.TaikaiException",
                    defaultConfiguration()))
            .naming(naming -> naming
                .packagesShouldMatchDefault()
                .packagesShouldMatchDefault(defaultConfiguration())

                .packagesShouldMatch("regex")
                .packagesShouldMatch("regex", defaultConfiguration())

                .classesImplementingShouldMatch(Configurer.class, ".*Configurer")
                .classesImplementingShouldMatch(Configurer.class, ".*Configurer",
                    defaultConfiguration())
                .classesImplementingShouldMatch("com.enofex.taikai.configures.Configurer",
                    ".*Configurer")
                .classesImplementingShouldMatch("com.enofex.taikai.configures.Configurer",
                    ".*Configurer", defaultConfiguration())

                .classesAssignableToShouldMatch(AbstractConfigurer.class, ".*Configurer")
                .classesAssignableToShouldMatch(AbstractConfigurer.class, ".*Configurer",
                    defaultConfiguration())
                .classesAssignableToShouldMatch("com.enofex.taikai.configures.AbstractConfigurer",
                    ".*Configurer")
                .classesAssignableToShouldMatch("com.enofex.taikai.configures.AbstractConfigurer",
                    ".*Configurer", defaultConfiguration())

                .classesShouldNotMatch(".*Impl")
                .classesShouldNotMatch(".*Impl", defaultConfiguration())

                .classesAnnotatedWithShouldMatch(DisplayName.class, "regex")
                .classesAnnotatedWithShouldMatch(DisplayName.class, "regex", defaultConfiguration())
                .classesAnnotatedWithShouldMatch("org.junit.jupiter.api.DisplayName", "regex")
                .classesAnnotatedWithShouldMatch("org.junit.jupiter.api.DisplayName", "regex",
                    defaultConfiguration())

                .methodsAnnotatedWithShouldMatch(DisplayName.class, "regex")
                .methodsAnnotatedWithShouldMatch(DisplayName.class, "regex", defaultConfiguration())
                .methodsAnnotatedWithShouldMatch("org.junit.jupiter.api.DisplayName", "regex")
                .methodsAnnotatedWithShouldMatch("org.junit.jupiter.api.DisplayName", "regex",
                    defaultConfiguration())

                .methodsShouldNotMatch("regex")
                .methodsShouldNotMatch("regex", defaultConfiguration())

                .fieldsAnnotatedWithShouldMatch(DisplayName.class, "regex")
                .fieldsAnnotatedWithShouldMatch(DisplayName.class, "regex", defaultConfiguration())
                .fieldsAnnotatedWithShouldMatch("org.junit.jupiter.api.DisplayName", "regex")
                .fieldsAnnotatedWithShouldMatch("org.junit.jupiter.api.DisplayName", "regex",
                    defaultConfiguration())

                .fieldsShouldNotMatch("regex")
                .fieldsShouldNotMatch("regex", defaultConfiguration())

                .fieldsShouldMatch(Serializable.class, "regex")
                .fieldsShouldMatch(Serializable.class, "regex", defaultConfiguration())
                .fieldsShouldMatch("java.io.Serializable", "regex")
                .fieldsShouldMatch("java.io.Serializable", "regex", defaultConfiguration())

                .constantsShouldFollowConventions()
                .constantsShouldFollowConventions(defaultConfiguration())

                .interfacesShouldNotHavePrefixI()
                .interfacesShouldNotHavePrefixI(defaultConfiguration())))
        .logging(logging -> logging
            .classesShouldUseLogger(Logger.class, ".*Service")
            .classesShouldUseLogger(Logger.class, ".*Service", defaultConfiguration())
            .classesShouldUseLogger("java.util.logging.Logger", ".*Service")
            .classesShouldUseLogger("java.util.logging.Logger", ".*Service", defaultConfiguration())

            .loggersShouldFollowConventions(Logger.class, "logger")
            .loggersShouldFollowConventions(Logger.class, "logger", defaultConfiguration())
            .loggersShouldFollowConventions("java.util.logging.Logger", "logger")
            .loggersShouldFollowConventions("java.util.logging.Logger", "logger",
                defaultConfiguration())

            .loggersShouldFollowConventions(Logger.class, "logger", List.of(PRIVATE, FINAL))
            .loggersShouldFollowConventions(Logger.class, "logger", List.of(PRIVATE, FINAL),
                defaultConfiguration())
            .loggersShouldFollowConventions("java.util.logging.Logger", "logger",
                List.of(PRIVATE, FINAL))
            .loggersShouldFollowConventions("java.util.logging.Logger", "logger",
                List.of(PRIVATE, FINAL), defaultConfiguration())

        )
        .test(test -> test
            .junit(junit -> junit
                .methodsShouldNotDeclareExceptions()
                .methodsShouldNotDeclareExceptions(defaultConfiguration())

                .methodsShouldMatch("should.*")
                .methodsShouldMatch("should.*", defaultConfiguration())

                .methodsShouldBePackagePrivate()
                .methodsShouldBePackagePrivate(defaultConfiguration())

                .methodsShouldBeAnnotatedWithDisplayName()
                .methodsShouldBeAnnotatedWithDisplayName(defaultConfiguration())

                .methodsShouldNotBeAnnotatedWithDisabled()
                .methodsShouldNotBeAnnotatedWithDisabled(defaultConfiguration())

                .methodsShouldContainAssertionsOrVerifications()
                .methodsShouldContainAssertionsOrVerifications(defaultConfiguration())

                .classesShouldBePackagePrivate(".*Test")
                .classesShouldBePackagePrivate(".*Test", defaultConfiguration())

                .classesShouldNotBeAnnotatedWithDisabled()
                .classesShouldNotBeAnnotatedWithDisabled(defaultConfiguration())))

        .spring(spring -> spring
            .noAutowiredFields()
            .noAutowiredFields(defaultConfiguration())

            .boot(boot -> boot
                .applicationClassShouldResideInPackage()
                .applicationClassShouldResideInPackage("com.enofex.taikai")
                .applicationClassShouldResideInPackage("com.enofex.taikai", defaultConfiguration()))

            .properties(properties -> properties
                .namesShouldEndWithProperties()
                .namesShouldEndWithProperties(defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration())

                .shouldBeAnnotatedWithConfigurationProperties()
                .shouldBeAnnotatedWithConfigurationProperties(defaultConfiguration())

                .shouldBeAnnotatedWithConfigurationProperties("regex")
                .shouldBeAnnotatedWithConfigurationProperties("regex", defaultConfiguration())

                .shouldBeAnnotatedWithValidated()
                .shouldBeAnnotatedWithValidated(defaultConfiguration()))

            .configurations(configuration -> configuration
                .namesShouldEndWithConfiguration()
                .namesShouldEndWithConfiguration(defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration())
                .shouldBeRecords()
                .shouldBeRecords(defaultConfiguration()))

            .controllers(controllers -> controllers
                .shouldBeAnnotatedWithController()
                .shouldBeAnnotatedWithController(defaultConfiguration())

                .shouldBeAnnotatedWithController("regex")
                .shouldBeAnnotatedWithController("regex", defaultConfiguration())

                .shouldBeAnnotatedWithRestController()
                .shouldBeAnnotatedWithRestController(defaultConfiguration())

                .shouldBeAnnotatedWithRestController("regex")
                .shouldBeAnnotatedWithRestController("regex", defaultConfiguration())

                .shouldBeAnnotatedWithValidated()
                .shouldBeAnnotatedWithValidated(defaultConfiguration())

                .shouldBeAnnotatedWithValidated("regex")
                .shouldBeAnnotatedWithValidated("regex", defaultConfiguration())

                .shouldNotDependOnOtherControllers()
                .shouldNotDependOnOtherControllers(defaultConfiguration())

                .shouldBePackagePrivate()
                .shouldBePackagePrivate(defaultConfiguration())

                .namesShouldEndWithController()
                .namesShouldEndWithController(defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration()))

            .services(services -> services
                .shouldBeAnnotatedWithService()
                .shouldBeAnnotatedWithService(defaultConfiguration())

                .shouldBeAnnotatedWithService("regex")
                .shouldBeAnnotatedWithService("regex", defaultConfiguration())

                .shouldNotDependOnControllers()
                .shouldNotDependOnControllers(defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration())

                .namesShouldEndWithService()
                .namesShouldEndWithService(defaultConfiguration()))

            .repositories(repositories -> repositories
                .shouldNotDependOnServices()
                .shouldNotDependOnServices(defaultConfiguration())

                .shouldBeAnnotatedWithRepository()
                .shouldBeAnnotatedWithRepository(defaultConfiguration())

                .shouldBeAnnotatedWithRepository("regex")
                .shouldBeAnnotatedWithRepository("regex", defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration())

                .namesShouldEndWithRepository()
                .namesShouldEndWithRepository(defaultConfiguration())))
        .build()
        .check();

    Taikai.builder()
        .namespace("com.enofex.taikai")
        .java(java -> {
          DEFAULT_JAVA_PROFILE.customize(java);
          java.classesShouldHaveModifiers(".*", List.of(PRIVATE, FINAL));
        })
        .test(DEFAULT_TEST_PROFILE)
        .build()
        .checkAll();
  }

  private static final Customizer<JavaConfigurer> DEFAULT_JAVA_PROFILE = java -> {
    java.noUsageOf(Date.class)
        .fieldsShouldNotBePublic();
    // … more rules …
  };

  private static final Customizer<TestConfigurer> DEFAULT_TEST_PROFILE = test -> {
    test.junit(junit -> junit
        .methodsShouldBePackagePrivate()
        .methodsShouldMatch("should.*")
        .methodsShouldContainAssertionsOrVerifications()
        .classesShouldBePackagePrivate(".*Test")
        .classesShouldNotBeAnnotatedWithDisabled());
    // … more rules …
  };
}
