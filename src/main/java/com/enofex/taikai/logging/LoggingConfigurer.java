package com.enofex.taikai.logging;

import static com.enofex.taikai.internal.ArchConditions.haveFieldOfType;
import static com.enofex.taikai.logging.LoggerConventions.followLoggerConventions;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.util.Set;

public final class LoggingConfigurer extends AbstractConfigurer {

  public LoggingConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public LoggingConfigurer classesShouldUseLogger(String typeName, String regex) {
    return classesShouldUseLogger(typeName, regex, null);
  }

  public LoggingConfigurer classesShouldUseLogger(String typeName, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveNameMatching(regex)
        .should(haveFieldOfType(typeName)), configuration));
  }

  public LoggingConfigurer classesShouldUseLogger(Class<?> clazz, String regex) {
    return classesShouldUseLogger(clazz, regex, null);
  }

  public LoggingConfigurer classesShouldUseLogger(Class<?> clazz, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveNameMatching(regex)
        .should(haveFieldOfType(clazz.getName())), configuration));
  }

  public LoggingConfigurer loggersShouldFollowConventions(String typeName, String regex,
      Set<JavaModifier> requiredModifiers) {
    return loggersShouldFollowConventions(typeName, regex, requiredModifiers, null);
  }

  public LoggingConfigurer loggersShouldFollowConventions(String typeName, String regex,
      Set<JavaModifier> requiredModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(followLoggerConventions(typeName, regex, requiredModifiers)), configuration));
  }

  public LoggingConfigurer loggersShouldFollowConventions(Class<?> clazz, String regex,
      Set<JavaModifier> requiredModifiers) {
    return loggersShouldFollowConventions(clazz, regex, requiredModifiers, null);
  }

  public LoggingConfigurer loggersShouldFollowConventions(Class<?> clazz, String regex,
      Set<JavaModifier> requiredModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .should(followLoggerConventions(clazz.getName(), regex, requiredModifiers)),
        configuration));
  }
}