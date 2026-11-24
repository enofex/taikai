package com.enofex.taikai.exports;

import static com.enofex.taikai.exports.Svg.SquarePlot.x;
import static com.enofex.taikai.exports.Svg.SquarePlot.y;
import static com.enofex.taikai.exports.Svg.line;
import static com.enofex.taikai.exports.Svg.number;
import static com.enofex.taikai.exports.Svg.text;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import com.enofex.taikai.exports.ArchitectureMetrics.PackageMetric;
import com.enofex.taikai.exports.Svg.Box;
import com.enofex.taikai.exports.Svg.Circle;
import com.enofex.taikai.exports.Svg.Label;
import com.enofex.taikai.exports.Svg.Room;
import com.enofex.taikai.exports.Svg.SquarePlot;
import com.enofex.taikai.exports.Svg.SquarePlot.Axis;
import com.enofex.taikai.exports.Svg.SquarePlot.Tick;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * Draws every coupled package as a dot of its abstractness and instability, with the main sequence
 * and the zones. Packages with the same rounded values share a dot, and a dot without room for its
 * label goes without it.
 */
final class MainSequenceChart {

  static final String DESCRIPTION = "Abstractness and instability of the packages";

  private static final double MIN_RADIUS = 5;
  private static final double MAX_RADIUS = 14;
  private static final List<Tick> TICKS = Stream.of(0.0, 0.25, 0.5, 0.75, 1.0)
      .map(share -> new Tick(share, number(share)))
      .toList();
  private static final Axis INSTABILITY =
      new Axis("\u2190 stable", "Instability (I)", "unstable \u2192");
  private static final Axis ABSTRACTNESS =
      new Axis("\u2190 concrete", "Abstractness (A)", "abstract \u2192");

  /**
   * How far the zones reach from their corners.
   */
  private static final double ZONE = 1 - ArchitectureMetrics.ZONE_DISTANCE;
  private static final Label PAIN_CAPTION = Label.of("Zone of pain", x(0.02), y(0.04), "start");
  private static final Label USELESSNESS_CAPTION =
      Label.of("Zone of uselessness", x(0.98), y(0.95), "end");
  private static final double SEQUENCE_LABEL_X = x(0.36) + 8;
  private static final double SEQUENCE_LABEL_Y = y(0.64) - 8;
  private static final Box SEQUENCE_LABEL_BOX = new Box(SEQUENCE_LABEL_X - 32,
      SEQUENCE_LABEL_Y - 32, SEQUENCE_LABEL_X + 32, SEQUENCE_LABEL_Y + 32);
  private static final String MAIN_SEQUENCE_LABEL = "Main sequence";

  private MainSequenceChart() {
  }

  static String svg(ArchitectureMetrics metrics) {
    StringBuilder svg = Svg.start(SquarePlot.WIDTH, SquarePlot.HEIGHT, DESCRIPTION,
        MAIN_SEQUENCE_LABEL, Svg.ZONE_LEGEND.subList(0, 3));

    definitions(svg);
    plot(svg);
    SquarePlot.axes(svg, TICKS, INSTABILITY, ABSTRACTNESS);
    dots(svg, metrics);

    return Svg.end(svg);
  }

  private static void definitions(StringBuilder svg) {
    svg.append("  <defs>\n")
        .append(("    <clipPath id=\"plot\"><rect x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" "
            + "rx=\"8\"/></clipPath>\n").formatted(number(SquarePlot.LEFT),
            number(SquarePlot.TOP), number(SquarePlot.SIZE), number(SquarePlot.SIZE)))
        .append(gradient("pain-area", Svg.PAIN_COLOR, x(0), y(0), x(ZONE / 2), y(ZONE / 2)))
        .append(gradient("uselessness-area", Svg.USELESSNESS_COLOR, x(1), y(1), x(1 - ZONE / 2),
            y(1 - ZONE / 2)))
        .append("  </defs>\n");
  }

  private static String gradient(String id, String color, double x1, double y1, double x2,
      double y2) {
    return ("    <linearGradient id=\"%s\" gradientUnits=\"userSpaceOnUse\" x1=\"%s\" y1=\"%s\" "
        + "x2=\"%s\" y2=\"%s\"><stop offset=\"0\" stop-color=\"%s\" stop-opacity=\"0.24\"/>"
        + "<stop offset=\"1\" stop-color=\"%s\" stop-opacity=\"0.05\"/></linearGradient>\n")
        .formatted(id, number(x1), number(y1), number(x2), number(y2), color, color);
  }

