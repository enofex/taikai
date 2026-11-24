package com.enofex.taikai.exports;

import static com.enofex.taikai.exports.Svg.line;
import static com.tngtech.archunit.core.domain.Formatters.ensureSimpleName;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import com.enofex.taikai.exports.ArchitectureMetrics.ClassMetric;
import com.enofex.taikai.exports.Svg.Box;
import com.enofex.taikai.exports.Svg.Circle;
import com.enofex.taikai.exports.Svg.Dot;
import com.enofex.taikai.exports.Svg.Label;
import com.enofex.taikai.exports.Svg.Legend;
import com.enofex.taikai.exports.Svg.Room;
import com.enofex.taikai.exports.Svg.SquarePlot;
import com.enofex.taikai.exports.Svg.SquarePlot.Axis;
import com.enofex.taikai.exports.Svg.SquarePlot.Tick;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Draws the fan-in and fan-out of the classes as a density map, on logarithmic scales that start
 * at 0. A cell counts the classes with a fan-in and a fan-out from its ticks up to just below the
 * next ones, which step by 1, 2 and 5: 0, 1, 2 to 4, 5 to 9 and so on. The hubs are dots on top,
 * in the middle of the unit of their fan-in and fan-out, as are the dashed lines through the points
 * of the same fan-in × fan-out: 10, 100 and so on, which run below the counts. A count moves from
 * the middle of its cell to its top or bottom if a hub covers it, and the labels of the hubs, and
 * then those of the lines, keep clear of the counts. A count, a hub or a line without room for its
 * label goes without it.
 */
final class CouplingChart {

  static final String DESCRIPTION = "Fan-in and fan-out of the classes";

  private static final String TITLE = "Coupling map";
  private static final List<Legend> LEGEND = List.of(
      Legend.square("balanced", "Classes"),
      Legend.dot("balanced", "Hub"),
      Legend.line("isoline", "Same fan-in \u00d7 fan-out"));
  private static final Axis FAN_IN =
      new Axis("\u2190 used by few", "Fan-in", "used by many \u2192");
  private static final Axis FAN_OUT =
      new Axis("\u2190 depends on few", "Fan-out", "depends on many \u2192");
  private static final double HUB_RADIUS = 5;

  /**
   * The least distance of a hub from the edges of the plot, so that it stays inside its rounded
   * corners, its ring included.
   */
  private static final double HUB_INSET = HUB_RADIUS + 2;
  private static final int[] TICK_STEPS = {1, 2, 5};
  private static final int ISOLINE_POINTS = 48;

  private CouplingChart() {
  }

  static String svg(ArchitectureMetrics metrics) {
    if (metrics.classes().isEmpty()) {
      return Svg.empty(SquarePlot.WIDTH, DESCRIPTION, TITLE, LEGEND, "No classes");
    }

    Scale scale = new Scale(ticks(metrics.classes().stream()
        .mapToInt(metric -> Math.max(metric.fanIn(), metric.fanOut()))
        .max()
        .orElse(0)));
    StringBuilder svg = Svg.start(SquarePlot.WIDTH, SquarePlot.HEIGHT, DESCRIPTION, TITLE,
        LEGEND);
    Room room = new Room(SquarePlot.LABEL_AREA);
    List<Cell> cells = cells(scale, metrics);
    List<Isoline> isolines = isolines(scale);
    List<Dot> hubs = hubs(metrics, scale);

    // the counts make way for the hubs, and the labels for both
    hubs.forEach(hub -> room.take(hub.circle().box()));
    plot(svg, scale);
    cells.forEach(cell -> svg.append(cell.square()));
    isolines.forEach(isoline -> svg.append(isoline.polyline()));
    cells.forEach(cell -> cell.count(room).ifPresent(svg::append));
    Svg.dots(svg, hubs, room);
    isolines.forEach(isoline -> room.place(isoline.labels())
        .ifPresent(label -> svg.append(label.text("muted halo"))));
    SquarePlot.axes(svg, scale.ticks().stream()
        .map(tick -> new Tick(scale.share(tick), String.valueOf(tick)))
        .toList(), FAN_IN, FAN_OUT);

    return Svg.end(svg);
  }

