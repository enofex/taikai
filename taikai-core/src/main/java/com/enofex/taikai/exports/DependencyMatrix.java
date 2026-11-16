package com.enofex.taikai.exports;

import static java.util.stream.Collectors.joining;

import com.enofex.taikai.exports.ArchitectureMetrics.Library;
import com.enofex.taikai.exports.ArchitectureMetrics.LibraryUsage;
import com.enofex.taikai.exports.ArchitectureMetrics.PackageCycle;
import com.enofex.taikai.exports.ArchitectureMetrics.PackageDependency;
import com.enofex.taikai.exports.ArchitectureMetrics.PackageDependency.Kind;
import com.enofex.taikai.exports.ArchitectureMetrics.PackageMetric;
import com.enofex.taikai.exports.Svg.Legend;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * Draws dependencies as matrices with a row for every package, in the layered order from the bottom
 * up. With more than {@value Grid#MAX_NUMBERED} packages, the cells get smaller and lose their
 * counts.
 */
final class DependencyMatrix {

  static final String PACKAGES_DESCRIPTION = "Dependency structure matrix of the packages";
  static final String LIBRARIES_DESCRIPTION = "Libraries used by the packages";
  private static final String PACKAGES_TITLE = "Dependency matrix";
  private static final String LIBRARIES_TITLE = "Libraries";
  private static final List<Legend> PACKAGES_LEGEND = List.of(
      Legend.square("dependency", "Row uses column"),
      Legend.square("closing", "Closes a cycle"),
      Legend.square("unstable-target", "Uses less stable"),
      Legend.square("group", "Cycle"));
  private static final List<Legend> LIBRARIES_LEGEND = List.of(
      Legend.square("dependency", "Row uses column"));

  private DependencyMatrix() {
  }

  /**
   * Returns the dependency structure matrix of the packages, whose cells above the diagonal close a
   * cycle.
   */
  static String packages(ArchitectureMetrics metrics) {
    if (metrics.packages().isEmpty()) {
      return Svg.empty(PACKAGES_DESCRIPTION, PACKAGES_TITLE, PACKAGES_LEGEND, Svg.NO_PACKAGES);
    }

    Grid grid = Grid.ofPackages(metrics);
    StringBuilder svg = grid.start(PACKAGES_DESCRIPTION, PACKAGES_TITLE, PACKAGES_LEGEND);

    for (int row = 0; row < metrics.packages().size(); row++) {
      svg.append(grid.square(row, row, "class=\"self\""));
    }

    int maxDependencies = metrics.packageDependencies().stream()
        .mapToInt(PackageDependency::dependencies)
        .max()
        .orElse(1);

    for (PackageDependency dependency : metrics.packageDependencies()) {
      svg.append(cell(metrics, grid, dependency, maxDependencies));
    }

    cycles(svg, metrics, grid);

    return Svg.end(svg);
  }

  /**
   * Returns the matrix of the libraries that the packages use, the most used at the left.
   */
  static String libraries(ArchitectureMetrics metrics) {
    List<String> libraries = metrics.libraries().stream().map(Library::name).toList();

    if (libraries.isEmpty()) {
      return Svg.empty(LIBRARIES_DESCRIPTION, LIBRARIES_TITLE, LIBRARIES_LEGEND,
          metrics.packages().isEmpty() ? Svg.NO_PACKAGES : "No libraries");
    }

    Grid grid = Grid.of(metrics, libraries);
    StringBuilder svg = grid.start(LIBRARIES_DESCRIPTION, LIBRARIES_TITLE, LIBRARIES_LEGEND);
    int maxClasses = metrics.libraryUsages().stream()
        .mapToInt(usage -> usage.classes().size())
        .max()
        .orElse(1);

    for (LibraryUsage usage : metrics.libraryUsages()) {
      String packageLabel = metrics.packageLabel(usage.packageName());

      svg.append(grid.cell(grid.row(usage.packageName()), grid.column(usage.library()),
          usage.classes().size(), maxClasses,
          "class=\"library-cell\" data-package=\"%s\" data-library=\"%s\"".formatted(
              Svg.escaped(packageLabel), Svg.escaped(usage.library())),
          tooltip(metrics, packageLabel, usage), null));
    }

    return Svg.end(svg);
  }

  private static String cell(ArchitectureMetrics metrics, Grid grid,
      PackageDependency dependency, int maxDependencies) {
    CellStyle style = CellStyle.of(dependency.kind());
    String origin = metrics.packageLabel(dependency.origin());
    String target = metrics.packageLabel(dependency.target());

    return grid.cell(grid.row(dependency.origin()), grid.row(dependency.target()),
        dependency.dependencies(), maxDependencies,
        "class=\"%s\" data-origin=\"%s\" data-target=\"%s\"".formatted(style.group(),
            Svg.escaped(origin), Svg.escaped(target)),
        Svg.tooltip(origin, target, dependency), style.square());
  }

  private static void cycles(StringBuilder svg, ArchitectureMetrics metrics, Grid grid) {
    for (PackageCycle cycle : metrics.packageCycles()) {
      IntSummaryStatistics rows = cycle.packages().stream()
          .mapToInt(grid::row)
          .summaryStatistics();
      double size = (rows.getMax() - rows.getMin() + 1) * grid.cell();

      svg.append(Svg.rect(grid.x(rows.getMin()), grid.y(rows.getMin()), size, size,
          "rx=\"4\" class=\"group\" stroke-width=\"1.5\""));
    }
  }

  private static String tooltip(ArchitectureMetrics metrics, String packageLabel,
      LibraryUsage usage) {
    return Stream.concat(
            Stream.of("%s uses %s: %s".formatted(packageLabel, usage.library(),
                Svg.classes(usage.classes().size()))),
            usage.classes().stream()
                .limit(MetricsMarkdownExport.LISTED_CLASSES)
                .map(metrics::classLabel))
        .collect(joining("\n"));
  }

  /**
   * The CSS classes of a cell: of its group, and of its square unless that has the blue of its
   * count.
   */
  private record CellStyle(String group, @Nullable String square) {

    static CellStyle of(Kind kind) {
      return switch (kind) {
        case REGULAR -> new CellStyle("dependency-cell", null);
        case ON_LESS_STABLE -> new CellStyle("unstable-cell", "unstable-target");
        case CLOSES_CYCLE -> new CellStyle("closing-cell", "closing");
      };
    }
  }

  /**
   * The grid of both matrices: the row and column headers and the empty cells.
   */
  private static final class Grid {

    private static final int MAX_NUMBERED = 40;
    private static final double CELL = 28;
    private static final double SMALL_CELL = 16;
    private static final double GAP = 2;
    private static final double NUMBER_WIDTH = 26;
    private static final double NUMBER_HEADER_HEIGHT = 22;
    private static final double HEADER_GAP = 8;

    /**
     * The steps 150 to 650 of the blue ramp. Numbers are white from the step 450 on.
     */
    private static final List<String> RAMP = List.of(
        "#b7d3f6", "#86b6ef", "#5598e7", "#2a78d6", "#1c5cab", "#104281");
    private static final int FIRST_WHITE_STEP = 3;

    private final ArchitectureMetrics metrics;
    private final Map<String, Integer> rowOf = new HashMap<>();
    private final List<String> columns;
    private final Map<String, Integer> columnOf;
    private final boolean numbered;
    private final boolean rotatedHeaders;
    private final double cell;
    private final double left;
    private final double top;

    private Grid(ArchitectureMetrics metrics, List<String> columns, boolean namedColumns) {
      this.metrics = metrics;
      this.columns = columns;
      this.columnOf = DependencyGraph.indexes(columns);

      List<String> layeredOrder = metrics.layeredOrder();

      for (int index = 0; index < layeredOrder.size(); index++) {
        this.rowOf.put(layeredOrder.get(index), layeredOrder.size() - 1 - index);
      }

      this.numbered = layeredOrder.size() <= MAX_NUMBERED;
      this.rotatedHeaders = namedColumns || !this.numbered;
      this.cell = this.numbered ? CELL : SMALL_CELL;
      this.left = Svg.PADDING + NUMBER_WIDTH + 14 + 12 + metrics.packages().stream()
          .mapToDouble(metric -> Svg.width(metrics.packageLabel(metric.name())))
          .max()
          .orElse(0);
      this.top = Svg.CONTENT_TOP + (this.rotatedHeaders
          ? 2 * HEADER_GAP + columns.stream().mapToDouble(Svg::width).max().orElse(0)
          : NUMBER_HEADER_HEIGHT);
    }

    static Grid ofPackages(ArchitectureMetrics metrics) {
      return new Grid(metrics, IntStream.rangeClosed(1, metrics.packages().size())
          .mapToObj(String::valueOf)
          .toList(), false);
    }

    static Grid of(ArchitectureMetrics metrics, List<String> columns) {
      return new Grid(metrics, columns, true);
    }

    int row(String packageName) {
      return this.rowOf.get(packageName);
    }

    int column(String name) {
      return this.columnOf.get(name);
    }

    double cell() {
      return this.cell;
    }

    double x(int column) {
      return this.left + column * this.cell;
    }

    double y(int row) {
      return this.top + row * this.cell;
    }

    /**
     * Starts a document with the grid. The caller adds the cells and closes it with
     * {@link Svg#end(StringBuilder)}.
     */
    StringBuilder start(String description, String title, List<Legend> legend) {
      StringBuilder svg = Svg.start(
          Math.max(this.left + this.columns.size() * this.cell + Svg.PADDING,
              Svg.headerWidth(title, legend)),
          this.top + this.rowOf.size() * this.cell + Svg.PADDING, description, title, legend);

      rowHeaders(svg);
      columnHeaders(svg);
      emptyCells(svg);

      return svg;
    }

    /**
     * Returns a filled cell, with its count if the cells are numbered, in the given class or else
     * in the blue of its count.
     */
    String cell(int row, int column, int count, int maxCount, String attributes, String tooltip,
        @Nullable String squareClass) {
      int step = (int) Math.ceil(Math.sqrt((double) count / maxCount) * RAMP.size()) - 1;
      step = Math.max(0, Math.min(RAMP.size() - 1, step));
      String text = squareClass != null || step < FIRST_WHITE_STEP ? "ink" : "on-fill";

      return Svg.group(attributes, tooltip,
          square(row, column, squareClass != null
              ? "class=\"%s\"".formatted(squareClass)
              : "fill=\"%s\"".formatted(RAMP.get(step))),
          this.numbered
              ? Svg.text(x(column) + this.cell / 2, y(row) + this.cell / 2 + 4,
                  "text-anchor=\"middle\" class=\"%s\" font-size=\"11\" font-weight=\"600\""
                      .formatted(text), String.valueOf(count))
              : "");
    }

    String square(int row, int column, String attributes) {
      return Svg.rect(x(column) + GAP / 2, y(row) + GAP / 2, this.cell - GAP, this.cell - GAP,
          "rx=\"3\" " + attributes);
    }

    private void rowHeaders(StringBuilder svg) {
      for (PackageMetric metric : this.metrics.packages()) {
        int row = row(metric.name());
        double baseline = y(row) + this.cell / 2 + 4;

        svg.append(Svg.text(Svg.PADDING + NUMBER_WIDTH - 6, baseline, "end", "muted",
                String.valueOf(row + 1)))
            .append(Svg.circle(Svg.PADDING + NUMBER_WIDTH + 4, baseline - 4, 4.5,
                "class=\"%s\"".formatted(metric.style())))
            .append(Svg.text(Svg.PADDING + NUMBER_WIDTH + 14, baseline, "start", "ink",
                this.metrics.packageLabel(metric.name())));
      }
    }

    private void columnHeaders(StringBuilder svg) {
      for (int column = 0; column < this.columns.size(); column++) {
        double center = x(column) + this.cell / 2;

        svg.append(this.rotatedHeaders
            ? Svg.rotated(center + 4, this.top - HEADER_GAP, "start", "muted",
                this.columns.get(column))
            : Svg.text(center, this.top - HEADER_GAP, "middle", "muted",
                this.columns.get(column)));
      }
    }

    /**
     * Draws the empty cells as a pattern, so that the document only grows with the filled cells.
     */
    private void emptyCells(StringBuilder svg) {
      svg.append(("  <defs><pattern id=\"cells\" x=\"%1$s\" y=\"%2$s\" width=\"%3$s\" "
              + "height=\"%3$s\" patternUnits=\"userSpaceOnUse\"><rect x=\"%4$s\" y=\"%4$s\" "
              + "width=\"%5$s\" height=\"%5$s\" rx=\"3\" class=\"cell\"/></pattern></defs>\n")
              .formatted(Svg.number(this.left), Svg.number(this.top), Svg.number(this.cell),
                  Svg.number(GAP / 2), Svg.number(this.cell - GAP)))
          .append(Svg.rect(this.left, this.top, this.columns.size() * this.cell,
              this.rowOf.size() * this.cell, "fill=\"url(#cells)\""));
    }
  }
}
