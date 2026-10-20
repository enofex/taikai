package com.enofex.taikai;

import static com.enofex.taikai.java.ImportPatterns.*;
import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.STATIC;

import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.Configurer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class ArchitectureTest {

  @DisplayName("Should fulfill constraints")
  @TestFactory
  Stream<DynamicTest> shouldFulfillConstraints() {
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
                .interfacesShouldNotHavePrefixI()
                .constantsShouldFollowConventions()))
        .build()
        .dynamicTests();
  }
}
