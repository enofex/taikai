package com.enofex.taikai.exports;

import static java.util.stream.Collectors.joining;

import com.enofex.taikai.exports.ArchitectureMetrics.PackageDependency;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The look that the charts share: a card with a title and a legend, colors checked for color vision
 * deficiencies and contrast, cells with counts, a square plot, and dots with labels that do not
 * overlap. The charts have no scripts, so that they work wherever Markdown shows images.
 */
final class Svg {

  static final double PADDING = 24;

  private static final String BALANCED_COLOR = "#2a78d6";

  /**
   * Also for dependencies that close a cycle.
   */
  static final String PAIN_COLOR = "#d03b3b";

  /**
   * Also for dependencies on less stable packages.
   */
  static final String USELESSNESS_COLOR = "#c98500";

  static final List<Legend> ZONE_LEGEND = List.of(
      Legend.dot("balanced", "Balanced"),
      Legend.dot("pain", "Zone of pain"),
      Legend.dot("uselessness", "Zone of uselessness"),
      Legend.dot("isolated", "No couplings"));

  static final String NO_PACKAGES = "No packages";

  private static final double HEADER_BASELINE = 40;

  static final double CONTENT_TOP = 72;

  private static final double CHAR_WIDTH = 6.8 / 12;
  private static final double BOLD_CHAR_WIDTH = 0.6;
  private static final double TITLE_CHAR_WIDTH = 8.6;
  private static final double LEGEND_GAP = 18;
  private static final double LEGEND_MARKER = 14;

  /**
   * The steps 150 to 650 of the blue ramp for counts. Numbers are white from the step 450 on.
   */
  private static final List<String> RAMP = List.of(
      "#b7d3f6", "#86b6ef", "#5598e7", "#2a78d6", "#1c5cab", "#104281");
  private static final int FIRST_WHITE_STEP = 3;

  /**
   * The space between two cells.
   */
  static final double CELL_GAP = 2;
  private static final double COUNT_FONT_SIZE = 11;

  /**
   * The most names that a label shows before it shows their number.
   */
  private static final int MAX_NAMES = 3;
  private static final double LABEL_LINE_HEIGHT = 16;
  private static final int MAX_LABEL_LINES = 16;

  private static final Pattern TRAILING_ZEROS = Pattern.compile("\\.?0+$");
  private static final String STYLE = """
        .card { fill: #ffffff; stroke: #e1e0d9; }
        .plane { fill: #f7f7f5; }
        .ink { fill: #0b0b0b; }
        .ink-2 { fill: #52514e; }
        .muted { fill: #898781; }
        .grid { stroke: #e6e5df; }
        .sequence { stroke: #52514e; }
        .leader { stroke: #b4b3ac; }
        .isoline { fill: none; stroke: #b4b3ac; stroke-dasharray: 4 3; }
        .halo { paint-order: stroke; stroke: #f7f7f5; stroke-width: 3px; stroke-linejoin: round; }
        .ring { stroke: #f7f7f5; }
        .node { fill: #ffffff; stroke: #d9d8d1; }
        .edge { fill: none; stroke: #898781; stroke-opacity: 0.75; }
        .cycle { fill: none; stroke: %2$s; }
        .unstable { fill: none; stroke: %3$s; }
        .edge-head { fill: #898781; }
        .cycle-head { fill: %2$s; }
        .unstable-head { fill: %3$s; }
        .balanced { fill: %1$s; }
        .pain { fill: %2$s; }
        .uselessness { fill: %3$s; }
        .isolated { fill: #898781; }
        .on-fill { fill: #ffffff; }
        .cell { fill: #f7f7f5; }
        .self { fill: #e6e5df; }
        .dependency { fill: %1$s; }
        .closing { fill: #f7d4d4; stroke: %2$s; }
        .unstable-target { fill: #f4e7cc; stroke: %3$s; }
        .group { fill: none; stroke: %2$s; }
      """.formatted(BALANCED_COLOR, PAIN_COLOR, USELESSNESS_COLOR);

