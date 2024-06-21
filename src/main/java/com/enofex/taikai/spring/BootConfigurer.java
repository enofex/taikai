package com.enofex.taikai.spring;

import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_SPRING_BOOT_APPLICATION;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithSpringBootApplication;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static java.util.Objects.requireNonNull;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;

public final class BootConfigurer extends AbstractConfigurer {

  BootConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public BootConfigurer springBootApplicationShouldBeIn(String location) {
    requireNonNull(location);

    return springBootApplicationShouldBeIn(location, null);
  }

  public BootConfigurer springBootApplicationShouldBeIn(String location, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithSpringBootApplication(true)))
        .should().resideInAPackage(location)
        .as("Classes annotated with %s should be located in %s".formatted(
            ANNOTATION_SPRING_BOOT_APPLICATION, location)), configuration));
  }
}
