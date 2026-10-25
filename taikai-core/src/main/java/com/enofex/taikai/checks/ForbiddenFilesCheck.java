package com.enofex.taikai.checks;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

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
 * {@code .git}.</p>
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
    Path projectDirectory = context.projectDirectory();
    Map<Search, List<Path>> filesBySearch = new HashMap<>();
    Map<String, String> patternsByFile = new TreeMap<>();

    for (String pattern : context.requireConfiguration(Settings.class).files()) {
      List<PathMatcher> matchers = matchers(pattern);
      Search search = Search.of(projectDirectory, pattern);

      for (Path file : filesBySearch.computeIfAbsent(search, Search::files)) {
        Path relativeFile = projectDirectory.relativize(file);

        if (matchers.stream().anyMatch(matcher -> matcher.matches(relativeFile))) {
          patternsByFile.putIfAbsent(relativeFile.toString(), pattern);
        }
      }
    }

    return patternsByFile.entrySet().stream()
        .map(entry -> "%s is forbidden (matches %s)".formatted(entry.getKey(), entry.getValue()))
        .toList();
  }

  private static List<PathMatcher> matchers(String pattern) {
    Set<String> variants = new LinkedHashSet<>(List.of(pattern));
    boolean added = true;

    while (added) {
      added = false;

      for (String variant : List.copyOf(variants)) {
        for (int index = variant.indexOf("**/"); index >= 0;
            index = variant.indexOf("**/", index + 1)) {
          added |= variants.add(variant.substring(0, index) + variant.substring(index + 3));
        }
      }
    }

    return variants.stream()
        .map(variant -> FileSystems.getDefault().getPathMatcher("glob:" + variant))
        .toList();
  }

  private record Search(Path directory, int depth) {

    static Search of(Path projectDirectory, String pattern) {
      String[] segments = pattern.split("/");
      int fixedSegments = 0;

      while (fixedSegments < segments.length - 1 && !isWildcard(segments[fixedSegments])) {
        fixedSegments++;
      }

      return new Search(
          projectDirectory.resolve(String.join("/",
              Arrays.asList(segments).subList(0, fixedSegments))),
          pattern.contains("**") ? Integer.MAX_VALUE : segments.length - fixedSegments);
    }

    private static boolean isWildcard(String segment) {
      return segment.chars().anyMatch(character -> "*?[{".indexOf(character) >= 0);
    }

    List<Path> files() {
      if (!Files.isDirectory(this.directory)) {
        return List.of();
      }

      try (Stream<Path> files = Files.walk(this.directory, this.depth)) {
        return files.filter(Files::isRegularFile).toList();
      } catch (IOException e) {
        throw new UncheckedIOException("Could not search " + this.directory, e);
      }
    }
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
