package com.enofex.taikai.checks;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Checks that the project contains no forbidden files, for example private keys or {@code .env}
 * files. The files are given as glob patterns relative to the
 * {@linkplain CheckContext#projectDirectory() project directory}, as in
 * {@link java.nio.file.FileSystem#getPathMatcher(String)}. As in {@code .gitignore},
 * <code>**&#47;</code> also matches no directory, so <code>**&#47;.env</code> also finds the
 * {@code .env} in the project directory.
 *
 * <p>The check needs a configuration. Patterns in {@code src/main} are a good start, because
 * everything in there ends up in the artifact, whether it is committed or not:</p>
 *
 * <pre>
 * .discoverChecks(checks -&gt; checks
 *     .configure(ForbiddenFilesCheck.class,
 *         ForbiddenFilesCheck.files("src/main/**&#47;*.key", "src/main/**&#47;.env")))
 * </pre>
 *
 * <p>Only the directory before the first wildcard of a pattern is searched, and only as deep as
 * the pattern reaches, so <code>src/main/**&#47;*.key</code> does not look into {@code target} or
 * {@code .git}. A pattern outside of the project directory starts with {@code ..}, for example
 * {@code ../.env} in the root of a multi-module build while the project directory is the module
 * under test.</p>
 *
 * <p>Taikai does not register its predefined checks itself, so that a new Taikai version never adds
 * checks on its own. To run this check, list its class name in the file
 * {@code src/test/resources/META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck} of the
 * project and call {@code discoverChecks()}.</p>
 */
public final class ForbiddenFilesCheck implements TaikaiCheck {

  /**
   * Returns the settings that forbid the given files.
   *
   * @param files the glob patterns of the forbidden files, relative to the project directory
   * @return the settings
   */
  public static Settings files(String... files) {
    return new Settings(List.of(files));
  }

  @Override
  public String description() {
    return "Project should not contain forbidden files";
  }

  @Override
  public List<String> violations(CheckContext context) {
    ProjectFileSearch search = new ProjectFileSearch(context.projectDirectory());
    Map<String, String> patternsByFile = new TreeMap<>();

    for (String pattern : context.requireConfiguration(Settings.class).files()) {
      for (Path file : search.files(pattern)) {
        patternsByFile.putIfAbsent(file.toString(), pattern);
      }
    }

    return patternsByFile.entrySet().stream()
        .map(entry -> "%s is forbidden (matches %s)".formatted(entry.getKey(), entry.getValue()))
        .toList();
  }

  /**
   * The settings of the {@link ForbiddenFilesCheck}.
   *
   * @param files the glob patterns of the forbidden files, relative to the project directory
   */
  public record Settings(List<String> files) {

    /**
     * Creates the settings.
     *
     * @param files the glob patterns of the forbidden files, relative to the project directory
     * @throws IllegalArgumentException if no pattern is given
     */
    public Settings {
      files = List.copyOf(files);

      if (files.isEmpty()) {
        throw new IllegalArgumentException("At least one forbidden file must be given");
      }
    }
  }
}
