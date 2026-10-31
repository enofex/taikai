package com.enofex.taikai.exports;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.TaikaiRule;
import com.enofex.taikai.exports.spi.ExportContext;
import com.enofex.taikai.exports.spi.TaikaiExport;
import com.tngtech.archunit.core.Convertible;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.SourceCodeLocation;
import com.tngtech.archunit.core.domain.properties.HasSourceCodeLocation;
import com.tngtech.archunit.lang.EvaluationResult;
import com.tngtech.archunit.lang.Priority;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * Exports the violations of the rules as a SARIF 2.1.0 report, with the file and line of each
 * violation. The export needs a configuration:
 *
 * <pre>{@code
 * .discoverExports(exports -> exports
 *     .configure(SarifExport.class, SarifExport.file("target/taikai.sarif")))
 * }</pre>
 *
 * <p>The export {@linkplain ExportContext#evaluate(TaikaiRule) evaluates} every rule and writes
 * the report even if there are violations: failing the build is left to {@code check()} or
 * {@code checkAll()}, ideally in a test of its own. Every rule is listed in the report, with an
 * id derived from its description and the level {@code error}, {@code warning} or {@code note}
 * for the ArchUnit priority {@code HIGH}, {@code MEDIUM} or {@code LOW}.</p>
 *
 * <p>The location of a violation is the source file of its class, looked up in the
 * {@linkplain Settings#sourceDirectories() source directories} by the package of the class, or
 * else by its file name if exactly one source file has that name, as Kotlin files do not need to
 * be in the directory of their package. The report names source files by absolute {@code file}
 * URIs, so that they do not depend on the directory the report is read from, also in
 * multi-module builds. Violations without a line, such as of classes or fields, point to the
 * first line of their file. Code scanning on GitHub only shows violations with a location, so
 * violations of a package point to its {@code package-info}, or else to its first class by name,
 * and violations of a cycle to its first dependency. Violations without a source file have no
 * location.</p>
 *
 * <p>Taikai does not register its predefined exports itself, so that a new Taikai version never
 * writes files on its own. To run this export, list its class name in the file
 * {@code src/test/resources/META-INF/services/com.enofex.taikai.exports.spi.TaikaiExport} of the
 * project and call {@code discoverExports()}.</p>
 */
public final class SarifExport implements TaikaiExport {

  private static final List<String> SOURCE_DIRECTORIES = List.of(
      "src/main/java", "src/main/kotlin", "src/test/java", "src/test/kotlin");
  private static final String SCHEMA = "https://json.schemastore.org/sarif-2.1.0.json";
  private static final int MAX_DESCRIPTION_LENGTH = 1024;
  private static final Comparator<Result> RESULT_ORDER = Comparator
      .comparingInt(Result::ruleIndex)
      .thenComparing(Result::location, Comparator.nullsFirst(
          Comparator.comparing(Location::file).thenComparingInt(Location::line)))
      .thenComparing(Result::message);

  /**
   * Returns the settings that export to the given file, with the source directories
   * {@code src/main/java}, {@code src/main/kotlin}, {@code src/test/java} and
   * {@code src/test/kotlin}.
   *
   * @param file the path of the file, relative to the project directory
   * @return the settings
   * @see Settings#withSourceDirectories(String...)
   */
  public static Settings file(String file) {
    return new Settings(file, SOURCE_DIRECTORIES);
  }

  @Override
  public String description() {
    return "Rule violations as SARIF";
  }

  @Override
  public void export(ExportContext context) {
    Settings settings = context.requireConfiguration(Settings.class);
    SourceFiles sourceFiles = new SourceFiles(settings.sourceDirectories().stream()
        .map(directory -> context.projectDirectory().resolve(directory).normalize())
        .toList());
    List<Rule> rules = new ArrayList<>();
    Map<String, Integer> ruleIndexes = new HashMap<>();
    Set<Result> results = new LinkedHashSet<>();

    for (TaikaiRule rule : context.rules()) {
      String description = rule.archRule().getDescription();
      EvaluationResult evaluation = context.evaluate(rule);
      int ruleIndex = ruleIndexes.computeIfAbsent(description, key -> {
        rules.add(new Rule(id(description), description, level(evaluation.getPriority())));
        return rules.size() - 1;
      });

      evaluation.handleViolations((Collection<Object> objects, String message) ->
          results.add(new Result(ruleIndex, message, location(objects, sourceFiles))));
    }

    write(context.projectDirectory().resolve(settings.file()).normalize(),
        sarif(rules, results.stream().sorted(RESULT_ORDER).toList()));
  }

  private static String id(String description) {
    try {
      byte[] hash = MessageDigest.getInstance("SHA-256")
          .digest(description.getBytes(StandardCharsets.UTF_8));
      return "taikai-" + HexFormat.of().formatHex(hash, 0, 6);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not supported", e);
    }
  }

  private static String level(Priority priority) {
    return switch (priority) {
      case HIGH -> "error";
      case MEDIUM -> "warning";
      case LOW -> "note";
    };
  }

  private static @Nullable Location location(Collection<?> objects, SourceFiles sourceFiles) {
    return objects.stream()
        .map(SarifExport::sourceCodeLocation)
        .flatMap(Optional::stream)
        .findFirst()
        .flatMap(location -> sourceFiles.find(location)
            .map(file -> new Location(file, location.getLineNumber())))
        .orElse(null);
  }

  /**
   * Returns where a violating object is in the source code. A package has no location of its own,
   * so its {@code package-info} stands for it, or else its first class by name. A cycle has none
   * either, so its first dependency stands for it, in the order ArchUnit sorts dependencies.
   */
  private static Optional<SourceCodeLocation> sourceCodeLocation(Object object) {
    if (object instanceof HasSourceCodeLocation hasSourceCodeLocation) {
      return Optional.of(hasSourceCodeLocation.getSourceCodeLocation());
    }

    if (object instanceof JavaPackage javaPackage) {
      return javaPackage.tryGetPackageInfo()
          .flatMap(SarifExport::sourceCodeLocation)
          .or(() -> javaPackage.getClasses().stream()
              .min(Comparator.comparing(JavaClass::getName))
              .map(JavaClass::getSourceCodeLocation));
    }

    if (object instanceof Convertible convertible) {
      return convertible.convertTo(Dependency.class).stream()
          .min(Comparator.naturalOrder())
          .map(Dependency::getSourceCodeLocation);
    }

    return Optional.empty();
  }

  private static Map<String, Object> sarif(List<Rule> rules, List<Result> results) {
    return object(
        "$schema", SCHEMA,
        "version", "2.1.0",
        "runs", List.of(object(
            "tool", object("driver", object(
                "name", "Taikai",
                "informationUri", "https://enofex.github.io/taikai",
                "rules", rules.stream().map(SarifExport::rule).toList())),
            "results", results.stream().map(result -> result(result, rules)).toList())));
  }

  private static Map<String, Object> rule(Rule rule) {
    String description = rule.description().length() > MAX_DESCRIPTION_LENGTH
        ? rule.description().substring(0, MAX_DESCRIPTION_LENGTH - 3) + "..."
        : rule.description();

    return object(
        "id", rule.id(),
        "shortDescription", object("text", description),
        "fullDescription", object("text", description),
        "help", object("text", rule.description()),
        "defaultConfiguration", object("level", rule.level()));
  }

  private static Map<String, Object> result(Result result, List<Rule> rules) {
    Map<String, Object> json = object(
        "ruleId", rules.get(result.ruleIndex()).id(),
        "ruleIndex", result.ruleIndex(),
        "message", object("text", result.message()));
    Location location = result.location();

    if (location != null) {
      json.put("locations", List.of(object("physicalLocation", object(
          "artifactLocation", object("uri", location.file().toUri().toString()),
          "region", object("startLine", Math.max(1, location.line()))))));
    }

    return json;
  }

  private static Map<String, Object> object(Object... entries) {
    Map<String, Object> object = new LinkedHashMap<>();

    for (int i = 0; i < entries.length; i += 2) {
      object.put((String) entries[i], entries[i + 1]);
    }

    return object;
  }

  private static void json(Object value, String indent, StringBuilder json) {
    if (value instanceof Map<?, ?> map) {
      json.append('{');
      appendAll(map.entrySet(), indent, json, (entry, inner) -> {
        string(entry.getKey().toString(), json);
        json.append(": ");
        json(entry.getValue(), inner, json);
      });
      json.append('}');
    } else if (value instanceof List<?> list) {
      json.append('[');
      appendAll(list, indent, json, (element, inner) -> json(element, inner, json));
      json.append(']');
    } else if (value instanceof String string) {
      string(string, json);
    } else {
      json.append(value);
    }
  }

  private static <T> void appendAll(Collection<T> elements, String indent, StringBuilder json,
      BiConsumer<T, String> writer) {
    if (elements.isEmpty()) {
      return;
    }

    String inner = indent + "  ";
    int written = 0;

    for (T element : elements) {
      json.append(written++ == 0 ? "\n" : ",\n").append(inner);
      writer.accept(element, inner);
    }

    json.append('\n').append(indent);
  }

  private static void string(String value, StringBuilder json) {
    json.append('"');

    for (char character : value.toCharArray()) {
      switch (character) {
        case '"' -> json.append("\\\"");
        case '\\' -> json.append("\\\\");
        case '\n' -> json.append("\\n");
        case '\r' -> json.append("\\r");
        case '\t' -> json.append("\\t");
        default -> json.append(character < 0x20
            ? "\\u%04x".formatted((int) character)
            : String.valueOf(character));
      }
    }

    json.append('"');
  }

  private static void write(Path file, Map<String, Object> sarif) {
    StringBuilder json = new StringBuilder();
    json(sarif, "", json);

    try {
      Path directory = file.getParent();

      if (directory != null) {
        Files.createDirectories(directory);
      }

      Files.writeString(file, json.append('\n'), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not write " + file, e);
    }
  }

  /**
   * The settings of the {@link SarifExport}.
   *
   * @param file the path of the file, relative to the project directory
   * @param sourceDirectories the paths of the directories with the source files, relative to the
   *     project directory
   */
  public record Settings(String file, List<String> sourceDirectories) {

    /**
     * Creates the settings.
     *
     * @param file the path of the file, relative to the project directory
     * @param sourceDirectories the paths of the directories with the source files, relative to
     *     the project directory
     * @throws IllegalArgumentException if the file is blank or no source directory is given
     */
    public Settings {
      requireNonNull(file);
      sourceDirectories = List.copyOf(sourceDirectories);

      if (file.isBlank()) {
        throw new IllegalArgumentException("The file to export to must be given");
      }

      if (sourceDirectories.isEmpty()) {
        throw new IllegalArgumentException("At least one source directory must be given");
      }
    }

    /**
     * Returns these settings with other source directories, for example for generated sources.
     *
     * <pre>{@code
     * SarifExport.file("target/taikai.sarif")
     *     .withSourceDirectories("src/main/java", "target/generated-sources/annotations")
     * }</pre>
     *
     * @param sourceDirectories the paths of the directories with the source files, relative to
     *     the project directory
     * @return the settings with the given source directories
     * @throws IllegalArgumentException if no source directory is given
     */
    public Settings withSourceDirectories(String... sourceDirectories) {
      return new Settings(this.file, List.of(sourceDirectories));
    }
  }

  private record Rule(String id, String description, String level) {

  }

  private record Result(int ruleIndex, String message, @Nullable Location location) {

  }

  private record Location(Path file, int line) {

  }

  private static final class SourceFiles {

    private final List<Path> directories;
    private final Map<String, Optional<Path>> found;
    private @Nullable Map<String, List<Path>> byName;

    SourceFiles(List<Path> directories) {
      this.directories = directories;
      this.found = new HashMap<>();
    }

    Optional<Path> find(SourceCodeLocation location) {
      String packageName = location.getSourceClass().getPackageName();
      String fileName = location.getSourceFileName();

      return this.found.computeIfAbsent(packageName + "/" + fileName,
          key -> inPackageDirectory(packageName, fileName).or(() -> uniqueByName(fileName)));
    }

    private Optional<Path> inPackageDirectory(String packageName, String fileName) {
      String packageDirectory = packageName.replace('.', '/');

      return this.directories.stream()
          .map(directory -> directory.resolve(packageDirectory).resolve(fileName))
          .filter(Files::isRegularFile)
          .findFirst();
    }

    private Optional<Path> uniqueByName(String fileName) {
      List<Path> files = byName().getOrDefault(fileName, List.of());

      return files.size() == 1 ? Optional.of(files.get(0)) : Optional.empty();
    }

    private Map<String, List<Path>> byName() {
      Map<String, List<Path>> files = this.byName;

      if (files == null) {
        files = new HashMap<>();

        for (Path directory : this.directories) {
          addFiles(directory, files);
        }

        this.byName = files;
      }

      return files;
    }

    private static void addFiles(Path directory, Map<String, List<Path>> files) {
      if (!Files.isDirectory(directory)) {
        return;
      }

      try (Stream<Path> paths = Files.walk(directory)) {
        paths.filter(Files::isRegularFile)
            .forEach(file -> files
                .computeIfAbsent(file.getFileName().toString(), name -> new ArrayList<>())
                .add(file));
      } catch (IOException e) {
        throw new UncheckedIOException("Could not read " + directory, e);
      }
    }
  }
}
