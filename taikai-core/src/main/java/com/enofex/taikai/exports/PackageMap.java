package com.enofex.taikai.exports;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;

import com.enofex.taikai.exports.ArchitectureMetrics.PackageMetric;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Draws the packages as a treemap: a tile per package, sized by its classes and colored by its
 * zone. The tiles are laid out by the squarified algorithm of Bruls, Huizing and van Wijk.
 */
final class PackageMap {

  private static final String TITLE = "Package map";
  static final String DESCRIPTION = "Map of the packages";

  private static final double WIDTH = 644;
  private static final double MAP_HEIGHT = 400;
  private static final double GAP = 3;
  private static final double MIN_OPACITY = 0.14;
  private static final double DISTANCE_OPACITY = 0.5;
  private static final double NAME_CHAR_WIDTH = Svg.width("x", 12, true);
  private static final double DETAIL_CHAR_WIDTH = Svg.width("x", 11, false);

  private PackageMap() {
  }

  static String svg(ArchitectureMetrics metrics) {
    List<PackageMetric> packages = metrics.packages();

    if (packages.isEmpty()) {
      return Svg.empty(WIDTH, DESCRIPTION, TITLE, Svg.ZONE_LEGEND);
    }

    StringBuilder svg = Svg.start(WIDTH, Svg.CONTENT_TOP + MAP_HEIGHT + Svg.PADDING, DESCRIPTION,
        TITLE, Svg.ZONE_LEGEND);

    List<PackageMetric> bySize = packages.stream()
        .sorted(comparingInt(PackageMetric::classes).reversed()
            .thenComparing(PackageMetric::name))
        .toList();

    for (Tile tile : squarify(bySize, new Space(Svg.PADDING, Svg.CONTENT_TOP,
        WIDTH - 2 * Svg.PADDING, MAP_HEIGHT))) {
      tile(svg, tile, metrics.packageLabel(tile.metric().name()));
    }

    return Svg.end(svg);
  }

  private static void tile(StringBuilder svg, Tile tile, String label) {
    PackageMetric metric = tile.metric();
    OptionalDouble distance = metric.distance();
    String zone = metric.style();
    String classes = Svg.classes(metric.classes());
    List<String> details = distance.isPresent()
        ? List.of("%s \u00b7 D %s".formatted(classes,
            ArchitectureMetrics.formatted(distance.getAsDouble())),
            "D " + ArchitectureMetrics.formatted(distance.getAsDouble()))
        : List.of(classes + " \u00b7 no couplings", classes);
    double opacity = MIN_OPACITY + DISTANCE_OPACITY * distance.orElse(0);
    double x = tile.x() + GAP / 2;
    double y = tile.y() + GAP / 2;
    double width = Math.max(0, tile.width() - GAP);
    double height = Math.max(0, tile.height() - GAP);

    svg.append(Svg.group("class=\"tile\" data-zone=\"%s\"".formatted(zone),
        "%s: %s, I %s, A %s, D %s".formatted(label, classes,
            ArchitectureMetrics.formatted(metric.instability()),
            ArchitectureMetrics.formatted(metric.abstractness()),
            ArchitectureMetrics.formatted(distance)),
        Svg.rect(x, y, width, height,
            "rx=\"6\" class=\"%s\" fill-opacity=\"%s\"".formatted(zone, Svg.number(opacity))),
        height >= 26
            ? text(x + 8, y + 18, width - 16, label, NAME_CHAR_WIDTH,
                "class=\"ink\" font-size=\"12\" font-weight=\"600\"")
            : "",
        height >= 42
            ? details.stream()
                .filter(detail -> detail.length() * DETAIL_CHAR_WIDTH <= width - 16)
                .findFirst()
                .map(detail -> text(x + 8, y + 33, width - 16, detail, DETAIL_CHAR_WIDTH,
                    "class=\"ink-2\" font-size=\"11\""))
                .orElse("")
            : ""));
  }

  /**
   * Returns the text shortened with an ellipsis to the width, or nothing if not even three
   * characters fit.
   */
  private static String text(double x, double y, double width, String text, double charWidth,
      String attributes) {
    int fitting = (int) (width / charWidth);

    if (fitting < 3) {
      return "";
    }

    String shown = text.length() <= fitting ? text : text.substring(0, fitting - 1) + "\u2026";

    return Svg.text(x, y, attributes, shown);
  }

  /**
   * Lays out the packages, largest first, in rows along the shorter side of the remaining space, as
   * long as a row does not make its most elongated tile worse.
   */
  private static List<Tile> squarify(List<PackageMetric> packages, Space space) {
    double scale = space.width() * space.height()
        / packages.stream().mapToInt(PackageMetric::classes).sum();
    List<Tile> tiles = new ArrayList<>();
    List<PackageMetric> row = new ArrayList<>();

    for (PackageMetric metric : packages) {
      double side = Math.min(space.width(), space.height());
      List<PackageMetric> extended = new ArrayList<>(row);
      extended.add(metric);

      if (!row.isEmpty() && worst(extended, side, scale) > worst(row, side, scale)) {
        space = place(row, space, scale, tiles);
        row.clear();
      }

      row.add(metric);
    }

    place(row, space, scale, tiles);
    tiles.sort(comparing((Tile tile) -> tile.metric().name()));

    return tiles;
  }

  /**
   * Returns the largest aspect ratio of the tiles of a row.
   */
  private static double worst(List<PackageMetric> row, double side, double scale) {
    double total = row.stream().mapToDouble(metric -> metric.classes() * scale).sum();
    double worst = 0;

    for (PackageMetric metric : row) {
      double area = metric.classes() * scale;
      worst = Math.max(worst, Math.max(side * side * area / (total * total),
          total * total / (side * side * area)));
    }

    return worst;
  }

  /**
   * Places a row and returns the space that is left.
   */
  private static Space place(List<PackageMetric> row, Space space, double scale,
      List<Tile> tiles) {
    double total = row.stream().mapToDouble(metric -> metric.classes() * scale).sum();

    if (space.width() >= space.height()) {
      double columnWidth = total / space.height();
      double y = space.top();

      for (PackageMetric metric : row) {
        double tileHeight = metric.classes() * scale / columnWidth;
        tiles.add(new Tile(metric, space.left(), y, columnWidth, tileHeight));
        y += tileHeight;
      }

      return new Space(space.left() + columnWidth, space.top(), space.width() - columnWidth,
          space.height());
    }

    double rowHeight = total / space.width();
    double x = space.left();

    for (PackageMetric metric : row) {
      double tileWidth = metric.classes() * scale / rowHeight;
      tiles.add(new Tile(metric, x, space.top(), tileWidth, rowHeight));
      x += tileWidth;
    }

    return new Space(space.left(), space.top() + rowHeight, space.width(),
        space.height() - rowHeight);
  }

  private record Tile(PackageMetric metric, double x, double y, double width, double height) {

  }

  private record Space(double left, double top, double width, double height) {

  }
}
