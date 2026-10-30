package com.enofex.taikai.demo;

import static com.tngtech.archunit.core.domain.JavaModifier.PUBLIC;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.FieldCriteria;
import com.enofex.taikai.MethodCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.checks.ClassFileVersionCheck;
import com.enofex.taikai.checks.ForbiddenFilesCheck;
import com.enofex.taikai.checks.RequiredFilesCheck;
import com.enofex.taikai.java.ImportPatterns;
import com.enofex.taikai.spring.TransactionalConfigurer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.web.bind.annotation.RequestMapping;

class ArchitectureTest {

  private static final String NAMESPACE = "com.enofex.taikai.demo";
  private static final String TASK = NAMESPACE + ".task";

  @DisplayName("Check architecture constraints")
  @TestFactory
  Stream<DynamicTest> architecture() {
    return Taikai.builder()
        .namespace(NAMESPACE)
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .noUsageOfSystemOutOrErr()
            .noUsageOf(Date.class)
            .noUsageOf(Calendar.class)
            .noUsageOf(SimpleDateFormat.class)
            .fieldsShouldNotBePublic()
            .classesShouldResideInPackage(NAMESPACE + "..")
            .classesShouldBePackagePrivate(ClassCriteria.inPackage(TASK + ".."))
            .classesShouldBeRecords(ClassCriteria.simpleNameMatching(".*(Request|Response)"))
            .classesShouldImplementHashCodeAndEquals()
            .finalClassesShouldNotHaveProtectedMembers()
            .methodsShouldNotHaveModifiers(MethodCriteria.annotatedWith(RequestMapping.class), List.of(PUBLIC))
            .methodsShouldNotDeclareGenericExceptions()
            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldNotImport(ImportPatterns.shaded()))
            .naming(naming -> naming
                .packagesShouldMatchDefault()
                .classesShouldNotMatch(".*Impl")
                .methodsShouldNotMatch("^(foo|bar)$")
                .methodsShouldMatch("[a-z][a-zA-Z0-9]*")
                .fieldsShouldNotMatch(".*(List|Set|Map)$")
                .fieldsShouldMatch(FieldCriteria.ofType(TASK + ".TaskService"), "service")
                .fieldsShouldMatch(FieldCriteria.ofType(TASK + ".TaskRepository"), "repository")
                // No constants, enums, or interfaces yet; these rules guard code added later.
                .constantsShouldFollowConventions()
                .enumConstantsShouldFollowConventions()
                .interfacesShouldNotHavePrefixI()))
        .metrics(metrics -> metrics
            .methodsShouldNotExceedMaxParameters(3))
        .spring(spring -> spring
            .noAutowiredFields()
            .noInjectionIntoStaticFields()
            // No proxied or transactional methods yet; these rules guard code added later.
            .noSelfInvocationOfProxiedMethods()
            .transactional(TransactionalConfigurer::shouldNotBeUsedInControllers)
            .boot(boot -> boot.applicationClassShouldResideInPackage(NAMESPACE))
            .controllers(controllers -> controllers
                .shouldBeAnnotatedWithRestController()
                .namesShouldEndWithController()
                .shouldBePackagePrivate()
                .shouldNotDependOnOtherControllers()
                .shouldNotDependOnRepositories()
                .methodsShouldReturn(ClassCriteria.simpleNameMatching(".*Response")))
            .services(services -> services
                .shouldBeAnnotatedWithService()
                .namesShouldEndWithService()
                .shouldNotDependOnControllers())
            .repositories(repositories -> repositories
                .shouldBeAnnotatedWithRepository()
                .namesShouldEndWithRepository()
                .shouldBePackagePrivate()
                .shouldNotDependOnServices()
                .shouldNotDependOnControllers()))
        .test(test -> test
            .junit(junit -> junit
                .classesShouldEndWithTest()
                .classesShouldBePackagePrivate(ClassCriteria.simpleNameMatching(".*Test"))
                .classesShouldNotBeAnnotatedWithDisabled()
                .methodsShouldBePackagePrivate()
                .methodsShouldNotBeAnnotatedWithDisabled()
                .methodsShouldContainAssertionsOrVerifications()
                .testsShouldBeExecutable()))
        // Feature packages stay independent of each other as the application grows.
        .addRule(TaikaiRule.of(slices().matching(NAMESPACE + ".(*)..")
            .should().notDependOnEachOther()))
        .discoverChecks(checks -> checks
            .configure(RequiredFilesCheck.class, RequiredFilesCheck.files("README.md", "pom.xml"))
            .configure(ForbiddenFilesCheck.class,
                ForbiddenFilesCheck.files("src/main/**/*.key", "src/main/**/.env"))
            .configure(ClassFileVersionCheck.class, ClassFileVersionCheck.javaVersion(17)))
        .build()
        .dynamicTests();
  }
}
