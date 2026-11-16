package com.enofex.taikai.exports;

import static java.util.stream.Collectors.joining;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.exports.spi.ExportContext;
import com.enofex.taikai.exports.spi.TaikaiExport;
import java.util.Collection;
import org.jspecify.annotations.Nullable;

/**
 * Exports the rules as Markdown instructions for coding agents, which read files such as
 * {@code AGENTS.md} or {@code CLAUDE.md} before they change code. The file lists the description
 * of every configured rule and is written to the
 * {@linkplain ExportContext#projectDirectory() project directory}. The export needs a
 * configuration:
 *
 * <pre>{@code
 * .discoverExports(exports -> exports
 *     .configure(AgentsMarkdownExport.class, AgentsMarkdownExport.file("ARCHITECTURE.md")))
 * }</pre>
 *
 * <p>For the namespace {@code com.company.project}, the file reads:</p>
 *
 * <pre>{@code
 * # Architecture Rules
 *
 * The architecture test enforces these rules for `com.company.project`, so code that breaks one of them fails the build.
 *
 * - Classes should not use System.out or System.err
 * - Fields should not be public unless they are static
 * }</pre>
 *
 * <p>The export replaces the whole file. To keep instructions written by hand, export to a file of
 * its own and reference it from {@code AGENTS.md} or {@code CLAUDE.md}. The lines end with
 * {@code \n} on every platform, so the file only changes when the rules change.</p>
 *
 * <p>Taikai does not register its predefined exports itself, so that a new Taikai version never
 * writes files on its own. To run this export, list its class name in the file
 * {@code src/test/resources/META-INF/services/com.enofex.taikai.exports.spi.TaikaiExport} of the
 * project and call {@code discoverExports()}.</p>
 */
public final class AgentsMarkdownExport implements TaikaiExport {

  /**
   * Returns the settings that export to the given file.
   *
   * @param file the path of the file, relative to the project directory
   * @return the settings
   */
  public static Settings file(String file) {
    return new Settings(file);
  }

  @Override
  public String description() {
    return "Rules as Markdown for coding agents";
  }

  @Override
  public void export(ExportContext context) {
    ExportFiles.write(
        ExportFiles.resolve(context, context.requireConfiguration(Settings.class).file()),
        markdown(context.namespace(), context.rules()));
  }

  private static String markdown(@Nullable String namespace, Collection<TaikaiRule> rules) {
    StringBuilder markdown = new StringBuilder("# Architecture Rules\n\n")
        .append(namespace != null
            ? "The architecture test enforces these rules for `%s`, ".formatted(namespace)
            : "The architecture test enforces these rules, ")
        .append("so code that breaks one of them fails the build.\n\n");

    rules.stream()
        .map(rule -> rule.archRule().getDescription())
        .distinct()
        .forEach(description -> markdown.append(listItem(description)));

    return markdown.toString();
  }

  /**
   * Returns a description as a list item. Its further lines are indented, so that they belong to
   * the item, and every line ends with {@code \n}, also for descriptions whose lines ArchUnit
   * separates with the line separator of the platform, such as of layered architectures.
   */
  private static String listItem(String description) {
    return description.lines().collect(joining("\n  ", "- ", "\n"));
  }

  /**
   * The settings of the {@link AgentsMarkdownExport}.
   *
   * @param file the path of the file, relative to the project directory
   */
  public record Settings(String file) {

    /**
     * Creates the settings.
     *
     * @param file the path of the file, relative to the project directory
     * @throws IllegalArgumentException if the file is blank
     */
    public Settings {
      file = ExportFiles.requireFile(file);
    }
  }
}
