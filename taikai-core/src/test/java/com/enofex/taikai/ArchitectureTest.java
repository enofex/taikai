package com.enofex.taikai;

import static com.enofex.taikai.java.ImportPatterns.*;
import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.STATIC;

import com.enofex.taikai.checks.ClassFileVersionCheck;
import com.enofex.taikai.checks.ForbiddenFilesCheck;
import com.enofex.taikai.checks.RequiredFilesCheck;
import com.enofex.taikai.checks.ServiceRegistrationsCheck;
import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.Configurer;
import java.nio.file.Path;
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
                .classesShouldMatch(ClassCriteria.implementing(CheckContext.class), ".*CheckContext")
                .interfacesShouldNotHavePrefixI()
                .enumConstantsShouldFollowConventions()
                .constantsShouldFollowConventions()))
        .discoverChecks(checks -> checks
            .projectDirectory(Path.of(".."))
            .configure(ServiceRegistrationsCheck.class, ServiceRegistrationsCheck.directories(
                "taikai-core/src/test/resources/META-INF/services"))
            .configure(RequiredFilesCheck.class, RequiredFilesCheck.files(
                "README.md", "LICENSE", "CONTRIBUTING.md", "CODE_OF_CONDUCT.md", "SECURITY.md"))
            .configure(ForbiddenFilesCheck.class, ForbiddenFilesCheck.files(
                "taikai-core/src/main/**/*.pem", "taikai-core/src/main/**/*.key",
                "taikai-core/src/main/**/*.p12", "taikai-core/src/main/**/*.jks",
                "taikai-core/src/main/**/.env", "taikai-core/src/main/**/*.class",
                "taikai-core/src/main/**/*.jar"))
            .configure(ClassFileVersionCheck.class, ClassFileVersionCheck.javaVersion(17)))
        .build()
        .dynamicTests();


  }
}
