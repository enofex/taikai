package com.enofex.taikai;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;
import static com.tngtech.archunit.core.domain.JavaModifier.PUBLIC;

import com.enofex.taikai.checks.ClassFileVersionCheck;
import com.enofex.taikai.checks.ForbiddenFilesCheck;
import com.enofex.taikai.checks.MavenWrapperCheck;
import com.enofex.taikai.checks.RequiredFilesCheck;
import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.ForwardingCheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.java.JavaConfigurer;
import com.enofex.taikai.test.TestConfigurer;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;

class Usage {

  public static void main(String[] args) {
    Taikai.builder()
        .namespace("com.enofex.taikai")
        .java(java -> java

            .classesShouldBeAnnotatedWith(ClassCriteria.nameMatching("regex"), DisplayName.class)
            .classesShouldBeAnnotatedWith(ClassCriteria.nameMatching("regex"), DisplayName.class, defaultConfiguration())
            .classesShouldBeAnnotatedWith(ClassCriteria.nameMatching("regex"), "org.junit.jupiter.api.DisplayName")
            .classesShouldBeAnnotatedWith(ClassCriteria.nameMatching("regex"), "org.junit.jupiter.api.DisplayName", defaultConfiguration())

            .classesShouldBeAnnotatedWithAll(ClassCriteria.nameMatching("regex"), List.of("org.junit.jupiter.api.Disabled"))
            .classesShouldBeAnnotatedWithAll(ClassCriteria.nameMatching("regex"), List.of("org.junit.jupiter.api.Disabled"), defaultConfiguration())

            .classesShouldBeAssignableTo(ClassCriteria.nameMatching("regex"), LocalDate.class)
            .classesShouldBeAssignableTo(ClassCriteria.nameMatching("regex"), LocalDate.class, defaultConfiguration())
            .classesShouldBeAssignableTo(ClassCriteria.nameMatching("regex"), "java.time.LocalDate")
            .classesShouldBeAssignableTo(ClassCriteria.nameMatching("regex"), "java.time.LocalDate", defaultConfiguration())

            .classesShouldBeInterfaces(ClassCriteria.nameMatching("regex"))
            .classesShouldBeInterfaces(ClassCriteria.nameMatching("regex"), defaultConfiguration())

            .classesShouldBePackagePrivate(ClassCriteria.nameMatching("regex"))
            .classesShouldBePackagePrivate(ClassCriteria.nameMatching("regex"), defaultConfiguration())

            .classesShouldBeRecords(ClassCriteria.nameMatching("regex"))
            .classesShouldBeRecords(ClassCriteria.nameMatching("regex"), defaultConfiguration())

            .classesShouldHaveModifiers(ClassCriteria.nameMatching("regex"), List.of(PUBLIC))
            .classesShouldHaveModifiers(ClassCriteria.nameMatching("regex"), List.of(PUBLIC), defaultConfiguration())

            .classesShouldHaveField(ClassCriteria.nameMatching("regex"), FieldCriteria.nameMatching("regex"))
            .classesShouldHaveField(ClassCriteria.nameMatching("regex"), FieldCriteria.nameMatching("regex"), defaultConfiguration())

            .classesShouldImplement(ClassCriteria.nameMatching("regex"), Serializable.class)
            .classesShouldImplement(ClassCriteria.nameMatching("regex"), Serializable.class, defaultConfiguration())
            .classesShouldImplement(ClassCriteria.nameMatching("regex"), "java.io.Serializable")
            .classesShouldImplement(ClassCriteria.nameMatching("regex"), "java.io.Serializable", defaultConfiguration())

            .classesShouldImplementHashCodeAndEquals()
            .classesShouldImplementHashCodeAndEquals(defaultConfiguration())

            .classesShouldNotBeAnnotatedWith(ClassCriteria.nameMatching("regex"), DisplayName.class)
            .classesShouldNotBeAnnotatedWith(ClassCriteria.nameMatching("regex"), DisplayName.class, defaultConfiguration())
            .classesShouldNotBeAnnotatedWith(ClassCriteria.nameMatching("regex"), "org.junit.jupiter.api.DisplayName")
            .classesShouldNotBeAnnotatedWith(ClassCriteria.nameMatching("regex"), "org.junit.jupiter.api.DisplayName", defaultConfiguration())

            .classesShouldNotHaveModifiers(ClassCriteria.nameMatching("regex"), List.of(PUBLIC))
            .classesShouldNotHaveModifiers(ClassCriteria.nameMatching("regex"), List.of(PUBLIC), defaultConfiguration())

            .classesShouldResideInPackage("com.enofex.taikai..")
            .classesShouldResideInPackage("com.enofex.taikai..", defaultConfiguration())
            .classesShouldResideInPackage(ClassCriteria.nameMatching("regex"), "com.enofex.taikai")
            .classesShouldResideInPackage(ClassCriteria.nameMatching("regex"), "com.enofex.taikai", defaultConfiguration())

            .classesShouldResideOutsidePackage(ClassCriteria.nameMatching("regex"), "com.enofex.taikai")
            .classesShouldResideOutsidePackage(ClassCriteria.nameMatching("regex"), "com.enofex.taikai", defaultConfiguration())

            .fieldsShouldHaveModifiers(FieldCriteria.nameMatching("regex"), List.of(PRIVATE, FINAL))
            .fieldsShouldHaveModifiers(FieldCriteria.nameMatching("regex"), List.of(PRIVATE, FINAL), defaultConfiguration())

            .fieldsShouldHaveModifiersForClass(ClassCriteria.nameMatching("regex"), List.of(PRIVATE, FINAL))
            .fieldsShouldHaveModifiersForClass(ClassCriteria.nameMatching("regex"), List.of(PRIVATE, FINAL), defaultConfiguration())

            .fieldsShouldNotBeOfType(FieldCriteria.nameMatching("regex"), ClassCriteria.type(UUID.class))
            .fieldsShouldNotBeOfType(FieldCriteria.nameMatching("regex"), ClassCriteria.type(UUID.class), defaultConfiguration())

            .fieldsShouldNotBeOfTypeForClass(ClassCriteria.nameMatching("regex"), ClassCriteria.type(UUID.class))
            .fieldsShouldNotBeOfTypeForClass(ClassCriteria.nameMatching("regex"), ClassCriteria.type(UUID.class), defaultConfiguration())

            .fieldsShouldNotBePublic()
            .fieldsShouldNotBePublic(defaultConfiguration())

            .fieldsShouldNotHaveModifiers(FieldCriteria.nameMatching("regex"), List.of(PUBLIC))
            .fieldsShouldNotHaveModifiers(FieldCriteria.nameMatching("regex"), List.of(PUBLIC), defaultConfiguration())

            .fieldsShouldNotHaveModifiersForClass(ClassCriteria.nameMatching("regex"), List.of(PUBLIC))
            .fieldsShouldNotHaveModifiersForClass(ClassCriteria.nameMatching("regex"), List.of(PUBLIC), defaultConfiguration())

            .finalClassesShouldNotHaveProtectedMembers()
            .finalClassesShouldNotHaveProtectedMembers(defaultConfiguration())

            .methodsShouldBeAnnotatedWith(MethodCriteria.nameMatching("regex"), DisplayName.class)
            .methodsShouldBeAnnotatedWith(MethodCriteria.nameMatching("regex"), DisplayName.class, defaultConfiguration())
            .methodsShouldBeAnnotatedWith(MethodCriteria.nameMatching("regex"), "org.junit.jupiter.api.DisplayName")
            .methodsShouldBeAnnotatedWith(MethodCriteria.nameMatching("regex"), "org.junit.jupiter.api.DisplayName", defaultConfiguration())

            .methodsShouldBeAnnotatedWithForClass(ClassCriteria.nameMatching("regex"), DisplayName.class)
            .methodsShouldBeAnnotatedWithForClass(ClassCriteria.nameMatching("regex"), DisplayName.class, defaultConfiguration())
            .methodsShouldBeAnnotatedWithForClass(ClassCriteria.nameMatching("regex"), "org.junit.jupiter.api.DisplayName")
            .methodsShouldBeAnnotatedWithForClass(ClassCriteria.nameMatching("regex"), "org.junit.jupiter.api.DisplayName", defaultConfiguration())

            .methodsShouldBeAnnotatedWithAll(MethodCriteria.annotatedWith(DisplayName.class), List.of("org.junit.jupiter.api.Disabled"))
            .methodsShouldBeAnnotatedWithAll(MethodCriteria.annotatedWith(DisplayName.class), List.of("org.junit.jupiter.api.Disabled"), defaultConfiguration())

            .methodsShouldBeAnnotatedWithAllForClass(ClassCriteria.nameMatching("regex"), List.of("org.junit.jupiter.api.Disabled"))
            .methodsShouldBeAnnotatedWithAllForClass(ClassCriteria.nameMatching("regex"), List.of("org.junit.jupiter.api.Disabled"), defaultConfiguration())

            .methodsShouldHaveModifiers(MethodCriteria.nameMatching("regex"), List.of(PUBLIC))
            .methodsShouldHaveModifiers(MethodCriteria.nameMatching("regex"), List.of(PUBLIC), defaultConfiguration())

            .methodsShouldHaveModifiersForClass(ClassCriteria.nameMatching("regex"), List.of(PUBLIC))
            .methodsShouldHaveModifiersForClass(ClassCriteria.nameMatching("regex"), List.of(PUBLIC), defaultConfiguration())

            .methodsShouldNotBeAnnotatedWith(MethodCriteria.annotatedWith(DisplayName.class), Disabled.class)
            .methodsShouldNotBeAnnotatedWith(MethodCriteria.annotatedWith(DisplayName.class), Disabled.class, defaultConfiguration())
            .methodsShouldNotBeAnnotatedWith(MethodCriteria.annotatedWith(DisplayName.class), "org.junit.jupiter.api.Disabled")
            .methodsShouldNotBeAnnotatedWith(MethodCriteria.annotatedWith(DisplayName.class), "org.junit.jupiter.api.Disabled", defaultConfiguration())

            .methodsShouldNotBeAnnotatedWithForClass(ClassCriteria.nameMatching("regex"), Disabled.class)
            .methodsShouldNotBeAnnotatedWithForClass(ClassCriteria.nameMatching("regex"), Disabled.class, defaultConfiguration())
            .methodsShouldNotBeAnnotatedWithForClass(ClassCriteria.nameMatching("regex"), "org.junit.jupiter.api.Disabled")
            .methodsShouldNotBeAnnotatedWithForClass(ClassCriteria.nameMatching("regex"), "org.junit.jupiter.api.Disabled", defaultConfiguration())

            .methodsShouldNotDeclareException(MethodCriteria.nameMatching("regex"), RuntimeException.class)
            .methodsShouldNotDeclareException(MethodCriteria.nameMatching("regex"), RuntimeException.class, defaultConfiguration())
            .methodsShouldNotDeclareException(MethodCriteria.nameMatching("regex"), "java.lang.RuntimeException")
            .methodsShouldNotDeclareException(MethodCriteria.nameMatching("regex"), "java.lang.RuntimeException", defaultConfiguration())
            .methodsShouldNotDeclareException(MethodCriteria.nameMatching("regex"), ClassCriteria.nameMatching(".*Exception"))
            .methodsShouldNotDeclareException(MethodCriteria.nameMatching("regex"), ClassCriteria.nameMatching(".*Exception"), defaultConfiguration())

            .methodsShouldNotDeclareExceptionForClass(ClassCriteria.nameMatching("regex"), RuntimeException.class)
            .methodsShouldNotDeclareExceptionForClass(ClassCriteria.nameMatching("regex"), RuntimeException.class, defaultConfiguration())
            .methodsShouldNotDeclareExceptionForClass(ClassCriteria.nameMatching("regex"), "java.lang.RuntimeException")
            .methodsShouldNotDeclareExceptionForClass(ClassCriteria.nameMatching("regex"), "java.lang.RuntimeException", defaultConfiguration())
            .methodsShouldNotDeclareExceptionForClass(ClassCriteria.nameMatching("regex"), ClassCriteria.nameMatching(".*Exception"))
            .methodsShouldNotDeclareExceptionForClass(ClassCriteria.nameMatching("regex"), ClassCriteria.nameMatching(".*Exception"), defaultConfiguration())

            .methodsShouldNotDeclareGenericExceptions()
            .methodsShouldNotDeclareGenericExceptions(defaultConfiguration())

            .methodsShouldNotExceedMaxParameters(10)
            .methodsShouldNotExceedMaxParameters(10, defaultConfiguration())

            .methodsShouldNotHaveModifiers(MethodCriteria.nameMatching("regex"), List.of(PUBLIC))
            .methodsShouldNotHaveModifiers(MethodCriteria.nameMatching("regex"), List.of(PUBLIC), defaultConfiguration())

            .methodsShouldNotHaveModifiersForClass(ClassCriteria.nameMatching("regex"), List.of(PUBLIC))
            .methodsShouldNotHaveModifiersForClass(ClassCriteria.nameMatching("regex"), List.of(PUBLIC), defaultConfiguration())
            .methodsShouldNotReturn(MethodCriteria.nameMatching("find.*"), ClassCriteria.type(Optional.class))
            .methodsShouldNotReturn(MethodCriteria.nameMatching("find.*"), ClassCriteria.type(Optional.class), defaultConfiguration())


            .methodsShouldNotReturnForClass(ClassCriteria.nameMatching(".*Controller"), ClassCriteria.nameMatching(".*Optional"))
            .methodsShouldNotReturnForClass(ClassCriteria.nameMatching(".*Controller"), ClassCriteria.nameMatching(".*Optional"), defaultConfiguration())
            .methodsShouldReturn(MethodCriteria.annotatedWith(DisplayName.class), ClassCriteria.nameMatching(".*ResponseEntity"))
            .methodsShouldReturn(MethodCriteria.annotatedWith(DisplayName.class), ClassCriteria.nameMatching(".*ResponseEntity"), defaultConfiguration())


            .methodsShouldReturnForClass(ClassCriteria.nameMatching(".*Controller"), ClassCriteria.nameMatching(".*ResponseEntity"))
            .methodsShouldReturnForClass(ClassCriteria.nameMatching(".*Controller"), ClassCriteria.nameMatching(".*ResponseEntity"), defaultConfiguration())

            .noUsageOf(Date.class)
            .noUsageOf(Date.class, defaultConfiguration())
            .noUsageOf("java.util.Date")
            .noUsageOf("java.util.Date", defaultConfiguration())
            .noUsageOf(ClassCriteria.nameMatching(".*Date"))
            .noUsageOf(ClassCriteria.nameMatching(".*Date"), defaultConfiguration())
            .noUsageOf(ClassCriteria.nameMatching("regex"), Date.class)
            .noUsageOf(ClassCriteria.nameMatching("regex"), Date.class, defaultConfiguration())
            .noUsageOf(ClassCriteria.nameMatching("regex"), "java.util.Date")
            .noUsageOf(ClassCriteria.nameMatching("regex"), "java.util.Date", defaultConfiguration())
            .noUsageOf(ClassCriteria.nameMatching("regex"), ClassCriteria.nameMatching(".*Date"))
            .noUsageOf(ClassCriteria.nameMatching("regex"), ClassCriteria.nameMatching(".*Date"), defaultConfiguration())

            .noUsageOfDeprecatedAPIs()
            .noUsageOfDeprecatedAPIs(defaultConfiguration())

            .noUsageOfSystemOutOrErr()
            .noUsageOfSystemOutOrErr(defaultConfiguration())

            .serialVersionUIDFieldsShouldBeStaticFinalLong()
            .serialVersionUIDFieldsShouldBeStaticFinalLong(defaultConfiguration())

            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor(defaultConfiguration())

            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldHaveNoCycles(defaultConfiguration())

                .shouldImport(ClassCriteria.nameMatching(".*ImportsConfigurer"), ClassCriteria.nameMatching("com.enofex.taikai.TaikaiException"))
                .shouldImport(ClassCriteria.nameMatching(".*ImportsConfigurer"), ClassCriteria.nameMatching("com.enofex.taikai.TaikaiException"), defaultConfiguration())

                .shouldNotImport(ClassCriteria.nameMatching("com.enofex.taikai.TaikaiException"))
                .shouldNotImport(ClassCriteria.nameMatching("com.enofex.taikai.TaikaiException"), defaultConfiguration())
                .shouldNotImport(ClassCriteria.nameMatching(".*ImportsConfigurer"), ClassCriteria.nameMatching("com.enofex.taikai.TaikaiException"))
                .shouldNotImport(ClassCriteria.nameMatching(".*ImportsConfigurer"), ClassCriteria.nameMatching("com.enofex.taikai.TaikaiException"), defaultConfiguration()))

            .naming(naming -> naming
                .booleanMethodsShouldStartWith()
                .booleanMethodsShouldStartWith(defaultConfiguration())
                .booleanMethodsShouldStartWith(List.of("is", "has"))
                .booleanMethodsShouldStartWith(List.of("is", "has"), defaultConfiguration())

                .classesShouldMatch("regex")
                .classesShouldMatch("regex", defaultConfiguration())
                .classesShouldMatch(ClassCriteria.nameMatching("regex"), "regex")
                .classesShouldMatch(ClassCriteria.nameMatching("regex"), "regex", defaultConfiguration())

                .classesShouldNotMatch(".*Impl")
                .classesShouldNotMatch(".*Impl", defaultConfiguration())
                .classesShouldNotMatch(ClassCriteria.nameMatching("regex"), ".*Impl")
                .classesShouldNotMatch(ClassCriteria.nameMatching("regex"), ".*Impl", defaultConfiguration())

                .constantsShouldFollowConventions()
                .constantsShouldFollowConventions(defaultConfiguration())
                .constantsShouldFollowConventions(List.of("serialVersionUID"))
                .constantsShouldFollowConventions(List.of("serialVersionUID"), defaultConfiguration())

                .enumConstantsShouldFollowConventions()
                .enumConstantsShouldFollowConventions(defaultConfiguration())

                .fieldsShouldMatch(FieldCriteria.ofType(Serializable.class), "regex")
                .fieldsShouldMatch(FieldCriteria.ofType(Serializable.class), "regex", defaultConfiguration())
                .fieldsShouldMatch(FieldCriteria.ofType("java.io.Serializable"), "regex")
                .fieldsShouldMatch(FieldCriteria.ofType("java.io.Serializable"), "regex", defaultConfiguration())
                .fieldsShouldMatch(FieldCriteria.ofType(ClassCriteria.nameMatching(".*Logger")), "regex")
                .fieldsShouldMatch(FieldCriteria.ofType(ClassCriteria.nameMatching(".*Logger")), "regex", defaultConfiguration())

                .fieldsShouldMatchForClass(ClassCriteria.nameMatching("regex"), "regex")
                .fieldsShouldMatchForClass(ClassCriteria.nameMatching("regex"), "regex", defaultConfiguration())

                .fieldsShouldNotMatch("regex")
                .fieldsShouldNotMatch("regex", defaultConfiguration())
                .fieldsShouldNotMatch(FieldCriteria.annotatedWith(DisplayName.class), "regex")
                .fieldsShouldNotMatch(FieldCriteria.annotatedWith(DisplayName.class), "regex", defaultConfiguration())

                .fieldsShouldNotMatchForClass(ClassCriteria.nameMatching("regex"), "regex")
                .fieldsShouldNotMatchForClass(ClassCriteria.nameMatching("regex"), "regex", defaultConfiguration())

                .interfacesShouldNotHavePrefixI()
                .interfacesShouldNotHavePrefixI(defaultConfiguration())

                .methodsShouldMatch("regex")
                .methodsShouldMatch("regex", defaultConfiguration())
                .methodsShouldMatch(MethodCriteria.annotatedWith(DisplayName.class), "regex")
                .methodsShouldMatch(MethodCriteria.annotatedWith(DisplayName.class), "regex", defaultConfiguration())

                .methodsShouldMatchForClass(ClassCriteria.nameMatching("regex"), "regex")
                .methodsShouldMatchForClass(ClassCriteria.nameMatching("regex"), "regex", defaultConfiguration())

                .methodsShouldNotMatch("regex")
                .methodsShouldNotMatch("regex", defaultConfiguration())
                .methodsShouldNotMatch(MethodCriteria.annotatedWith(DisplayName.class), "regex")
                .methodsShouldNotMatch(MethodCriteria.annotatedWith(DisplayName.class), "regex", defaultConfiguration())

                .methodsShouldNotMatchForClass(ClassCriteria.nameMatching("regex"), "regex")
                .methodsShouldNotMatchForClass(ClassCriteria.nameMatching("regex"), "regex", defaultConfiguration())

                .packagesShouldMatch("regex")
                .packagesShouldMatch("regex", defaultConfiguration())

                .packagesShouldMatchDefault()
                .packagesShouldMatchDefault(defaultConfiguration())))
        .logging(logging -> logging
            .classesShouldUseLogger(ClassCriteria.nameMatching(".*Service"), Logger.class)
            .classesShouldUseLogger(ClassCriteria.nameMatching(".*Service"), Logger.class, defaultConfiguration())
            .classesShouldUseLogger(ClassCriteria.nameMatching(".*Service"), "java.util.logging.Logger")
            .classesShouldUseLogger(ClassCriteria.nameMatching(".*Service"), "java.util.logging.Logger", defaultConfiguration())

            .loggersShouldFollowConventions(Logger.class, "logger")
            .loggersShouldFollowConventions(Logger.class, "logger", defaultConfiguration())
            .loggersShouldFollowConventions("java.util.logging.Logger", "logger")
            .loggersShouldFollowConventions("java.util.logging.Logger", "logger", defaultConfiguration())
            .loggersShouldFollowConventions(Logger.class, "logger", List.of(PRIVATE, FINAL))
            .loggersShouldFollowConventions(Logger.class, "logger", List.of(PRIVATE, FINAL), defaultConfiguration())
            .loggersShouldFollowConventions("java.util.logging.Logger", "logger", List.of(PRIVATE, FINAL))
            .loggersShouldFollowConventions("java.util.logging.Logger", "logger", List.of(PRIVATE, FINAL), defaultConfiguration()))
        .quarkus(quarkus -> quarkus
            .noInjectionFields()
            .noInjectionFields(defaultConfiguration())

            .ai(ai -> ai
                .namesShouldEndWithAssistantOrService()
                .namesShouldEndWithAssistantOrService(defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration())

                .shouldBeAnnotatedWithApplicationScoped()
                .shouldBeAnnotatedWithApplicationScoped(defaultConfiguration())

                .shouldNotUseToolsAttributeInAiService()
                .shouldNotUseToolsAttributeInAiService(defaultConfiguration()))

            .panache(panache -> panache
                .namesShouldEndWithRepository()
                .namesShouldEndWithRepository(defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration())

                .shouldBeAnnotatedWithEntityWhenActiveRecordPattern()
                .shouldBeAnnotatedWithEntityWhenActiveRecordPattern(defaultConfiguration()))

            .resources(resources -> resources
                .namesShouldEndWithResource()
                .namesShouldEndWithResource(defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration())

                .shouldBeAnnotatedWithPath()
                .shouldBeAnnotatedWithPath(defaultConfiguration())
                .shouldBeAnnotatedWithPath(ClassCriteria.nameMatching("regex"))
                .shouldBeAnnotatedWithPath(ClassCriteria.nameMatching("regex"), defaultConfiguration())

                .shouldBePublic()
                .shouldBePublic(defaultConfiguration())

                .shouldNotDependOnOtherResources()
                .shouldNotDependOnOtherResources(defaultConfiguration())))
        .jpa(jpa -> jpa
            .entities(entities -> entities
                .fieldsShouldNotBeOfType(ClassCriteria.assignableTo(Date.class))
                .fieldsShouldNotBeOfType(ClassCriteria.assignableTo(Date.class), defaultConfiguration())

                .idsShouldNotBeOfType(ClassCriteria.type(UUID.class))
                .idsShouldNotBeOfType(ClassCriteria.type(UUID.class), defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration())

                .shouldBePackagePrivate()
                .shouldBePackagePrivate(defaultConfiguration())

                .shouldHaveFieldAnnotatedWithVersion()
                .shouldHaveFieldAnnotatedWithVersion(defaultConfiguration())))
        .spring(spring -> spring
            .noAutowiredFields()
            .noAutowiredFields(defaultConfiguration())

            .noInjectionIntoStaticFields()
            .noInjectionIntoStaticFields(defaultConfiguration())

            .noSelfInvocationOfProxiedMethods()
            .noSelfInvocationOfProxiedMethods(defaultConfiguration())
            .noSelfInvocationOfProxiedMethods(List.of("jakarta.transaction.Transactional"))
            .noSelfInvocationOfProxiedMethods(List.of("jakarta.transaction.Transactional"), defaultConfiguration())

            .boot(boot -> boot
                .applicationClassShouldResideInPackage()
                .applicationClassShouldResideInPackage(defaultConfiguration())
                .applicationClassShouldResideInPackage("com.enofex.taikai")
                .applicationClassShouldResideInPackage("com.enofex.taikai", defaultConfiguration()))

            .configurations(configurations -> configurations
                .namesShouldEndWithConfiguration()
                .namesShouldEndWithConfiguration(defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration()))

            .controllers(controllers -> controllers
                .methodsShouldNotReturn(ClassCriteria.nameMatching(".*Optional"))
                .methodsShouldNotReturn(ClassCriteria.nameMatching(".*Optional"), defaultConfiguration())

                .methodsShouldReturn(ClassCriteria.nameMatching(".*ResponseEntity"))
                .methodsShouldReturn(ClassCriteria.nameMatching(".*ResponseEntity"), defaultConfiguration())

                .namesShouldEndWithController()
                .namesShouldEndWithController(defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration())

                .shouldBeAnnotatedWithController()
                .shouldBeAnnotatedWithController(defaultConfiguration())
                .shouldBeAnnotatedWithController(ClassCriteria.nameMatching("regex"))
                .shouldBeAnnotatedWithController(ClassCriteria.nameMatching("regex"), defaultConfiguration())

                .shouldBeAnnotatedWithRestController()
                .shouldBeAnnotatedWithRestController(defaultConfiguration())
                .shouldBeAnnotatedWithRestController(ClassCriteria.nameMatching("regex"))
                .shouldBeAnnotatedWithRestController(ClassCriteria.nameMatching("regex"), defaultConfiguration())

                .shouldBeAnnotatedWithValidated()
                .shouldBeAnnotatedWithValidated(defaultConfiguration())
                .shouldBeAnnotatedWithValidated(ClassCriteria.nameMatching("regex"))
                .shouldBeAnnotatedWithValidated(ClassCriteria.nameMatching("regex"), defaultConfiguration())

                .shouldBePackagePrivate()
                .shouldBePackagePrivate(defaultConfiguration())

                .shouldNotBeAnnotatedWithValidated()
                .shouldNotBeAnnotatedWithValidated(defaultConfiguration())
                .shouldNotBeAnnotatedWithValidated(ClassCriteria.nameMatching("regex"))
                .shouldNotBeAnnotatedWithValidated(ClassCriteria.nameMatching("regex"), defaultConfiguration())

                .shouldNotDependOnOtherControllers()
                .shouldNotDependOnOtherControllers(defaultConfiguration())

                .shouldNotDependOnRepositories()
                .shouldNotDependOnRepositories(defaultConfiguration()))

            .properties(properties -> properties
                .namesShouldEndWithProperties()
                .namesShouldEndWithProperties(defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration())

                .shouldBeAnnotatedWithConfigurationProperties()
                .shouldBeAnnotatedWithConfigurationProperties(defaultConfiguration())
                .shouldBeAnnotatedWithConfigurationProperties(ClassCriteria.nameMatching("regex"))
                .shouldBeAnnotatedWithConfigurationProperties(ClassCriteria.nameMatching("regex"), defaultConfiguration())

                .shouldBeAnnotatedWithValidated()
                .shouldBeAnnotatedWithValidated(defaultConfiguration())

                .shouldBeRecords()
                .shouldBeRecords(defaultConfiguration()))

            .repositories(repositories -> repositories
                .namesShouldEndWithRepository()
                .namesShouldEndWithRepository(defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration())

                .shouldBeAnnotatedWithRepository()
                .shouldBeAnnotatedWithRepository(defaultConfiguration())
                .shouldBeAnnotatedWithRepository(ClassCriteria.nameMatching("regex"))
                .shouldBeAnnotatedWithRepository(ClassCriteria.nameMatching("regex"), defaultConfiguration())

                .shouldBePackagePrivate()
                .shouldBePackagePrivate(defaultConfiguration())

                .shouldNotDependOnControllers()
                .shouldNotDependOnControllers(defaultConfiguration())

                .shouldNotDependOnServices()
                .shouldNotDependOnServices(defaultConfiguration()))

            .services(services -> services
                .namesShouldEndWithService()
                .namesShouldEndWithService(defaultConfiguration())

                .namesShouldMatch("regex")
                .namesShouldMatch("regex", defaultConfiguration())

                .shouldBeAnnotatedWithService()
                .shouldBeAnnotatedWithService(defaultConfiguration())
                .shouldBeAnnotatedWithService(ClassCriteria.nameMatching("regex"))
                .shouldBeAnnotatedWithService(ClassCriteria.nameMatching("regex"), defaultConfiguration())

                .shouldNotDependOnControllers()
                .shouldNotDependOnControllers(defaultConfiguration())

                .shouldNotDependOnOtherServices()
                .shouldNotDependOnOtherServices(defaultConfiguration()))

            .transactional(transactional -> transactional
                .methodsShouldBePublic()
                .methodsShouldBePublic(defaultConfiguration())

                .shouldNotBeSelfInvoked()
                .shouldNotBeSelfInvoked(defaultConfiguration())

                .shouldNotBeUsedInControllers()
                .shouldNotBeUsedInControllers(defaultConfiguration())))
        .test(test -> test
            .junit(junit -> junit
                .classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Test"))
                .classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Test"), defaultConfiguration())

                .classesShouldEndWithTest()
                .classesShouldEndWithTest(defaultConfiguration())

                .classesShouldMatch("regex")
                .classesShouldMatch("regex", defaultConfiguration())

                .classesShouldNotBeAnnotatedWithDisabled()
                .classesShouldNotBeAnnotatedWithDisabled(defaultConfiguration())

                .methodsShouldBeAnnotatedWithDisplayName()
                .methodsShouldBeAnnotatedWithDisplayName(defaultConfiguration())

                .methodsShouldBePackagePrivate()
                .methodsShouldBePackagePrivate(defaultConfiguration())

                .methodsShouldContainAssertionsOrVerifications()
                .methodsShouldContainAssertionsOrVerifications(defaultConfiguration())

                .methodsShouldMatch("should.*")
                .methodsShouldMatch("should.*", defaultConfiguration())

                .methodsShouldNotBeAnnotatedWithDisabled()
                .methodsShouldNotBeAnnotatedWithDisabled(defaultConfiguration())

                .methodsShouldNotDeclareExceptions()
                .methodsShouldNotDeclareExceptions(defaultConfiguration())

                .nestedClassesShouldBeAnnotatedWithNested()
                .nestedClassesShouldBeAnnotatedWithNested(defaultConfiguration())

                .testsShouldBeExecutable()
                .testsShouldBeExecutable(defaultConfiguration())))
        .build()
        .check();