  private Svg() {
  }

  /**
   * Starts a document with the card, the title and the legend. The caller adds the content and
   * closes it with {@link #end(StringBuilder)}.
   */
  static StringBuilder start(double width, double height, String description, String title,
      List<Legend> legend) {
    StringBuilder svg = new StringBuilder()
        .append(("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%1$s\" height=\"%2$s\" "
            + "viewBox=\"0 0 %1$s %2$s\" font-family=\"system-ui, -apple-system, 'Segoe UI', "
            + "Helvetica, Arial, sans-serif\" font-size=\"12\">\n").formatted(number(width),
            number(height)))
        .append("  <title>%s</title>\n".formatted(escaped(description)))
        .append("  <style>\n").append(STYLE).append("  </style>\n")
        .append(rect(0.5, 0.5, width - 1, height - 1, "rx=\"12\" class=\"card\""));

    header(svg, width, title, legend);
    return svg;
  }

  static String end(StringBuilder svg) {
    return svg.append("</svg>\n").toString();
  }

  static String empty(double width, String description, String title, List<Legend> legend) {
    return empty(width, description, title, legend, NO_PACKAGES);
  }

  /**
   * Returns a card with only the header and the note, as wide as the header.
   */
  static String empty(String description, String title, List<Legend> legend, String note) {
    return empty(headerWidth(title, legend), description, title, legend, note);
  }

  /**
   * Returns a card with only the header and the note.
   */
  static String empty(double width, String description, String title, List<Legend> legend,
      String note) {
    StringBuilder svg = start(width, CONTENT_TOP + 40 + PADDING, description, title, legend);
    svg.append(text(width / 2, CONTENT_TOP + 24, "middle", "muted", note));

    return end(svg);
  }

  private static void header(StringBuilder svg, double width, String title,
      List<Legend> legend) {
    double x = width - PADDING - legendWidth(legend);
    double y = HEADER_BASELINE - 4;

    svg.append(text(PADDING, HEADER_BASELINE, "class=\"ink\" font-size=\"15\" font-weight=\"600\"",
        title));

    for (Legend entry : legend) {
      String style = "class=\"%s\"".formatted(entry.style());

      svg.append(switch (entry.shape()) {
        case LINE -> line(x, y, x + 10, y, style + " stroke-width=\"2\"");
        case SQUARE -> rect(x, y - 5, 10, 10, "rx=\"2\" " + style + " stroke-width=\"1.5\"");
        case DOT -> circle(x + 5, y, 5, style);
      });

      svg.append(text(x + LEGEND_MARKER, HEADER_BASELINE, "start", "ink-2", entry.label()));
      x += LEGEND_MARKER + width(entry.label()) + LEGEND_GAP;
    }
  }

  static double headerWidth(String title, List<Legend> legend) {
    return 2 * PADDING + title.length() * TITLE_CHAR_WIDTH + 2 * LEGEND_GAP + legendWidth(legend);
  }

  static String text(double x, double y, String anchor, String style, String content) {
    return text(x, y, "text-anchor=\"%s\" class=\"%s\"".formatted(anchor, style), content);
  }

  /**
   * Returns a text at the baseline {@code y}. This and the following elements are indented for the
   * top level of the document.
   */
  static String text(double x, double y, String attributes, String content) {
    return "  <text x=\"%s\" y=\"%s\" %s>%s</text>\n".formatted(number(x), number(y), attributes,
        escaped(content));
  }

  static String rotated(double x, double y, String anchor, String style, String content) {
    return text(x, y, "text-anchor=\"%s\" class=\"%s\" transform=\"rotate(-90 %s %s)\"".formatted(
        anchor, style, number(x), number(y)), content);
  }

  static String line(double x1, double y1, double x2, double y2, String attributes) {
    return "  <line x1=\"%s\" y1=\"%s\" x2=\"%s\" y2=\"%s\" %s/>\n".formatted(number(x1),
        number(y1), number(x2), number(y2), attributes);
  }

