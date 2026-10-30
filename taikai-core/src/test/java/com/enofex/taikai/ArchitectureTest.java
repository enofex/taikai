package com.enofex.taikai;

import static com.enofex.taikai.java.ImportPatterns.lombok;
import static com.enofex.taikai.java.ImportPatterns.shaded;
import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.STATIC;

import com.enofex.taikai.checks.ClassFileVersionCheck;
import com.enofex.taikai.checks.ForbiddenFilesCheck;
import com.enofex.taikai.checks.MavenWrapperCheck;
import com.enofex.taikai.checks.RequiredFilesCheck;
import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.Configurer;
import com.enofex.taikai.exports.spi.ExportContext;
import com.enofex.taikai.exports.spi.TaikaiExport;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class ArchitectureTest {

  @DisplayName("Should fulfill architecture constraints")
  @TestFactory
  Stream<DynamicTest> architecture() {
    return Taikai.builder()
        .namespace("com.enofex.taikai")
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .noUsageOfSystemOutOrErr()
            .noUsageOf(Date.class)
            .noUsageOf(Calendar.class)
            .noUsageOf(SimpleDateFormat.class)
            .fieldsShouldHaveModifiers(FieldCriteria.nameMatching("^[A-Z][A-Z0-9_]*$"), List.of(STATIC, FINAL))
            .classesShouldImplementHashCodeAndEquals()
            .finalClassesShouldNotHaveProtectedMembers()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .methodsShouldNotDeclareGenericExceptions()
            .fieldsShouldNotBePublic()
            .serialVersionUIDFieldsShouldBeStaticFinalLong()
            .classesShouldResideInPackage("com.enofex.taikai..")
            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldNotImport(ClassCriteria.inPackage("org.springframework.core.annotation.."))
                .shouldNotImport(ClassCriteria.inPackage("jakarta.annotation.."))
                .shouldNotImport(ClassCriteria.inPackage("javax.annotation.."))
                .shouldNotImport(ClassCriteria.inPackage("org.jetbrains.annotations.."))
                .shouldNotImport(shaded())
                .shouldNotImport(lombok()))
            .naming(naming -> naming
                .packagesShouldMatchDefault()
                .fieldsShouldNotMatch(".*(List|Set|Map)$")
                .classesShouldNotMatch(".*Impl")
                .classesShouldMatch(ClassCriteria.assignableTo(AbstractConfigurer.class), ".*Configurer")
                .classesShouldMatch(ClassCriteria.implementing(Configurer.class), ".*Configurer")
                .classesShouldMatch(ClassCriteria.implementing(TaikaiCheck.class), ".*Check")
                .classesShouldMatch(ClassCriteria.implementing(TaikaiExport.class), ".*Export")
                .classesShouldMatch(ClassCriteria.implementing(CheckContext.class), ".*CheckContext")
                .classesShouldMatch(ClassCriteria.implementing(ExportContext.class), ".*ExportContext")
                .interfacesShouldNotHavePrefixI()
                .enumConstantsShouldFollowConventions()
                .constantsShouldFollowConventions()))
        .metrics(metrics -> metrics
            .classesShouldNotExceedMaxInheritanceDepth(2)
            .packagesShouldNotExceedMaxClasses(20)
            .classesShouldNotExceedMaxDependencies(25))
        .discoverChecks(checks -> checks
            .configure(RequiredFilesCheck.class, RequiredFilesCheck.files(
                "../README.md", "../LICENSE", "../CONTRIBUTING.md", "../CODE_OF_CONDUCT.md",
                "../SECURITY.md"))
            .configure(MavenWrapperCheck.class, MavenWrapperCheck.directories(".."))
            .configure(ForbiddenFilesCheck.class, ForbiddenFilesCheck.files(
                "src/main/**/*.pem", "src/main/**/*.key", "src/main/**/*.p12", "src/main/**/*.jks",
                "src/main/**/.env", "src/main/**/*.class", "src/main/**/*.jar"))
            .configure(ClassFileVersionCheck.class, ClassFileVersionCheck.javaVersion(17)))
        .build()
        .dynamicTests();
  }
}
