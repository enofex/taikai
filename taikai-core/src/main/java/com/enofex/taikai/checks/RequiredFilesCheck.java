package com.enofex.taikai.checks;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import java.util.List;

/**
 * Checks that the project has the required files, and that none of them is empty. The files are
 * looked up in the {@linkplain CheckContext#projectDirectory() project directory}. The check needs
 * a configuration:
 *
 * <pre>{@code
 * .discoverChecks(checks -> checks
 *     .configure(RequiredFilesCheck.class,
 *         RequiredFilesCheck.files("README.md", "LICENSE", ".github/CODEOWNERS")))
 * }</pre>
 *
 * <p>A file outside of the project directory is given with {@code ..}, for example
 * {@code ../README.md} in the root of a multi-module build while the project directory is the
 * module under test.</p>
 *
 * <p>Taikai does not register its predefined checks itself, so that a new Taikai version never adds
 * checks on its own. To run this check, list its class name in the file
 * {@code src/test/resources/META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck} of the
 * project and call {@code discoverChecks()}.</p>
 */
public final class RequiredFilesCheck implements TaikaiCheck {

  /**
   * Returns the settings that require the given files.
   *
   * @param files the paths of the required files, relative to the project directory
   * @return the settings
   */
  public static Settings files(String... files) {
    return new Settings(List.of(files));
  }

  @Override
  public String description() {
    return "Project should have the required files";
  }

  @Override
  public List<String> violations(CheckContext context) {
    return ProjectFiles.missingOrEmpty(context.projectDirectory(),
        context.requireConfiguration(Settings.class).files());
  }

  /**
   * The settings of the {@link RequiredFilesCheck}.
   *
   * @param files the paths of the required files, relative to the project directory
   */
  public record Settings(List<String> files) {

    /**
     * Creates the settings.
     *
     * @param files the paths of the required files, relative to the project directory
     * @throws IllegalArgumentException if no file is given
     */
    public Settings {
      files = List.copyOf(files);

      if (files.isEmpty()) {
        throw new IllegalArgumentException("At least one required file must be given");
      }
    }
  }
}
