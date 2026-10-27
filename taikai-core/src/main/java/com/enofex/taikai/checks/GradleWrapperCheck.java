package com.enofex.taikai.checks;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import java.nio.file.Path;
import java.util.List;

/**
 * Checks that the project has the Gradle Wrapper, so that it can be built without a local Gradle
 * installation: the scripts {@code gradlew} and {@code gradlew.bat}, and the files
 * {@code gradle/wrapper/gradle-wrapper.jar} and {@code gradle/wrapper/gradle-wrapper.properties},
 * none of them empty. The files are looked up in the
 * {@linkplain CheckContext#projectDirectory() project directory}.
 *
 * <p>The check needs no configuration. When the wrapper lies elsewhere, for example in the root of
 * a multi-project build while the project directory is the subproject under test, configure its
 * directory instead:</p>
 *
 * <pre>{@code
 * .discoverChecks(checks -> checks
 *     .configure(GradleWrapperCheck.class, GradleWrapperCheck.directory("..")))
 * }</pre>
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

  /**
   * Returns the settings that look up the Gradle Wrapper in the given directory instead of the
   * project directory.
   *
   * @param directory the path of the directory with the Gradle Wrapper, relative to the project
   *     directory
   * @return the settings
   */
  public static Settings directory(String directory) {
    return new Settings(directory);
  }

  @Override
  public String description() {
    return "Project should have the Gradle Wrapper";
  }

  @Override
  public List<String> violations(CheckContext context) {
    Path directory = context.configuration(Settings.class)
        .map(settings -> context.projectDirectory().resolve(settings.directory()).normalize())
        .orElse(context.projectDirectory());

    return ProjectFiles.missingOrEmpty(directory, WRAPPER_FILES);
  }

  /**
   * The settings of the {@link GradleWrapperCheck}.
   *
   * @param directory the path of the directory with the Gradle Wrapper, relative to the project
   *     directory
   */
  public record Settings(String directory) {
  }
}
