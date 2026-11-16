package com.enofex.taikai.exports;

import static com.enofex.taikai.exports.Svg.line;
import static com.enofex.taikai.exports.Svg.number;
import static com.enofex.taikai.exports.Svg.rotated;
import static com.enofex.taikai.exports.Svg.text;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import com.enofex.taikai.exports.ArchitectureMetrics.PackageMetric;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * Draws the abstractness and instability of the packages coupled to others as dots, sized by their
 * classes, with the main sequence and the zones of pain and uselessness. Packages with the same
 * rounded values share a dot, and a dot without room for its label goes without it.
 */
final class MainSequenceChart {

  static final String DESCRIPTION = "Abstractness and instability of the packages";

  private static final double WIDTH = 644;
  private static final double HEIGHT = 670;
  private static final double LEFT = 92;
  private static final double TOP = Svg.CONTENT_TOP;
  private static final double SIZE = 520;
  private static final double MIN_RADIUS = 5;
  private static final double MAX_RADIUS = 14;
  private static final int MAX_NAMES = 3;
  private static final double[] TICKS = {0, 0.25, 0.5, 0.75, 1};

  /**
   * How far the zones reach along the axes from their corners.
   */
  private static final double ZONE = 1 - ArchitectureMetrics.ZONE_DISTANCE;
  private static final double LINE_HEIGHT = 16;
  private static final int MAX_LABEL_LINES = 16;
  private static final Label PAIN_CAPTION = Label.of("Zone of pain", x(0.02), y(0.04), "start");
  private static final Label USELESSNESS_CAPTION =
      Label.of("Zone of uselessness", x(0.98), y(0.95), "end");
  private static final double SEQUENCE_LABEL_X = x(0.36) + 8;
  private static final double SEQUENCE_LABEL_Y = y(0.64) - 8;
  private static final Box SEQUENCE_LABEL_BOX = new Box(SEQUENCE_LABEL_X - 32,
      SEQUENCE_LABEL_Y - 32, SEQUENCE_LABEL_X + 32, SEQUENCE_LABEL_Y + 32);
  private static final String MAIN_SEQUENCE_LABEL = "Main sequence";
  private static final Box PLOT = new Box(LEFT + 2, TOP + 2, LEFT + SIZE - 2, TOP + SIZE - 2);

  private MainSequenceChart() {
  }

  /**
   * Returns the chart, which leaves out the packages without couplings and their style in the
   * legend.
   */
  static String svg(ArchitectureMetrics metrics) {
    StringBuilder svg = Svg.start(WIDTH, HEIGHT, DESCRIPTION, MAIN_SEQUENCE_LABEL,
        Svg.ZONE_LEGEND.subList(0, 3));

    definitions(svg);
    plot(svg);
    axes(svg);
    dots(svg, metrics);

    return Svg.end(svg);
  }

