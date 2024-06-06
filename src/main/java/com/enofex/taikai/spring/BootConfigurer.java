package com.enofex.taikai.spring;

import static com.enofex.taikai.spring.SpringPredicates.ANNOTATION_SPRING_BOOT_APPLICATION;
import static com.enofex.taikai.spring.SpringPredicates.annotatedWithSpringBootApplication;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import java.util.Objects;

public final class BootConfigurer extends AbstractConfigurer {

  BootConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public BootConfigurer springBootApplicationShouldBeIn(String location) {
    Objects.requireNonNull(location);

    return springBootApplicationShouldBeIn(location, null);
  }

  public BootConfigurer springBootApplicationShouldBeIn(String location, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithSpringBootApplication()))
        .should().resideInAPackage(location)
        .as("Classes annotated with %s should be located in %s".formatted(
            ANNOTATION_SPRING_BOOT_APPLICATION, location)), configuration));
  }
}