  static String rect(double x, double y, double width, double height, String attributes) {
    return "  <rect x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\" %s/>\n".formatted(number(x),
        number(y), number(width), number(height), attributes);
  }

  static String circle(double cx, double cy, double radius, String attributes) {
    return "  <circle cx=\"%s\" cy=\"%s\" r=\"%s\" %s/>\n".formatted(number(cx), number(cy),
        number(radius), attributes);
  }

  /**
   * Returns a group around the elements that are not empty, indented once more.
   */
  static String group(String attributes, String title, String... elements) {
    StringBuilder group = new StringBuilder("  <g %s>\n    <title>%s</title>\n".formatted(
        attributes, escaped(title)));

    for (String element : elements) {
      if (!element.isEmpty()) {
        group.append("  ").append(element);
      }
    }

    return group.append("  </g>\n").toString();
  }

  /**
   * Returns a cell of a grid, inset by half the {@link #CELL_GAP} on every side.
   */
  static String cell(double x, double y, double width, double height, String attributes) {
    return rect(x + CELL_GAP / 2, y + CELL_GAP / 2, width - CELL_GAP, height - CELL_GAP,
        "rx=\"3\" " + attributes);
  }

  /**
   * Returns a count in the middle of a cell, at the baseline {@code y}, in the given CSS class.
   */
  static String count(double x, double y, String style, int count) {
    return text(x, y, "text-anchor=\"middle\" class=\"%s\" font-size=\"%s\" font-weight=\"600\""
        .formatted(style, number(COUNT_FONT_SIZE)), String.valueOf(count));
  }

  /**
   * Returns the box of a {@link #count(double, double, String, int) count}.
   */
  static Box countBox(double x, double y, int count) {
    double width = width(String.valueOf(count), COUNT_FONT_SIZE, true);

    return new Box(x - width / 2, y - 10, x + width / 2, y + 2);
  }

  /**
   * Draws the dots in their order, each with its label at the first free position of the room:
   * next to the dot, above or below it, or farther away with a line. The dots take their room
   * first, and a dot without a free position goes without its label, so the dots that matter most
   * come first.
   */
  static void dots(StringBuilder svg, List<Dot> dots, Room room) {
    dots.forEach(dot -> room.take(dot.circle().box()));

    for (Dot dot : dots) {
      Circle shape = dot.circle();
      Optional<Label> label = room.place(positions(dot.label(), shape));

      svg.append(group(dot.attributes(), dot.title(),
          label.filter(free -> free.isDetached(shape))
              .map(free -> line(shape.cx(), shape.cy(), free.leaderX(shape), free.leaderY(shape),
                  "class=\"leader\""))
              .orElse(""),
          circle(shape.cx(), shape.cy(), shape.radius(), dot.circleAttributes()),
          label.map(free -> text(free.x(), free.y(),
                  "text-anchor=\"%s\" class=\"ink halo\" font-weight=\"500\"".formatted(
                      free.anchor()), free.content()))
              .orElse("")));
    }
  }

  /**
   * Returns the positions of the label of a dot: next to it, then above and below it, farther and
   * farther away.
   */
  private static Stream<Label> positions(String text, Circle circle) {
    return Stream.concat(
        Stream.of(Label.of(text, circle.cx() + circle.radius() + 4, circle.cy() + 4, "start"),
            Label.of(text, circle.cx() - circle.radius() - 4, circle.cy() + 4, "end")),
        IntStream.range(0, MAX_LABEL_LINES).boxed().flatMap(line -> Stream.of(
                circle.cy() - circle.radius() - 6 - line * LABEL_LINE_HEIGHT,
                circle.cy() + circle.radius() + 15 + line * LABEL_LINE_HEIGHT)
            .flatMap(y -> line == 0
                ? Stream.of(Label.of(text, circle.cx(), y, "middle"))
                : Stream.of(Label.of(text, circle.cx(), y, "middle"),
                    Label.of(text, circle.cx() - 4, y, "end"),
                    Label.of(text, circle.cx() + 4, y, "start")))));
  }

