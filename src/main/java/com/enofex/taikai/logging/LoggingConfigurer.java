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
import java.util.List;

public class LoggingConfigurer extends AbstractConfigurer {

  LoggingConfigurer(ConfigurerContext configurerContext) {
    super(configurerContext);
  }

  public LoggingConfigurer classesShouldUseLogger(String typeName, String regex) {
    return classesShouldUseLogger(typeName, regex, defaultConfiguration());
  }

  public LoggingConfigurer classesShouldUseLogger(Class<?> clazz, String regex) {
    return classesShouldUseLogger(clazz.getName(), regex, defaultConfiguration());
  }

  public LoggingConfigurer classesShouldUseLogger(Class<?> clazz, String regex,
      Configuration configuration) {
    return classesShouldUseLogger(clazz.getName(), regex, configuration);
  }

  public LoggingConfigurer classesShouldUseLogger(String typeName, String regex,
      Configuration configuration) {
    return addRule(TaikaiRule.of(classes()
            .that().haveNameMatching(regex)
            .should(haveFieldOfType(typeName))
            .as("Classes with names matching %s should use a logger of type %s".formatted(regex,
                typeName)),
        configuration));
  }

  public LoggingConfigurer loggersShouldFollowConventions(String typeName, String regex) {
    return loggersShouldFollowConventions(typeName, regex, List.of(),
        defaultConfiguration());
  }

  public LoggingConfigurer loggersShouldFollowConventions(String typeName, String regex,
      Configuration configuration) {
    return loggersShouldFollowConventions(typeName, regex, List.of(),
        configuration);
  }

  public LoggingConfigurer loggersShouldFollowConventions(Class<?> clazz, String regex) {
    return loggersShouldFollowConventions(clazz.getName(), regex, List.of(),
        defaultConfiguration());
  }

  public LoggingConfigurer loggersShouldFollowConventions(Class<?> clazz, String regex,
      Configuration configuration) {
    return loggersShouldFollowConventions(clazz.getName(), regex, List.of(),
        configuration);
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
            .should(followLoggerConventions(typeName, regex, requiredModifiers))
            .as("Loggers in classes matching %s should follow conventions and be of type %s with required modifiers %s".formatted(
                regex, typeName, requiredModifiers)),
        configuration));
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