  /**
   * Returns 0 and the steps 1, 2, 5, 10, 20, 50 and so on up to the first above the maximum, so
   * that every value is below the last tick.
   */
  static List<Integer> ticks(int max) {
    List<Integer> ticks = new ArrayList<>(List.of(0));

    for (int magnitude = 1; ; magnitude *= 10) {
      for (int step : TICK_STEPS) {
        ticks.add(step * magnitude);

        if (step * magnitude > max) {
          return ticks;
        }
      }
    }
  }

  private static void plot(StringBuilder svg, Scale scale) {
    svg.append(SquarePlot.plane());

    for (int index = 1; index < scale.ticks().size() - 1; index++) {
      svg.append(line(scale.tickX(index), SquarePlot.TOP, scale.tickX(index),
              SquarePlot.TOP + SquarePlot.SIZE, "class=\"grid\""))
          .append(line(SquarePlot.LEFT, scale.tickY(index), SquarePlot.LEFT + SquarePlot.SIZE,
              scale.tickY(index), "class=\"grid\""));
    }
  }

  /**
   * Returns a cell for every range of fan-in and fan-out with classes, in the blue of its count.
   */
  private static List<Cell> cells(Scale scale, ArchitectureMetrics metrics) {
    Map<Position, List<ClassMetric>> classesOf = new TreeMap<>(
        comparingInt(Position::column).thenComparingInt(Position::row));
    metrics.classes().forEach(metric -> classesOf.computeIfAbsent(
            new Position(scale.range(metric.fanIn()), scale.range(metric.fanOut())),
            position -> new ArrayList<>())
        .add(metric));
    int maxClasses = classesOf.values().stream().mapToInt(List::size).max().orElse(1);

    return classesOf.entrySet().stream()
        .map(entry -> {
          Position position = entry.getKey();
          List<ClassMetric> classes = entry.getValue();
          double x = scale.tickX(position.column());
          double y = scale.tickY(position.row() + 1);

          return new Cell(x, y, scale.tickX(position.column() + 1) - x,
              scale.tickY(position.row()) - y, classes.size(),
              Svg.tooltip("Fan-in %s, fan-out %s: %s".formatted(scale.label(position.column()),
                      scale.label(position.row()), Svg.classes(classes.size())),
                  classes.stream().map(metric -> metrics.classLabel(metric.name()))),
              Svg.Shade.of(classes.size(), maxClasses));
        })
        .toList();
  }

  /**
   * Returns the lines through the points with the same fan-in × fan-out, for every power of ten
   * below the corner of the plot.
   */
  private static List<Isoline> isolines(Scale scale) {
    double last = scale.last();

    return LongStream.iterate(10, product -> product < last * last, product -> product * 10)
        .mapToObj(product -> {
          double from = product / last;
          double ratio = last / from;

          return new Isoline(product, IntStream.rangeClosed(0, ISOLINE_POINTS)
              .mapToDouble(point -> from * Math.pow(ratio, (double) point / ISOLINE_POINTS))
              .mapToObj(fanIn -> new Point(scale.x(fanIn), scale.y(product / fanIn)))
              .toList());
        })
        .toList();
  }

  /**
   * Returns a dot for the listed hubs of every fan-in and fan-out, the highest fan-in × fan-out
   * first, so that they are labeled first.
   */
  private static List<Dot> hubs(ArchitectureMetrics metrics, Scale scale) {
    return metrics.hubs().stream()
        .limit(MetricsMarkdownExport.LISTED_CLASSES)
        .collect(groupingBy(metric -> List.of(metric.fanIn(), metric.fanOut()),
            LinkedHashMap::new, toList()))
        .values().stream()
        .map(hubs -> new Dot(
            Svg.names(hubs.stream().map(metric -> ensureSimpleName(metric.name())).toList(),
                Svg.classes(hubs.size())),
            hubs.stream()
                .map(metric -> "%s: fan-in %s, fan-out %s".formatted(
                    metrics.classLabel(metric.name()), metric.fanIn(), metric.fanOut()))
                .collect(joining("\n")),
            "class=\"hub\"", "class=\"balanced ring\" stroke-width=\"2\"",
            new Circle(
                inside(scale.x(hubs.get(0).fanIn()), SquarePlot.LEFT),
                inside(scale.y(hubs.get(0).fanOut()), SquarePlot.TOP),
                HUB_RADIUS)))
        .toList();
  }