  /**
   * Returns the names, or their number if there are more than {@link #MAX_NAMES}, so that a label
   * stays short.
   */
  static String names(List<String> names, String number) {
    return names.size() <= MAX_NAMES ? String.join(", ", names) : number;
  }

  /**
   * Returns the estimated width of a text in the font size 12.
   */
  static double width(String text) {
    return width(text, 12, false);
  }

  static double width(String text, double fontSize, boolean bold) {
    return text.length() * fontSize * (bold ? BOLD_CHAR_WIDTH : CHAR_WIDTH);
  }

  static String classes(int classes) {
    return classes == 1 ? "1 class" : classes + " classes";
  }

  private static String dependencies(int dependencies) {
    return dependencies == 1 ? "1 dependency" : dependencies + " dependencies";
  }

  static String dependency(String origin, String target, int dependencies) {
    return "%s \u2192 %s: %s".formatted(origin, target, dependencies(dependencies));
  }

  /**
   * Returns a tooltip of the header and up to {@link MetricsMarkdownExport#LISTED_CLASSES} classes,
   * one per line.
   */
  static String tooltip(String header, Stream<String> classes) {
    return Stream.concat(Stream.of(header), classes.limit(MetricsMarkdownExport.LISTED_CLASSES))
        .collect(joining("\n"));
  }

  static String tooltip(String origin, String target, PackageDependency dependency) {
    return dependency(origin, target, dependency.dependencies())
        + (dependency.closesCycle() ? ", closes a cycle" : "")
        + (dependency.dependsOnLessStable() ? ", on a less stable package" : "");
  }

  /**
   * Formats a coordinate with at most two decimals, so that a chart only changes when the metrics
   * do.
   */
  static String number(double value) {
    return TRAILING_ZEROS.matcher(String.format(Locale.ROOT, "%.2f", value)).replaceFirst("");
  }

  /**
   * Formats a point of a polygon or a polyline.
   */
  static String point(double x, double y) {
    return number(x) + "," + number(y);
  }

