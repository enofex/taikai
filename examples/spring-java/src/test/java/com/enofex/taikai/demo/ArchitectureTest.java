package com.enofex.taikai.demo;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.FieldCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.checks.ClassFileVersionCheck;
import com.enofex.taikai.checks.ForbiddenFilesCheck;
import com.enofex.taikai.checks.RequiredFilesCheck;
import com.enofex.taikai.java.ImportPatterns;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class ArchitectureTest {

  private static final String NAMESPACE = "com.enofex.taikai.demo";
  private static final String TASK = NAMESPACE + ".task";

  @TestFactory
  Stream<DynamicTest> architecture() {
    return Taikai.builder()
        .namespace(NAMESPACE)
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .noUsageOfSystemOutOrErr()
            .fieldsShouldNotBePublic()
            .classesShouldResideInPackage(NAMESPACE + "..")
            .classesShouldBePackagePrivate(ClassCriteria.inPackage(TASK + ".."))
            .classesShouldBeRecords(ClassCriteria.simpleNameMatching(".*(Request|Response)"))
            .methodsShouldNotDeclareGenericExceptions()
            .methodsShouldNotExceedMaxParameters(3)
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
        .spring(spring -> spring
            .noAutowiredFields()
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
                .shouldNotDependOnServices()
                .shouldNotDependOnControllers()))
        .test(test -> test
            .junit(junit -> junit
                .classesShouldEndWithTest()
                .methodsShouldBePackagePrivate()
                .methodsShouldNotBeAnnotatedWithDisabled()))
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
