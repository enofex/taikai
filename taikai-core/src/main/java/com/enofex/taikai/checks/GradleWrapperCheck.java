package com.enofex.taikai.checks;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import java.util.List;

/**
 * Checks that the project has the Gradle Wrapper, so that it can be built without a local Gradle
 * installation: the scripts {@code gradlew} and {@code gradlew.bat}, and the files
 * {@code gradle/wrapper/gradle-wrapper.jar} and {@code gradle/wrapper/gradle-wrapper.properties},
 * none of them empty. The files are looked up in the
 * {@linkplain CheckContext#projectDirectory() project directory}.
 *
 * <p>Taikai does not register its predefined checks itself, so that a new Taikai version never adds
 * checks on its own. To run this check, list its class name in the file
 * {@code src/test/resources/META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck} of the
 * project and call {@code discoverChecks()}.</p>
 */
public final class GradleWrapperCheck implements TaikaiCheck {

  private static final List<String> WRAPPER_FILES = List.of(
      "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.jar",
      "gradle/wrapper/gradle-wrapper.properties");

  @Override
  public String description() {
    return "Project should have the Gradle Wrapper";
  }

  @Override
  public List<String> violations(CheckContext context) {
    return ProjectFiles.missingOrEmpty(context.projectDirectory(), WRAPPER_FILES);
  }
}