  private static void plot(StringBuilder svg) {
    svg.append(SquarePlot.plane())
        .append("  <g clip-path=\"url(#plot)\">\n")
        .append("    <polygon points=\"%s %s %s\" fill=\"url(#pain-area)\"/>\n".formatted(
            point(0, 0), point(ZONE, 0), point(0, ZONE)))
        .append("    <polygon points=\"%s %s %s\" fill=\"url(#uselessness-area)\"/>\n".formatted(
            point(1, 1), point(1 - ZONE, 1), point(1, 1 - ZONE)));

    for (Tick tick : TICKS.subList(1, TICKS.size() - 1)) {
      double share = tick.share();

      svg.append("  ").append(line(x(share), y(0), x(share), y(1), "class=\"grid\""))
          .append("  ").append(line(x(0), y(share), x(1), y(share), "class=\"grid\""));
    }

    svg.append("  ").append(line(x(0), y(1), x(1), y(0),
            "class=\"sequence\" stroke-width=\"1.5\" stroke-dasharray=\"6 5\""))
        .append("  </g>\n")
        .append(PAIN_CAPTION.text("ink-2"))
        .append(USELESSNESS_CAPTION.text("ink-2"))
        .append(text(SEQUENCE_LABEL_X, SEQUENCE_LABEL_Y,
            "text-anchor=\"middle\" class=\"ink-2 halo\" transform=\"rotate(45 %s %s)\"".formatted(
                number(SEQUENCE_LABEL_X), number(SEQUENCE_LABEL_Y)), MAIN_SEQUENCE_LABEL));
  }

  private static void dots(StringBuilder svg, ArchitectureMetrics metrics) {
    List<Dot> dots = metrics.packages().stream()
        .filter(metric -> metric.instability().isPresent())
        .collect(groupingBy(MainSequenceChart::position, LinkedHashMap::new, toList()))
        .values().stream()
        .map(group -> Dot.of(group, metrics))
        .sorted(comparingInt(Dot::classes).reversed().thenComparing(Dot::label))
        .toList();
    int maxClasses = dots.stream().mapToInt(Dot::classes).max().orElse(1);

    Svg.dots(svg, dots.stream()
            .map(dot -> new Svg.Dot(dot.label(), dot.title(),
                "class=\"dot\" data-zone=\"%s\"".formatted(dot.style()),
                "class=\"%s ring\" fill-opacity=\"0.9\" stroke-width=\"2\"".formatted(
                    dot.style()),
                new Circle(x(dot.instability()), y(dot.abstractness()),
                    radius(dot.classes(), maxClasses))))
            .toList(),
        new Room(SquarePlot.LABEL_AREA, PAIN_CAPTION.box(), USELESSNESS_CAPTION.box(),
            SEQUENCE_LABEL_BOX));
  }

  /**
   * Returns the radius of a dot of {@code classes} of at most {@code maxClasses} classes, so that
   * its area grows with its classes.
   */
  private static double radius(int classes, int maxClasses) {
    return MIN_RADIUS + (MAX_RADIUS - MIN_RADIUS) * Math.sqrt((double) classes / maxClasses);
  }

  private static String position(PackageMetric metric) {
    return ArchitectureMetrics.formatted(metric.instability().getAsDouble()) + " "
        + ArchitectureMetrics.formatted(metric.abstractness());
  }

  private static String point(double instability, double abstractness) {
    return Svg.point(x(instability), y(abstractness));
  }

  /**
   * A dot of the packages with the same rounded instability and abstractness.
   */
  private record Dot(String label, String title, String style, int classes, double instability,
      double abstractness) {

    static Dot of(List<PackageMetric> packages, ArchitectureMetrics metrics) {
      PackageMetric first = packages.get(0);

      return new Dot(
          Svg.names(packages.stream().map(metric -> metrics.packageLabel(metric.name())).toList(),
              packages.size() + " packages"),
          packages.stream()
              .map(metric -> "%s: I = %s, A = %s, D = %s, %s".formatted(
                  metrics.packageLabel(metric.name()),
                  ArchitectureMetrics.formatted(metric.instability()),
                  ArchitectureMetrics.formatted(metric.abstractness()),
                  ArchitectureMetrics.formatted(metric.distance()),
                  Svg.classes(metric.classes())))
              .collect(joining("\n")),
          first.style(),
          packages.stream().mapToInt(PackageMetric::classes).sum(),
          ArchitectureMetrics.rounded(first.instability().getAsDouble()),
          ArchitectureMetrics.rounded(first.abstractness()));
    }
  }
}
