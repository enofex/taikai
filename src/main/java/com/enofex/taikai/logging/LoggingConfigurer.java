package com.enofex.taikai.logging;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.internal.ArchConditions.haveFieldOfType;
import static com.enofex.taikai.logging.LoggerConventions.followLoggerConventions;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.DisableableConfigurer;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.util.Collection;

public class LoggingConfigurer extends AbstractConfigurer {

  public LoggingConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public LoggingConfigurer classesShouldUseLogger(String typeName, String regex) {
    return classesShouldUseLogger(typeName, regex, defaultConfiguration());
  }

  public LoggingConfigurer classesShouldUseLogger(Class<?> clazz, String regex) {
    return classesShouldUseLogger(clazz.getName(), regex, defaultConfiguration());
  }

  public LoggingConfigurer classesShouldUseLogger(String typeName, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .that().haveNameMatching(regex)
        .should(haveFieldOfType(typeName)), configuration));
  }

  public LoggingConfigurer loggersShouldFollowConventions(String typeName, String regex,
      Collection<JavaModifier> requiredModifiers) {
    return loggersShouldFollowConventions(typeName, regex, requiredModifiers,
        defaultConfiguration());
  }

  public LoggingConfigurer loggersShouldFollowConventions(Class<?> clazz, String regex,
      Collection<JavaModifier> requiredModifiers) {
    return loggersShouldFollowConventions(clazz.getName(), regex, requiredModifiers,
        defaultConfiguration());
  }

  public LoggingConfigurer loggersShouldFollowConventions(Class<?> clazz, String regex,
      Collection<JavaModifier> requiredModifiers, Configuration configuration) {
    return loggersShouldFollowConventions(clazz.getName(), regex, requiredModifiers,
        configuration);
  }

  public LoggingConfigurer loggersShouldFollowConventions(String typeName, String regex,
      Collection<JavaModifier> requiredModifiers, Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
        .should(followLoggerConventions(typeName, regex, requiredModifiers)), configuration));
  }

  public static final class Disableable extends LoggingConfigurer implements DisableableConfigurer {

    public Disableable(ConfigurerContext configurerContext) {
      super(configurerContext);
    }

    @Override
    public LoggingConfigurer disable() {
      disable(LoggingConfigurer.class);

      return this;
    }
  }
}