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
import org.junit.jupiter.api.Test;

class ArchitectureTest {

  @Test
  void shouldFulfilConstraints() {
    Taikai.builder()
        .namespace("com.enofex.taikai")
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .noUsageOfSystemOutOrErr()
            .noUsageOf(Date.class)
            .noUsageOf(Calendar.class)
            .noUsageOf(SimpleDateFormat.class)
            .fieldsShouldHaveModifiers("^[A-Z][A-Z0-9_]*$", List.of(STATIC, FINAL))
            .classesShouldImplementHashCodeAndEquals()
            .finalClassesShouldNotHaveProtectedMembers()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .methodsShouldNotDeclareGenericExceptions()
            .fieldsShouldNotBePublic()
            .serialVersionUIDFieldsShouldBeStaticFinalLong()
            .classesShouldResideInPackage("com.enofex.taikai..")
            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldNotImport("org.springframework.core.annotation..")
                .shouldNotImport("jakarta.annotation..")
                .shouldNotImport("javax.annotation..")
                .shouldNotImport("org.jetbrains.annotations..")
                .shouldNotImport(shaded())
                .shouldNotImport(lombok()))
            .naming(naming -> naming
                .packagesShouldMatchDefault()
                .fieldsShouldNotMatch(".*(List|Set|Map)$")
                .classesShouldNotMatch(".*Impl")
                .classesAssignableToShouldMatch(AbstractConfigurer.class, ".*Configurer")
                .classesImplementingShouldMatch(Configurer.class, ".*Configurer")
                .interfacesShouldNotHavePrefixI()
                .constantsShouldFollowConventions()))
        .build()
        .checkAll();
  }
}
