package com.enofex.taikai.checks;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks the content of the project files with rules. A rule applies a {@link Condition} to the
 * files matching a glob pattern relative to the
 * {@linkplain CheckContext#projectDirectory() project directory}, as in the
 * {@link ForbiddenFilesCheck}. The check needs a configuration:
 *
 * <pre>
 * .discoverChecks(checks -&gt; checks
 *     .configure(FileContentCheck.class, FileContentCheck.rules(
 *         FileContentCheck.forbid("src/main/**&#47;*.java", "^import .*\\*;"),
 *         FileContentCheck.require("src/main/**&#47;package-info.java", "@NullMarked"))))
 * </pre>
 *
 * <p>In regular expressions, {@code ^} and {@code $} match at every line. Files are read as UTF-8;
 * binary files are skipped.</p>
 *
 * <p>Taikai does not register its predefined checks itself, so that a new Taikai version never adds
 * checks on its own. To run this check, list its class name in the file
 * {@code src/test/resources/META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck} of the
 * project and call {@code discoverChecks()}.</p>
 */
public final class FileContentCheck implements TaikaiCheck {

  private static final String BYTE_ORDER_MARK = "\uFEFF";

  /**
   * Returns the settings that check the files with the given rules.
   *
   * @param rules the rules
   * @return the settings
   */
  public static Settings rules(Rule... rules) {
    return new Settings(List.of(rules));
  }

  /**
   * Returns the rule that the given files must not contain the regular expression, reporting
   * every line that does.
   *
   * @param files the glob pattern of the files, relative to the project directory
   * @param regex the regular expression of the forbidden content
   * @return the rule
   * @throws java.util.regex.PatternSyntaxException if the regular expression is invalid
   */
  public static Rule forbid(String files, String regex) {
    Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

    return new Rule(files, (file, content) -> forbidden(pattern, file, content));
  }

  /**
   * Returns the rule that the given files must contain the regular expression.
   *
   * @param files the glob pattern of the files, relative to the project directory
   * @param regex the regular expression of the required content
   * @return the rule
   * @throws java.util.regex.PatternSyntaxException if the regular expression is invalid
   */
  public static Rule require(String files, String regex) {
    Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

    return new Rule(files, (file, content) -> pattern.matcher(content).find()
        ? List.of()
        : List.of("%s does not contain required content (should match %s)".formatted(file, regex)));
  }

  /**
   * Returns the rule that the given files must fulfill the condition, for example a lambda.
   *
   * @param files the glob pattern of the files, relative to the project directory
   * @param condition the condition of the content
   * @return the rule
   */
  public static Rule rule(String files, Condition condition) {
    return new Rule(files, condition);
  }

  @Override
  public String description() {
    return "Project files should follow the content rules";
  }

  @Override
  public List<String> violations(CheckContext context) {
    Path projectDirectory = context.projectDirectory();
    ProjectFileSearch search = new ProjectFileSearch(projectDirectory);
    Map<Path, List<Rule>> rulesByFile = new TreeMap<>();

    for (Rule rule : context.requireConfiguration(Settings.class).rules()) {
      for (Path file : search.files(rule.files())) {
        rulesByFile.computeIfAbsent(file, key -> new ArrayList<>()).add(rule);
      }
    }

    List<String> violations = new ArrayList<>();

    rulesByFile.forEach((file, rules) -> text(projectDirectory.resolve(file)).ifPresent(content ->
        rules.forEach(rule -> violations.addAll(rule.condition().violations(file, content)))));

    return violations;
  }

  private static List<String> forbidden(Pattern pattern, Path file, String content) {
    Set<Integer> lines = new LinkedHashSet<>();
    Matcher matcher = pattern.matcher(content);
    int line = 1;
    int position = 0;

    while (matcher.find()) {
      for (; position < matcher.start(); position++) {
        if (content.charAt(position) == '\n') {
          line++;
        }
      }

      lines.add(line);
    }

    return lines.stream()
        .map(number -> "%s:%d contains forbidden content (matches %s)".formatted(
            file, number, pattern.pattern()))
        .toList();
  }

  private static Optional<String> text(Path file) {
    try {
      byte[] bytes = Files.readAllBytes(file);

      for (byte value : bytes) {
        // a NUL byte marks a binary file, as in Git
        if (value == 0) {
          return Optional.empty();
        }
      }

      String text = new String(bytes, StandardCharsets.UTF_8);

      return Optional.of(text.startsWith(BYTE_ORDER_MARK) ? text.substring(1) : text);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read " + file, e);
    }
  }

  /**
   * A condition that the content of a file must fulfill.
   */
  @FunctionalInterface
  public interface Condition {

    /**
     * Checks the content of a file and returns its violations.
     *
     * @param file the path of the file, relative to the project directory
     * @param content the content of the file
     * @return the violations, or an empty list if the content fulfills the condition
     */
    List<String> violations(Path file, String content);
  }

  /**
   * A rule of the {@link FileContentCheck}.
   *
   * @param files the glob pattern of the files, relative to the project directory
   * @param condition the condition of the content
   */
  public record Rule(String files, Condition condition) {
  }

  /**
   * The settings of the {@link FileContentCheck}.
   *
   * @param rules the rules
   */
  public record Settings(List<Rule> rules) {

    /**
     * Creates the settings.
     *
     * @param rules the rules
     * @throws IllegalArgumentException if no rule is given
     */
    public Settings {
      rules = List.copyOf(rules);

      if (rules.isEmpty()) {
        throw new IllegalArgumentException("At least one rule must be given");
      }
    }
  }
}