  static String escaped(String text) {
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  private static double legendWidth(List<Legend> legend) {
    return legend.stream()
        .mapToDouble(entry -> LEGEND_MARKER + width(entry.label()))
        .sum() + LEGEND_GAP * Math.max(0, legend.size() - 1);
  }

  /**
   * A dot of a chart.
   *
   * @param attributes the attributes of the group around the dot and its label
   * @param circleAttributes the attributes of the circle, such as its color
   */
  record Dot(String label, String title, String attributes, String circleAttributes,
      Circle circle) {

  }

  /**
   * The blue of a count and the color of a number on it.
   *
   * @param fill the color of the step of the ramp
   * @param text the CSS class of a number on the fill
   */
  record Shade(String fill, String text) {

    /**
     * Returns the step of the ramp by the square root of the share of the highest count, so that
     * small counts stay visible.
     */
    static Shade of(int count, int maxCount) {
      int step = (int) Math.ceil(Math.sqrt((double) count / maxCount) * RAMP.size()) - 1;
      step = Math.max(0, Math.min(RAMP.size() - 1, step));

      return new Shade(RAMP.get(step), step < FIRST_WHITE_STEP ? "ink" : "on-fill");
    }
  }

  record Box(double x1, double y1, double x2, double y2) {

    boolean overlaps(Box other) {
      return this.x1 < other.x2 + 1 && other.x1 < this.x2 + 1
          && this.y1 < other.y2 + 1 && other.y1 < this.y2 + 1;
    }

    boolean isInside(Box other) {
      return this.x1 >= other.x1 && this.x2 <= other.x2
          && this.y1 >= other.y1 && this.y2 <= other.y2;
    }
  }

  record Circle(double cx, double cy, double radius) {

    Box box() {
      return new Box(this.cx - this.radius, this.cy - this.radius, this.cx + this.radius,
          this.cy + this.radius);
    }
  }

  /**
   * A label at the baseline {@code y}, with an estimated width.
   */
  record Label(String content, double x, double y, String anchor, Box box) {

    static Label of(String content, double x, double y, String anchor) {
      double width = width(content);
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
   * The free room of a plot for labels and counts. Something goes to the first of its positions
   * whose box is inside the plot and free, and then takes its room, as does every box that
   * something else takes, such as a dot. A box taken twice takes its room once.
   */
  static final class Room {

    private final Box plot;
    private final Set<Box> taken = new LinkedHashSet<>();

    Room(Box plot, Box... taken) {
      this.plot = plot;
      this.taken.addAll(List.of(taken));
    }

    void take(Box box) {
      this.taken.add(box);
    }

    Optional<Label> place(Stream<Label> positions) {
      return place(positions, Label::box);
    }

    <T> Optional<T> place(Stream<T> positions, Function<T, Box> boxOf) {
      Optional<T> free = positions
          .filter(position -> boxOf.apply(position).isInside(this.plot))
          .filter(position -> this.taken.stream().noneMatch(boxOf.apply(position)::overlaps))
          .findFirst();
      free.ifPresent(position -> take(boxOf.apply(position)));

      return free;
    }
  }

  /**
   * The square plot of the main sequence and the coupling map, with the same ticks on both axes.
   */
  static final class SquarePlot {

    static final double WIDTH = 644;
    static final double HEIGHT = 670;
    static final double LEFT = 92;
    static final double TOP = CONTENT_TOP;
    static final double SIZE = 520;

    /**
     * The plot without its rounded corners, which the labels stay inside.
     */
    static final Box LABEL_AREA = new Box(LEFT + 2, TOP + 2, LEFT + SIZE - 2, TOP + SIZE - 2);

    /**
     * The space between the plot and the ticks, so that the largest dots do not cover them.
     */
    private static final double MARGIN = 14;

    private SquarePlot() {
    }

    static String plane() {
      return rect(LEFT, TOP, SIZE, SIZE, "rx=\"8\" class=\"plane\"");
    }

    /**
     * Returns the horizontal position of the share of the axis.
     */
    static double x(double share) {
      return LEFT + share * SIZE;
    }

    /**
     * Returns the vertical position of the share of the axis, from the bottom.
     */
    static double y(double share) {
      return TOP + (1 - share) * SIZE;
    }

    /**
     * Draws the ticks on both axes and the title of each axis, between the meanings of its ends.
     */
    static void axes(StringBuilder svg, List<Tick> ticks, Axis horizontal, Axis vertical) {
      double titleY = TOP + SIZE + MARGIN + 40;
      double titleX = PADDING + 10;

      for (Tick tick : ticks) {
        svg.append(text(x(tick.share()), TOP + SIZE + MARGIN + 16, "middle", "muted",
                tick.label()))
            .append(text(LEFT - MARGIN - 6, y(tick.share()) + 4, "end", "muted", tick.label()));
      }

      svg.append(text(LEFT, titleY, "start", "muted", horizontal.low()))
          .append(text(LEFT + SIZE / 2, titleY, "middle", "ink-2", horizontal.title()))
          .append(text(LEFT + SIZE, titleY, "end", "muted", horizontal.high()))
          .append(rotated(titleX, TOP + SIZE, "start", "muted", vertical.low()))
          .append(rotated(titleX, TOP + SIZE / 2, "middle", "ink-2", vertical.title()))
          .append(rotated(titleX, TOP, "end", "muted", vertical.high()));
    }

    /**
     * A tick at its share of an axis.
     */
    record Tick(double share, String label) {

    }

    /**
     * The title of an axis and the meanings of its low and its high end.
     */
    record Axis(String low, String title, String high) {

    }
  }

  record Legend(String style, String label, Shape shape) {

    static Legend dot(String style, String label) {
      return new Legend(style, label, Shape.DOT);
    }

    static Legend line(String style, String label) {
      return new Legend(style, label, Shape.LINE);
    }

    static Legend square(String style, String label) {
      return new Legend(style, label, Shape.SQUARE);
    }
  }

  enum Shape {
    DOT, LINE, SQUARE
  }
}
