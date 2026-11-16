package com.enofex.taikai.exports;

import com.enofex.taikai.exports.ArchitectureMetrics.PackageDependency;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The look that the charts share: a card with a title and a legend, and colors checked for color
 * vision deficiencies and contrast. The charts have no scripts, so that they work wherever Markdown
 * shows images.
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