    Taikai.builder()
        .namespace("com.enofex.taikai")
        .java(java -> {
          DEFAULT_JAVA_PROFILE.customize(java);
          java.classesShouldHaveModifiers(ClassCriteria.nameMatching(".*"), List.of(PRIVATE, FINAL));
        })
        .test(DEFAULT_TEST_PROFILE)
        .build()
        .checkAll();

    Taikai.builder()
        .namespace("com.enofex.taikai")
        .java(JavaConfigurer::noUsageOfSystemOutOrErr)
        .build()
        .dynamicTests();

    Taikai.builder()
        .namespace("com.enofex.taikai")
        .discoverChecks()
        .build()
        .checkAll();

    Taikai.builder()
        .namespace("com.enofex.taikai")
        .discoverChecks(checks -> checks
            .projectDirectory(Path.of(".."))
            .exclude(MavenWrapperCheck.class))
        .build()
        .checkAll();

    Taikai.builder()
        .namespace("com.enofex.taikai")
        .discoverChecks(checks -> checks
            .configure(RequiredFilesCheck.class, RequiredFilesCheck.files("README.md", "LICENSE"))
            .configure(ClassFileVersionCheck.class, ClassFileVersionCheck.javaVersion(17))
            .configure(ForbiddenFilesCheck.class,
                ForbiddenFilesCheck.files("src/main/**/*.key", "**/.env")))
        .build()
        .checkAll();

    Taikai.builder()
        .namespace("com.enofex.taikai")
        .discoverChecks(checks -> checks
            .configure(LicenseCheck.class, "LICENSE")

            .context(context -> new ForwardingCheckContext(context) {
              @Override
              public ClassLoader classLoader() {
                return ClassLoader.getSystemClassLoader();
              }
            })
            .context(LicenseCheck.class, context -> new ForwardingCheckContext(context) {
              @Override
              public Path projectDirectory() {
                return super.projectDirectory().resolve("docs");
              }
            }))
        .build()
        .checkAll();
  }

  public static final class LicenseCheck implements TaikaiCheck {

    @Override
    public String description() {
      return "Project should have a LICENSE";
    }

    @Override
    public List<String> violations(CheckContext context) {
      String fileName = context.configuration(String.class).orElse("LICENSE");

      return Files.exists(context.projectDirectory().resolve(fileName))
          ? List.of()
          : List.of(fileName + " is missing in " + context.projectDirectory());
    }
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
        .classesShouldNotBeAnnotatedWithDisabled());
    // … more rules …
  };
}
