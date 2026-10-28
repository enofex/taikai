package com.enofex.taikai.checks;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import java.nio.file.Path;
import java.util.List;

/**
 * Checks that the project has the Maven Wrapper, so that it can be built without a local Maven
 * installation: the scripts {@code mvnw} and {@code mvnw.cmd}, and the file
 * {@code .mvn/wrapper/maven-wrapper.properties}, none of them empty. The files are looked up in the
 * {@linkplain CheckContext#projectDirectory() project directory}.
 *
 * <p>The check needs no configuration. When the wrapper lies elsewhere, for example in the root of
 * a multi-module build while the project directory is the module under test, configure its
 * directories instead. Every configured directory must have the wrapper:</p>
 *
 * <pre>{@code
 * .discoverChecks(checks -> checks
 *     .configure(MavenWrapperCheck.class, MavenWrapperCheck.directories("..")))
 * }</pre>
 *
 * <p>Taikai does not register its predefined checks itself, so that a new Taikai version never adds
 * checks on its own. To run this check, list its class name in the file
 * {@code src/test/resources/META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck} of the
 * project and call {@code discoverChecks()}.</p>
 */
public final class MavenWrapperCheck implements TaikaiCheck {

  private static final List<String> WRAPPER_FILES = List.of(
      "mvnw", "mvnw.cmd", ".mvn/wrapper/maven-wrapper.properties");

  /**
   * Returns the settings that look up the Maven Wrapper in the given directories instead of the
   * project directory.
   *
   * @param directories the paths of the directories with the Maven Wrapper, relative to the
   *     project directory
   * @return the settings
   */
  public static Settings directories(String... directories) {
    return new Settings(List.of(directories));
  }

  @Override
  public String description() {
    return "Project should have the Maven Wrapper";
  }

  @Override
  public List<String> violations(CheckContext context) {
    List<Path> directories = context.configuration(Settings.class)
        .map(settings -> settings.directories().stream()
            .map(directory -> context.projectDirectory().resolve(directory).normalize())
            .toList())
        .orElse(List.of(context.projectDirectory()));

    return directories.stream()
        .flatMap(directory -> ProjectFiles.missingOrEmpty(directory, WRAPPER_FILES).stream())
        .toList();
  }

  /**
   * The settings of the {@link MavenWrapperCheck}.
   *
   * @param directories the paths of the directories with the Maven Wrapper, relative to the
   *     project directory
   */
  public record Settings(List<String> directories) {

    /**
     * Creates the settings.
     *
     * @param directories the paths of the directories with the Maven Wrapper, relative to the
     *     project directory
     * @throws IllegalArgumentException if no directory is given
     */
    public Settings {
      directories = List.copyOf(directories);

      if (directories.isEmpty()) {
        throw new IllegalArgumentException("At least one directory must be given");
      }
    }
  }
}