  /**
   * Returns the position of a hub moved inside the plot that starts at {@code start}.
   */
  private static double inside(double position, double start) {
    return Math.max(start + HUB_INSET, Math.min(start + SquarePlot.SIZE - HUB_INSET, position));
  }

  /**
   * The position of a range of fan-in, its column, and a range of fan-out, its row.
   */
  private record Position(int column, int row) {

  }

  /**
   * A cell of the classes in a range of fan-in and fan-out, within its ticks.
   */
  private record Cell(double x, double y, double width, double height, int classes,
      String tooltip, Svg.Shade shade) {

    String square() {
      return Svg.group("class=\"density-cell\"", this.tooltip,
          Svg.cell(this.x, this.y, this.width, this.height,
              "fill=\"%s\"".formatted(this.shade.fill())));
    }

    /**
     * Returns the count in the middle of the cell, or else at its top or its bottom, wherever it is
     * free, if it fits at all.
     */
    Optional<String> count(Room room) {
      double center = this.x + this.width / 2;
      double top = this.y + Svg.CELL_GAP / 2;
      double inner = this.height - Svg.CELL_GAP;
      Box box = Svg.countBox(center, 0, this.classes);

      if (box.x2() - box.x1() > this.width - Svg.CELL_GAP - 4 || inner < 14) {
        return Optional.empty();
      }

      return room.place(Stream.of(this.y + this.height / 2 + 4, top + 11, top + inner - 3),
              baseline -> Svg.countBox(center, baseline, this.classes))
          .map(baseline -> Svg.count(center, baseline, this.shade.text(), this.classes));
    }
  }

  private record Point(double x, double y) {

  }

  /**
   * The line through the points with the same fan-in × fan-out.
   */
  private record Isoline(long product, List<Point> points) {

    String polyline() {
      return "  <polyline points=\"%s\" class=\"isoline\" stroke-width=\"1.5\"/>\n".formatted(
          this.points.stream()
              .map(point -> Svg.point(point.x(), point.y()))
              .collect(joining(" ")));
    }

    /**
     * Returns the positions of the label on the line, from its middle, where fan-in and fan-out
     * are equal, outwards.
     */
    Stream<Label> labels() {
      int middle = ISOLINE_POINTS / 2;

      return IntStream.rangeClosed(0, ISOLINE_POINTS)
          .map(step -> step % 2 == 0 ? middle + step / 2 : middle - (step + 1) / 2)
          .mapToObj(this.points::get)
          .map(point -> Label.of(String.valueOf(this.product), point.x(), point.y() + 4,
              "middle"));
    }
  }

  /**
   * The logarithmic scale of both axes, from 0 to the last tick. The ticks are the edges of the
   * cells, and a value lies half a unit above its tick, in the middle of its unit.
   */
  private record Scale(List<Integer> ticks) {

    private static final double HALF_UNIT = 0.5;

    /**
     * Returns the value at the end of both axes, half a unit below the last tick.
     */
    double last() {
      return this.ticks.get(this.ticks.size() - 1) - HALF_UNIT;
    }

    /**
     * Returns the index of the range from a tick up to just below the next one.
     */
    int range(int value) {
      int range = 0;

      while (this.ticks.get(range + 1) <= value) {
        range++;
      }

      return range;
    }

    /**
     * Returns the range as its only value or its first and last value, such as {@code 2–4}.
     */
    String label(int range) {
      int first = this.ticks.get(range);
      int last = this.ticks.get(range + 1) - 1;

      return first == last ? String.valueOf(first) : first + "\u2013" + last;
    }

    double x(double fanIn) {
      return SquarePlot.x(share(fanIn + HALF_UNIT));
    }

    double y(double fanOut) {
      return SquarePlot.y(share(fanOut + HALF_UNIT));
    }

    double tickX(int index) {
      return SquarePlot.x(share(this.ticks.get(index)));
    }

    double tickY(int index) {
      return SquarePlot.y(share(this.ticks.get(index)));
    }

    double share(double value) {
      return Math.log1p(value) / Math.log1p(this.ticks.get(this.ticks.size() - 1));
    }
  }
}
