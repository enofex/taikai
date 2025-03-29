package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_SPRING_BOOT_APPLICATION;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithSpringBootApplication;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static java.util.Objects.requireNonNull;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;

public class BootConfigurer extends AbstractConfigurer {

  BootConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public BootConfigurer springBootApplicationShouldBeIn(String packageIdentifier) {
    requireNonNull(packageIdentifier);

    return springBootApplicationShouldBeIn(packageIdentifier, defaultConfiguration());
  }

  public BootConfigurer springBootApplicationShouldBeIn(String packageIdentifier, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithSpringBootApplication(true)))
        .should().resideInAPackage(packageIdentifier)
        .allowEmptyShould(false)
        .as("Classes annotated with %s should be located in %s".formatted(
            ANNOTATION_SPRING_BOOT_APPLICATION, packageIdentifier)), configuration));
  }

  public static final class Disableable extends BootConfigurer implements DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    @Override
    public BootConfigurer disable() {
      disable(BootConfigurer.class);

      return this;
    }
  }
}