  /**
   * Defines the clip of the plot and the gradients of the zones, which fade towards the main
   * sequence.
   */
  private static void definitions(StringBuilder svg) {
    svg.append("  <defs>\n")
        .append(("    <clipPath id=\"plot\"><rect x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" "
            + "rx=\"8\"/></clipPath>\n").formatted(number(LEFT), number(TOP), number(SIZE),
            number(SIZE)))
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

  /**
   * Draws the plot: a rounded plane with the zones, the grid and the main sequence.
   */
  private static void plot(StringBuilder svg) {
    svg.append(Svg.rect(LEFT, TOP, SIZE, SIZE, "rx=\"8\" class=\"plane\""))
        .append("  <g clip-path=\"url(#plot)\">\n")
        .append("    <polygon points=\"%s %s %s\" fill=\"url(#pain-area)\"/>\n".formatted(
            point(0, 0), point(ZONE, 0), point(0, ZONE)))
        .append("    <polygon points=\"%s %s %s\" fill=\"url(#uselessness-area)\"/>\n".formatted(
            point(1, 1), point(1 - ZONE, 1), point(1, 1 - ZONE)));

    for (double tick : TICKS) {
      if (tick > 0 && tick < 1) {
        svg.append("  ").append(line(x(tick), y(0), x(tick), y(1), "class=\"grid\""))
            .append("  ").append(line(x(0), y(tick), x(1), y(tick), "class=\"grid\""));
      }
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

  /**
   * Draws the ticks and the titles of the axes, with what their ends mean.
   */
  private static void axes(StringBuilder svg) {
    double titleY = y(0) + MAX_RADIUS + 40;
    double titleX = Svg.PADDING + 10;

    for (double tick : TICKS) {
      svg.append(text(x(tick), y(0) + MAX_RADIUS + 16, "middle", "muted", number(tick)))
          .append(text(x(0) - MAX_RADIUS - 6, y(tick) + 4, "end", "muted", number(tick)));
    }

    svg.append(text(x(0), titleY, "start", "muted", "\u2190 stable"))
        .append(text(x(0.5), titleY, "middle", "ink-2", "Instability (I)"))
        .append(text(x(1), titleY, "end", "muted", "unstable \u2192"))
        .append(rotated(titleX, y(0), "start", "muted", "\u2190 concrete"))
        .append(rotated(titleX, y(0.5), "middle", "ink-2", "Abstractness (A)"))
        .append(rotated(titleX, y(1), "end", "muted", "abstract \u2192"));
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
    List<Circle> circles = dots.stream()
        .map(dot -> new Circle(x(dot.instability()), y(dot.abstractness()), MIN_RADIUS
            + (MAX_RADIUS - MIN_RADIUS) * Math.sqrt((double) dot.classes() / maxClasses)))
        .toList();
    List<Box> obstacles = new ArrayList<>(List.of(
        PAIN_CAPTION.box(), USELESSNESS_CAPTION.box(), SEQUENCE_LABEL_BOX));
    circles.forEach(circle -> obstacles.add(circle.box()));

    for (int index = 0; index < dots.size(); index++) {
      Dot dot = dots.get(index);
      Circle circle = circles.get(index);
      Optional<Label> label = label(dot.label(), circle, obstacles);
      label.ifPresent(free -> obstacles.add(free.box()));

      svg.append(Svg.group("class=\"dot\" data-zone=\"%s\"".formatted(dot.style()), dot.title(),
          label.filter(free -> free.isDetached(circle))
              .map(free -> line(circle.cx(), circle.cy(), free.leaderX(circle),
                  free.leaderY(circle), "class=\"leader\""))
              .orElse(""),
          Svg.circle(circle.cx(), circle.cy(), circle.radius(),
              "class=\"%s ring\" fill-opacity=\"0.9\" stroke-width=\"2\"".formatted(dot.style())),
          label.map(free -> text(free.x(), free.y(),
                  "text-anchor=\"%s\" class=\"ink halo\" font-weight=\"500\"".formatted(
                      free.anchor()), free.content()))
              .orElse("")));
    }
  }

  /**
   * Places the label of a dot at the first free position: next to it, above or below it, or further
   * away with a line, or nowhere, as a label on top of others could not be read.
   */
  private static Optional<Label> label(String text, Circle circle, List<Box> obstacles) {
    List<Label> candidates = new ArrayList<>(List.of(
        Label.of(text, circle.cx() + circle.radius() + 4, circle.cy() + 4, "start"),
        Label.of(text, circle.cx() - circle.radius() - 4, circle.cy() + 4, "end")));

    for (int line = 0; line < MAX_LABEL_LINES; line++) {
      double above = circle.cy() - circle.radius() - 6 - line * LINE_HEIGHT;
      double below = circle.cy() + circle.radius() + 15 + line * LINE_HEIGHT;

      for (double y : new double[]{above, below}) {
        candidates.add(Label.of(text, circle.cx(), y, "middle"));

        if (line > 0) {
          candidates.add(Label.of(text, circle.cx() - 4, y, "end"));
          candidates.add(Label.of(text, circle.cx() + 4, y, "start"));
        }
      }
    }

    return candidates.stream()
        .filter(candidate -> candidate.box().isInside(PLOT))
        .filter(candidate -> obstacles.stream().noneMatch(candidate.box()::overlaps))
        .findFirst();
  }

  private static String position(PackageMetric metric) {
    return ArchitectureMetrics.formatted(metric.instability().getAsDouble()) + " "
        + ArchitectureMetrics.formatted(metric.abstractness());
  }

  private static String point(double instability, double abstractness) {
    return number(x(instability)) + "," + number(y(abstractness));
  }

  private static double x(double instability) {
    return LEFT + instability * SIZE;
  }

  private static double y(double abstractness) {
    return TOP + (1 - abstractness) * SIZE;
  }

  /**
   * A rectangle that a label or dot covers.
   */
  private record Box(double x1, double y1, double x2, double y2) {

    boolean overlaps(Box other) {
      return this.x1 < other.x2 + 1 && other.x1 < this.x2 + 1
          && this.y1 < other.y2 + 1 && other.y1 < this.y2 + 1;
    }

    boolean isInside(Box other) {
      return this.x1 >= other.x1 && this.x2 <= other.x2
          && this.y1 >= other.y1 && this.y2 <= other.y2;
    }
  }

  private record Circle(double cx, double cy, double radius) {

    Box box() {
      return new Box(this.cx - this.radius, this.cy - this.radius, this.cx + this.radius,
          this.cy + this.radius);
    }
  }

  /**
   * A label at the baseline {@code y}, with the width estimated from its number of characters.
   */
  private record Label(String content, double x, double y, String anchor, Box box) {

    static Label of(String content, double x, double y, String anchor) {
      double width = Svg.width(content);
      double left = switch (anchor) {
        case "end" -> x - width;
        case "middle" -> x - width / 2;
        default -> x;
      };

      return new Label(content, x, y, anchor, new Box(left, y - 11, left + width, y + 3));
    }

    String text(String style) {
      return Svg.text(this.x, this.y, this.anchor, style, this.content);
    }

    /**
     * Returns whether a line connects the label to its dot.
     */
    boolean isDetached(Circle circle) {
      return this.box.y2() < circle.cy() - circle.radius() - 4
          || this.box.y1() > circle.cy() + circle.radius() + 5;
    }

    double leaderX(Circle circle) {
      return Math.max(this.box.x1(), Math.min(this.box.x2(), circle.cx()));
    }

    double leaderY(Circle circle) {
      return this.box.y2() < circle.cy() ? this.box.y2() + 1 : this.box.y1() - 1;
    }
  }

  /**
   * A dot of one or more packages with the same rounded instability and abstractness.
   */
  private record Dot(String label, String title, String style, int classes, double instability,
      double abstractness) {

    /**
     * Returns the dot of the given packages, which are sorted by name.
     */
    static Dot of(List<PackageMetric> packages, ArchitectureMetrics metrics) {
      PackageMetric first = packages.get(0);

      return new Dot(
          packages.size() <= MAX_NAMES
              ? packages.stream().map(metric -> metrics.packageLabel(metric.name()))
                  .collect(joining(", "))
              : packages.size() + " packages",
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
