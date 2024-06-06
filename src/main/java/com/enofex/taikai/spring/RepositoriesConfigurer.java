package com.enofex.taikai.spring;

import static com.enofex.taikai.spring.SpringPredicates.ANNOTATION_REPOSITORY;
import static com.enofex.taikai.spring.SpringPredicates.metaAnnotatedWithRepository;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.are;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;

public final class RepositoriesConfigurer extends AbstractConfigurer {

  private static final String DEFAULT_REPOSITORY_NAME_MATCHING = ".*Repository";

  RepositoriesConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public RepositoriesConfigurer namesShouldEndWithRepository() {
    return namesShouldMatch(DEFAULT_REPOSITORY_NAME_MATCHING, null);
  }

  public RepositoriesConfigurer namesShouldEndWithRepository(Configuration configuration) {
    return namesShouldMatch(DEFAULT_REPOSITORY_NAME_MATCHING, configuration);
  }

  public RepositoriesConfigurer namesShouldMatch(String regex) {
    return namesShouldMatch(regex, null);
  }

  public RepositoriesConfigurer namesShouldMatch(String regex, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that(are(metaAnnotatedWithRepository()))
        .should().haveNameMatching(regex)
        .as("Repositories should have name ending %s".formatted(regex)), configuration));
  }

  public RepositoriesConfigurer shouldBeAnnotatedWithRepository() {
    return shouldBeAnnotatedWithRepository(DEFAULT_REPOSITORY_NAME_MATCHING, null);
  }

  public RepositoriesConfigurer shouldBeAnnotatedWithRepository(Configuration configuration) {
    return shouldBeAnnotatedWithRepository(DEFAULT_REPOSITORY_NAME_MATCHING, configuration);
  }

  public RepositoriesConfigurer shouldBeAnnotatedWithRepository(String regex) {
    return shouldBeAnnotatedWithRepository(regex, null);
  }

  public RepositoriesConfigurer shouldBeAnnotatedWithRepository(String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(be(metaAnnotatedWithRepository()))
            .as("Repositories should be annotated with %s".formatted(ANNOTATION_REPOSITORY)),
        configuration));
  }
}

