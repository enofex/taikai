package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_REPOSITORY;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithRepository;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithService;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
import static com.tngtech.archunit.lang.conditions.ArchConditions.not;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;

public class RepositoriesConfigurer extends AbstractConfigurer {

  private static final String DEFAULT_REPOSITORY_NAME_MATCHING = ".+Repository";

  RepositoriesConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public RepositoriesConfigurer namesShouldEndWithRepository() {
    return namesShouldMatch(DEFAULT_REPOSITORY_NAME_MATCHING, defaultConfiguration());
  }

  public RepositoriesConfigurer namesShouldEndWithRepository(Configuration configuration) {
    return namesShouldMatch(DEFAULT_REPOSITORY_NAME_MATCHING, configuration);
  }

  public RepositoriesConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, defaultConfiguration());
  }

  public RepositoriesConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(annotatedWithRepository(true)))
        .should().haveNameMatching(regex)
        .as("Repositories should have name ending %s".formatted(regex)), configuration));
  }

  public RepositoriesConfigurer shouldBeAnnotatedWithRepository() {
    return shouldBeAnnotatedWithRepository(DEFAULT_REPOSITORY_NAME_MATCHING,
        defaultConfiguration());
  }

  public RepositoriesConfigurer shouldBeAnnotatedWithRepository(Configuration configuration) {
    return shouldBeAnnotatedWithRepository(DEFAULT_REPOSITORY_NAME_MATCHING, configuration);
  }

  public RepositoriesConfigurer shouldBeAnnotatedWithRepository(String regex) {
    return shouldBeAnnotatedWithRepository(regex, defaultConfiguration());
  }

  public RepositoriesConfigurer shouldBeAnnotatedWithRepository(String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(be(annotatedWithRepository(true)))
            .as("Repositories should be annotated with %s".formatted(ANNOTATION_REPOSITORY)),
        configuration));
  }

  public RepositoriesConfigurer shouldNotDependOnServices() {
    return shouldNotDependOnServices(defaultConfiguration());
  }

  public RepositoriesConfigurer shouldNotDependOnServices(Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that(are(annotatedWithRepository(true)))
            .should(not(dependOnClassesThat(annotatedWithService(true))))
            .as("Repositories should not depend on Services"),
        configuration));
  }

  public static final class Disableable extends RepositoriesConfigurer implements
      DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    @Override
    public RepositoriesConfigurer disable() {
      disable(RepositoriesConfigurer.class);

      return this;
    }
  }
}

