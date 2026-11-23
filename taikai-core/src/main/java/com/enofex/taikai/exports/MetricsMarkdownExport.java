package com.enofex.taikai.exports;

import static java.util.Comparator.comparingDouble;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.summingInt;

import com.enofex.taikai.exports.ArchitectureMetrics.ClassMetric;
import com.enofex.taikai.exports.ArchitectureMetrics.CohesionMetric;
import com.enofex.taikai.exports.ArchitectureMetrics.EntryPointMetric;
import com.enofex.taikai.exports.ArchitectureMetrics.InheritanceMetric;
import com.enofex.taikai.exports.ArchitectureMetrics.LibraryUsage;
import com.enofex.taikai.exports.ArchitectureMetrics.PackageCycle;
import com.enofex.taikai.exports.ArchitectureMetrics.PackageDependency;
import com.enofex.taikai.exports.ArchitectureMetrics.PackageMetric;
import com.enofex.taikai.exports.ArchitectureMetrics.Zone;
import com.enofex.taikai.exports.spi.ExportContext;
import com.enofex.taikai.exports.spi.TaikaiExport;
import com.enofex.taikai.internal.Arguments;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Exports metrics of the architecture as Markdown, with SVG charts of the main sequence, the
 * packages, their dependencies and the libraries they use. The export needs a configuration:
 *
 * <pre>{@code
 * .discoverExports(exports -> exports
 *     .configure(MetricsMarkdownExport.class, MetricsMarkdownExport.file("docs/METRICS.md")))
 * }</pre>
 *
 * <p>Names link to their sources in the
 * {@linkplain Settings#withSourceDirectories(String...) source directories}. The charts are named
 * after the Markdown file, such as {@code docs/METRICS-dependencies.svg}. The files only change
 * when the architecture does, so that they can be committed.</p>
 *
 * <p>Classes that only a framework calls, such as Spring MVC controllers, have no fan-in; see
 * {@link EntryPoints} to list the methods that a framework calls.</p>
 *
 * <p>Taikai does not register its predefined exports itself, so that a new Taikai version never
 * writes files on its own. To run this export, list its class name in the file
 * {@code src/test/resources/META-INF/services/com.enofex.taikai.exports.spi.TaikaiExport} of the
 * project and call {@code discoverExports()}.</p>
 */
public final class MetricsMarkdownExport implements TaikaiExport {

  /**
   * The most classes that a list or a tooltip shows.
   */
  static final int LISTED_CLASSES = 20;
  private static final Pattern UNSAFE_IN_LINK = Pattern.compile("[\\s()]");
  private static final Pattern LINE_BREAK = Pattern.compile("\\R");

  /**
   * Returns the settings that export to the given Markdown file, with the charts next to it and the
   * source directories of Maven and Gradle.
   *
   * @param file the Markdown file, relative to the project directory
   * @return the settings
   */
  public static Settings file(String file) {
    requireNonNull(file);

    int name = Math.max(file.lastIndexOf('/'), file.lastIndexOf('\\'));
    // a root directory, such as / or C:\, keeps its separator
    int directory = name == 0 || name > 0 && file.charAt(name - 1) == ':' ? name + 1 : name;

    return new Settings(file, name < 0 ? "." : file.substring(0, directory),
        SourceFiles.DEFAULT_DIRECTORIES, List.of());
  }

  @Override
  public String description() {
    return "Architecture metrics as Markdown";
  }

  @Override
  public void export(ExportContext context) {
    Settings settings = context.requireConfiguration(Settings.class);
    Path file = ExportFiles.resolve(context, settings.file());
    Path imageDirectory = ExportFiles.resolve(context, settings.imageDirectory());
    JavaClasses classes = context.classes();
    ArchitectureMetrics metrics = ArchitectureMetrics.of(classes, settings.entryPoints());
    Map<Path, String> files = new LinkedHashMap<>();
    Charts charts = new Charts(
        chart(files, file, imageDirectory, "-main-sequence.svg", MainSequenceChart.svg(metrics)),
        chart(files, file, imageDirectory, "-package-map.svg", PackageMap.svg(metrics)),
        chart(files, file, imageDirectory, "-dependencies.svg", DependencyChart.svg(metrics)),
        chart(files, file, imageDirectory, "-dependency-matrix.svg",
            DependencyMatrix.packages(metrics)),
        chart(files, file, imageDirectory, "-libraries.svg", DependencyMatrix.libraries(metrics)));
    Links links = new Links(file, classes,
        SourceFiles.in(context.projectDirectory(), settings.sourceDirectories()), metrics);

    files.put(file, new Markdown(metrics, context.namespace(), charts, links,
        !settings.entryPoints().isEmpty()).toString());
    files.forEach(ExportFiles::write);
  }

  /**
   * Adds a chart to the files to write and returns the link to it. The files are only written when
   * all are built, so that a failing chart leaves no new charts next to an old Markdown file.
   *
   * @throws IllegalArgumentException if the charts are on another drive, which no link reaches
   */
  private static String chart(Map<Path, String> files, Path file, Path imageDirectory,
      String suffix, String svg) {
    Path chart = imageDirectory.resolve(baseName(file) + suffix);

    files.put(chart, svg);

    return link(file, chart).orElseThrow(() -> new IllegalArgumentException(
        "The directory of the charts %s must be on the same drive as the file %s".formatted(
            imageDirectory, file)));
  }

  private static String baseName(Path file) {
    String name = String.valueOf(file.getFileName());
    int extension = name.lastIndexOf('.');

    return extension > 0 ? name.substring(0, extension) : name;
  }

  /**
   * Returns the relative link from the Markdown file, encoded for Markdown, or nothing on another
   * drive.
   */
  private static Optional<String> link(Path file, Path target) {
    Path directory = file.toAbsolutePath().getParent();
    Path absolute = target.toAbsolutePath();

    if (directory == null || !Objects.equals(directory.getRoot(), absolute.getRoot())) {
      return Optional.empty();
    }

    String link = directory.equals(absolute)
        ? "."
        : directory.relativize(absolute).toString().replace(File.separatorChar, '/')
            .replace("%", "%25")
            .replace("#", "%23")
            .replace("?", "%3F")
            .replace("<", "%3C")
            .replace(">", "%3E");

    return Optional.of(UNSAFE_IN_LINK.matcher(link).find() ? "<" + link + ">" : link);
  }

  /**
   * The settings of the {@link MetricsMarkdownExport}, with paths relative to the project
   * directory.
   *
   * @param file the Markdown file
   * @param imageDirectory the directory of the charts
   * @param sourceDirectories the directories of the sources that names link to
   * @param entryPoints the entry points to list, none to list no entry points
   */
  public record Settings(String file, String imageDirectory, List<String> sourceDirectories,
      List<EntryPoints> entryPoints) {

    /**
     * Creates the settings.
     *
     * @param file the Markdown file
     * @param imageDirectory the directory of the charts
     * @param sourceDirectories the directories of the sources that names link to
     * @param entryPoints the entry points to list, none to list no entry points
     * @throws IllegalArgumentException if the file or the directory is blank, or no source
     *     directory is given
     */
    public Settings {
      file = ExportFiles.requireFile(file);
      imageDirectory = ExportFiles.requireNotBlank(imageDirectory,
          "The directory of the charts must be given");
      sourceDirectories = ExportFiles.requireDirectories(sourceDirectories);
      entryPoints = List.copyOf(entryPoints);
    }

    /**
     * Creates the settings without entry points.
     *
     * @param file the Markdown file
     * @param imageDirectory the directory of the charts
     * @param sourceDirectories the directories of the sources that names link to
     * @throws IllegalArgumentException if the file or the directory is blank, or no source
     *     directory is given
     */
    public Settings(String file, String imageDirectory, List<String> sourceDirectories) {
      this(file, imageDirectory, sourceDirectories, List.of());
    }

    /**
     * Returns these settings with another directory for the charts.
     *
     * @param imageDirectory the directory, relative to the project directory
     * @return the new settings
     * @throws IllegalArgumentException if the directory is blank
     */
    public Settings withImageDirectory(String imageDirectory) {
      return new Settings(this.file, imageDirectory, this.sourceDirectories, this.entryPoints);
    }

    /**
     * Returns these settings with other source directories, for example for generated sources.
     *
     * <pre>{@code
     * MetricsMarkdownExport.file("docs/METRICS.md")
     *     .withSourceDirectories("src/main/java", "target/generated-sources/annotations")
     * }</pre>
     *
     * @param sourceDirectories the paths of the directories with the source files, relative to
     *     the project directory
     * @return the settings with the given source directories
     * @throws IllegalArgumentException if no source directory is given
     */
    public Settings withSourceDirectories(String... sourceDirectories) {
      return new Settings(this.file, this.imageDirectory, List.of(sourceDirectories),
          this.entryPoints);
    }

    /**
     * Returns these settings with {@link EntryPoints}, which the report lists with the fan-in of
     * their classes and counts per package.
     *
     * <pre>{@code
     * MetricsMarkdownExport.file("docs/METRICS.md")
     *     .withEntryPoints(EntryPoints.springMvc(),
     *         EntryPoints.annotatedWith("org.springframework.scheduling.annotation.Scheduled"))
     * }</pre>
     *
     * @param entryPoints the entry points, which replace those of these settings
     * @return the settings with the given entry points
     * @throws IllegalArgumentException if no entry points are given
     */
    public Settings withEntryPoints(EntryPoints... entryPoints) {
      return new Settings(this.file, this.imageDirectory, this.sourceDirectories,
          Arguments.requireNotEmpty(List.of(entryPoints),
              "At least one kind of entry points must be given"));
    }
  }

  private record Charts(String mainSequence, String treemap, String dependencies,
      String dependencyMatrix, String libraries) {

  }

  /**
   * Writes names as code, linked to their sources if found.
   */
  private record Links(Path file, JavaClasses classes, SourceFiles sources,
      ArchitectureMetrics metrics) {

    String packageName(String packageName) {
      return linked(this.metrics.packageLabel(packageName), this.sources.directory(packageName));
    }

    String className(String className) {
      return linked(this.metrics.classLabel(className),
          this.sources.find(this.classes.get(className).getSourceCodeLocation()));
    }

    private String linked(String label, Optional<Path> target) {
      String code = "`" + label + "`";

      return target.flatMap(path -> link(this.file, path))
          .map(link -> "[%s](%s)".formatted(code, link))
          .orElse(code);
    }
  }

  private static final class Markdown {

    private static final List<String> SECTIONS = List.of("Summary", "Main Sequence",
        "Package Map", "Packages", "Package Dependencies", "Libraries", "Inheritance", "Coupling",
        "Cohesion");

    private final ArchitectureMetrics metrics;
    private final Links links;
    private final boolean withEntryPoints;
    private final Map<String, Long> librariesOf;
    private final Map<String, Integer> entryPointsOf;
    private final StringBuilder markdown = new StringBuilder();

    Markdown(ArchitectureMetrics metrics, @Nullable String namespace, Charts charts,
        Links links, boolean withEntryPoints) {
      this.metrics = metrics;
      this.links = links;
      this.withEntryPoints = withEntryPoints;
      this.librariesOf = metrics.libraryUsages().stream()
          .collect(groupingBy(LibraryUsage::packageName, counting()));
      this.entryPointsOf = metrics.entryPoints().stream()
          .collect(groupingBy(EntryPointMetric::packageName,
              summingInt(EntryPointMetric::methods)));

      introduction(namespace);
      contents();
      summary();
      mainSequence(charts.mainSequence());
      packageMap(charts.treemap());
      packages();
      packageDependencies(charts.dependencies(), charts.dependencyMatrix());
      libraries(charts.libraries());
      inheritance();
      coupling();
      cohesion();
    }

    @Override
    public String toString() {
      return this.markdown.toString().stripTrailing() + "\n";
    }

    private void introduction(@Nullable String namespace) {
      this.markdown.append("# Architecture Metrics\n\n")
          .append(namespace != null
              ? "The metrics of `%s`, measured by Taikai. ".formatted(namespace)
              : "The metrics of the configured classes, measured by Taikai. ")
          .append("""
              Every package counts on its own, without its subpackages. Packages and coupling \
              count top-level classes, each together with the classes nested in it, while \
              inheritance and cohesion count every class on its own. Only dependencies between \
              the measured classes count, except for the libraries the packages use, and never \
              those on the JDK.\s""");

      if (!this.metrics.prefix().isEmpty()) {
        this.markdown.append("Packages and classes are named relative to `%s`. "
            .formatted(this.metrics.prefix()));
      }

      this.markdown.append("The lists show at most %s classes each.\n\n"
          .formatted(LISTED_CLASSES));
    }

    /**
     * Links every section by the anchors that GitHub derives from the headings.
     */
    private void contents() {
      this.markdown.append("**Contents:** ")
          .append(SECTIONS.stream()
              .map(section -> "[%s](#%s)".formatted(section,
                  section.toLowerCase(Locale.ROOT).replace(' ', '-')))
              .collect(joining(" \u00b7 ")))
          .append("\n\n");
    }

    private void summary() {
      List<PackageMetric> packages = this.metrics.packages();
      List<List<String>> rows = new ArrayList<>(List.of(
          List.of("Packages", String.valueOf(packages.size())),
          List.of("Classes", String.valueOf(this.metrics.classes().size())),
          List.of("Abstract classes and interfaces",
              String.valueOf(this.metrics.abstractClasses())),
          List.of("Average distance from the main sequence",
              ArchitectureMetrics.formatted(this.metrics.averageDistance())),
          List.of("Packages in the zone of pain", String.valueOf(inZone(Zone.PAIN))),
          List.of("Packages in the zone of uselessness",
              String.valueOf(inZone(Zone.USELESSNESS))),
          List.of("Package cycles", String.valueOf(this.metrics.packageCycles().size())),
          List.of("Dependencies on less stable packages",
              String.valueOf(lessStableDependencies().size())),
          List.of("Libraries", String.valueOf(this.metrics.libraries().size())),
          List.of("Maximum inheritance depth", String.valueOf(this.metrics.inheritance().stream()
              .mapToInt(InheritanceMetric::depth)
              .max()
              .orElse(0)))));

      if (this.withEntryPoints) {
        rows.add(List.of("Entry points", String.valueOf(this.metrics.entryPointMethods())));
      }

      heading("Summary");
      table(List.of("Metric", "Value"), List.of("---", "---:"), rows);
    }

    private void mainSequence(String chart) {
      heading("Main Sequence");
      image(MainSequenceChart.DESCRIPTION, chart);
      this.markdown.append("""
          Each dot is a package, sized by its number of classes. The abstractness A is the \
          share of abstract classes and interfaces. The instability I = Ce / (Ca + Ce) is the \
          share of outgoing couplings: Ca counts the classes in other packages that depend on \
          the package, Ce the classes of the package that depend on other packages. A dot \
          without room for its name has none: the packages are all listed below.

          """);

      table(List.of("Value", "Meaning"), List.of("---", "---"), List.of(
          List.of("A = 0", "Only concrete classes"),
          List.of("A = 1", "Only abstract classes and interfaces"),
          List.of("I = 0", "Stable: other packages depend on it and it depends on none, so a "
              + "change affects many classes"),
          List.of("I = 1", "Unstable: it depends on other packages and none depends on it, so "
              + "it is easy to change"),
          List.of("D = 0", "On the main sequence, the best"),
          List.of("D = 1", "In a corner, as far from the main sequence as possible, the worst")));

      this.markdown.append("""
          A stable package should be abstract, so that it can be extended without being \
          changed, and an unstable package concrete. Packages on the main sequence A + I = 1 \
          balance both, and the distance D = |A + I - 1| shows how far a package is from it. \
          With a distance above %s, a concrete and stable package is in the zone of pain, hard \
          to change as many classes depend on it, and an abstract and unstable package in the \
          zone of uselessness, with abstractions that barely anything uses. Packages without \
          couplings to other packages have no instability and are not shown.

          """.formatted(ArchitectureMetrics.formatted(ArchitectureMetrics.ZONE_DISTANCE)));
    }

    private void packageMap(String chart) {
      heading("Package Map");
      image(PackageMap.DESCRIPTION, chart);
      this.markdown.append("""
          Each tile is a package: its area is its number of classes and its color its zone, the \
          stronger the farther the package is from the main sequence.

          """);
    }

    private void packages() {
      heading("Packages");
      table(packageColumns(List.of("Package", "Classes", "Abstract", "Ca", "Ce", "I", "A", "D",
              "Visibility", "Libraries", "Zone"), "Entry points"),
          packageColumns(List.of("---", "---:", "---:", "---:", "---:", "---:", "---:", "---:",
              "---:", "---:", "---"), "---:"),
          this.metrics.packages().stream()
              .map(metric -> packageColumns(List.of(
                  this.links.packageName(metric.name()),
                  String.valueOf(metric.classes()),
                  String.valueOf(metric.abstractClasses()),
                  String.valueOf(metric.afferentCouplings()),
                  String.valueOf(metric.efferentCouplings()),
                  ArchitectureMetrics.formatted(metric.instability()),
                  ArchitectureMetrics.formatted(metric.abstractness()),
                  ArchitectureMetrics.formatted(metric.distance()),
                  ArchitectureMetrics.formatted(metric.relativeVisibility()),
                  String.valueOf(this.librariesOf.getOrDefault(metric.name(), 0L)),
                  metric.zone().map(Zone::label).orElse("")),
                  Objects.toString(this.entryPointsOf.get(metric.name()), "")))
              .toList());

      this.markdown.append("""
          Visibility is the relative visibility of Herbert Dowalil, the share of public classes, \
          and Libraries the number of libraries that the package uses.""");

      if (this.withEntryPoints) {
        this.markdown.append(" Entry points counts the methods of the package that a framework "
            + "calls, see [Entry Points](#entry-points).");
      }

      this.markdown.append("\n\n");
    }

    /**
     * Inserts the column of the entry points before the zone, if entry points are listed.
     */
    private List<String> packageColumns(List<String> columns, String entryPoints) {
      if (!this.withEntryPoints) {
        return columns;
      }

      List<String> withEntryPoints = new ArrayList<>(columns);
      withEntryPoints.add(columns.size() - 1, entryPoints);

      return withEntryPoints;
    }

    private void packageDependencies(String chart, String matrix) {
      heading("Package Dependencies");
      image(DependencyChart.DESCRIPTION, chart);
      this.markdown.append("""
          A package is drawn above the packages it depends on, so that the packages that many \
          depend on are at the bottom, and is marked in the color of its zone. An arrow points \
          from a package to a package it depends on, and the thicker it is, the more classes of \
          the one depend on classes of the other. Red arrows point upwards and close a cycle: \
          without them, the packages would have no cycles. Orange arrows point to a less stable \
          package.

          """);

      subheading("Dependency Matrix");
      image(DependencyMatrix.PACKAGES_DESCRIPTION, matrix);
      this.markdown.append("""
          A row counts the pairs of classes with which its package depends on the package of \
          each column, numbered as the rows. The packages are ordered from the bottom of the \
          chart up, so that a package depends on the packages above it: the red cells above the \
          diagonal close a cycle, as the red arrows do, a red frame surrounds the packages of a \
          cycle, and the orange cells use a less stable package, as the orange arrows do.

          """);

      cycles();
      dependenciesOnLessStablePackages();
    }

    private void cycles() {
      subheading("Package Cycles");

      if (this.metrics.packageCycles().isEmpty()) {
        this.markdown.append("None.\n\n");
        return;
      }

      this.markdown.append("""
          Under each cycle are the dependencies that close it, the red arrows of the chart, \
          each with up to %s of the pairs of classes behind it. Without them, the packages \
          would have no cycles, so they are the dependencies to remove or invert.

          """.formatted(LISTED_CLASSES));

      for (PackageCycle cycle : this.metrics.packageCycles()) {
        this.markdown.append("- ")
            .append(cycle.packages().stream()
                .map(this.links::packageName)
                .collect(joining(", ")))
            .append('\n');

        for (PackageDependency dependency : cycle.closingDependencies()) {
          this.markdown.append("  - ")
              .append(Svg.dependency(this.links.packageName(dependency.origin()),
                  this.links.packageName(dependency.target()), dependency.dependencies()))
              .append('\n');
          dependency.classDependencies().stream()
              .limit(LISTED_CLASSES)
              .forEach(classes -> this.markdown.append("    - %s \u2192 %s\n".formatted(
                  this.links.className(classes.origin()),
                  this.links.className(classes.target()))));
        }
      }

      this.markdown.append('\n');
    }

    private void dependenciesOnLessStablePackages() {
      subheading("Dependencies on Less Stable Packages");
      this.markdown.append("""
          A package should only depend on packages that are at least as stable as itself, as the \
          stable dependencies principle of Robert C. Martin demands: a package that many classes \
          depend on is hard to change, so it should not depend on a package that changes more \
          easily. These dependencies point to a package with a higher instability I, the largest \
          increase first. They are orange in the chart and the matrix, unless they close a cycle.

          """);
      table(List.of("Package", "Depends on", "Instability", "Dependencies"),
          List.of("---", "---", "---", "---:"),
          lessStableDependencies().stream()
              .sorted(comparingDouble(PackageDependency::instabilityIncrease).reversed())
              .map(dependency -> List.of(
                  this.links.packageName(dependency.origin()),
                  this.links.packageName(dependency.target()),
                  ArchitectureMetrics.formatted(dependency.originInstability()) + " \u2192 "
                      + ArchitectureMetrics.formatted(dependency.targetInstability()),
                  String.valueOf(dependency.dependencies())))
              .toList());
    }

    private List<PackageDependency> lessStableDependencies() {
      return this.metrics.packageDependencies().stream()
          .filter(PackageDependency::dependsOnLessStable)
          .toList();
    }

    private void libraries(String chart) {
      List<String> withoutLibraries = this.metrics.packages().stream()
          .map(PackageMetric::name)
          .filter(packageName -> !this.librariesOf.containsKey(packageName))
          .map(this.links::packageName)
          .toList();

      heading("Libraries");
      image(DependencyMatrix.LIBRARIES_DESCRIPTION, chart);
      this.markdown.append("""
          A row is a package, numbered as in the dependency matrix, and a column a library, named \
          after the first two segments of the packages of its classes, such as \
          `org.springframework` or `jakarta.persistence`. A cell counts the classes of the \
          package that use the library. The JDK does not count, nor do the standard libraries of \
          Kotlin, Groovy and Scala for the classes written in them, nor the classes of the \
          project: those in the measured packages and in the package of at least two segments \
          that they share.

          """);
      table(List.of("Library", "Packages", "Classes"), List.of("---", "---:", "---:"),
          this.metrics.libraries().stream()
              .map(library -> List.of("`" + library.name() + "`",
                  String.valueOf(library.packages()), String.valueOf(library.classes())))
              .toList());

      subheading("Packages Without Libraries");
      this.markdown
          .append(withoutLibraries.isEmpty() ? "None." : String.join(", ", withoutLibraries))
          .append("\n\n");
    }

    private void inheritance() {
      List<InheritanceMetric> inheritance = this.metrics.inheritance();

      heading("Inheritance");
      this.markdown.append("""
          The inheritance depth of a class counts its superclasses, its subclasses only the \
          direct ones. Interfaces and anonymous classes do not count.

          """);

      classes("Deepest Classes", "Depth", inheritance, InheritanceMetric::name,
          InheritanceMetric::depth);
      classes("Most Extended Classes", "Subclasses", inheritance, InheritanceMetric::name,
          InheritanceMetric::subclasses);
    }

    private void coupling() {
      heading("Coupling");
      this.markdown.append("""
          The fan-in of a class counts the classes that depend on it, its fan-out the classes it \
          depends on.

          """);

      classes("Most Used Classes", "Fan-in", this.metrics.classes(), ClassMetric::name,
          ClassMetric::fanIn);
      classes("Classes with the Most Dependencies", "Fan-out", this.metrics.classes(),
          ClassMetric::name, ClassMetric::fanOut);

      if (this.withEntryPoints) {
        entryPoints();
      }
    }

    private void entryPoints() {
      subheading("Entry Points");
      this.markdown.append("""
          Entry points are the methods that a framework calls rather than the measured classes, \
          such as the handler methods of Spring MVC controllers, and a class counts those it \
          inherits as well. They do not count as couplings, as their callers depend on a \
          contract, such as the path and the parameters of a request, and not on the class, \
          which can be renamed, split or moved without changing them. So a class with entry \
          points is used even with a fan-in of 0, and the classes with the lowest fan-in come \
          first, then those with the most entry points. Kinds counts the entry points by their \
          kind, such as their HTTP method, and an entry point with several kinds, such as GET \
          and POST, counts for each of them.

          """);
      table(List.of("Class", "Entry points", "Kinds", "Fan-in"),
          List.of("---", "---:", "---", "---:"),
          this.metrics.entryPoints().stream()
              .sorted(comparingInt(EntryPointMetric::fanIn)
                  .thenComparing(comparingInt(EntryPointMetric::methods).reversed())
                  .thenComparing(EntryPointMetric::name))
              .limit(LISTED_CLASSES)
              .map(metric -> List.of(this.links.className(metric.name()),
                  String.valueOf(metric.methods()),
                  metric.kinds().entrySet().stream()
                      .map(kind -> kind.getKey() + " " + kind.getValue())
                      .collect(joining(", ")),
                  String.valueOf(metric.fanIn())))
              .toList());
    }

    private void cohesion() {
      heading("Cohesion");
      this.markdown.append("""
          The LCOM4 of a class counts the groups of its methods that have nothing in common: \
          methods belong together if they use the same field or method of their object, also an \
          inherited one, or call each other. Lower is better: 1 is best, as all methods work \
          together. A higher value means that the class does as many unrelated things and can be \
          split into as many classes. Constructors, static and abstract methods, getters and \
          setters do not count, nor do methods without a connection to another member of their \
          class. Interfaces, records and anonymous classes are not measured.

          """);

      subheading("Least Cohesive Classes");
      table(List.of("Class", "LCOM4", "Methods"), List.of("---", "---:", "---:"),
          this.metrics.cohesion().stream()
              .filter(metric -> metric.groups() > 1)
              .sorted(comparingInt(CohesionMetric::groups).reversed()
                  .thenComparing(comparingInt(CohesionMetric::methods).reversed())
                  .thenComparing(CohesionMetric::name))
              .limit(LISTED_CLASSES)
              .map(metric -> List.of(this.links.className(metric.name()),
                  String.valueOf(metric.groups()), String.valueOf(metric.methods())))
              .toList());
    }

    /**
     * Lists the classes with the highest positive values.
     */
    private <T> void classes(String title, String column, List<T> metrics,
        Function<T, String> name, ToIntFunction<T> value) {
      subheading(title);
      table(List.of("Class", column), List.of("---", "---:"), metrics.stream()
          .filter(metric -> value.applyAsInt(metric) > 0)
          .sorted(Comparator.<T>comparingInt(value).reversed().thenComparing(name))
          .limit(LISTED_CLASSES)
          .map(metric -> List.of(this.links.className(name.apply(metric)),
              String.valueOf(value.applyAsInt(metric))))
          .toList());
    }

    private long inZone(Zone zone) {
      return this.metrics.packages().stream()
          .filter(metric -> metric.zone().filter(zone::equals).isPresent())
          .count();
    }

    private void image(String description, String link) {
      this.markdown.append("![%s](%s)\n\n".formatted(description, link));
    }

    private void heading(String heading) {
      this.markdown.append("## ").append(heading).append("\n\n");
    }

    private void subheading(String heading) {
      this.markdown.append("### ").append(heading).append("\n\n");
    }

    private void table(List<String> header, List<String> alignment, List<List<String>> rows) {
      if (rows.isEmpty()) {
        this.markdown.append("None.\n\n");
        return;
      }

      this.markdown.append(row(header)).append(row(alignment));
      rows.forEach(row -> this.markdown.append(row(row)));
      this.markdown.append('\n');
    }

    /**
     * Returns a row of a table, with the pipes in the cells escaped and their line breaks replaced
     * by spaces, so that neither ends a cell or the row.
     */
    private static String row(List<String> cells) {
      return cells.stream()
          .map(cell -> LINE_BREAK.matcher(cell.replace("|", "\\|")).replaceAll(" "))
          .collect(joining(" | ", "| ", " |\n"));
    }
  }
}
