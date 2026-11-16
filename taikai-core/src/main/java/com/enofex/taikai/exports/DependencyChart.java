package com.enofex.taikai.exports;

import static java.util.Comparator.comparingDouble;

import com.enofex.taikai.exports.ArchitectureMetrics.PackageDependency;
import com.enofex.taikai.exports.ArchitectureMetrics.PackageDependency.Kind;
import com.enofex.taikai.exports.ArchitectureMetrics.PackageMetric;
import com.enofex.taikai.exports.Svg.Legend;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Draws the package dependencies as a layered graph: every package above the packages it depends
 * on. Red arrows point upwards and close a cycle, orange ones depend on a less stable package.
 *
 * <p>A graph wider than {@value #MAX_WIDTH} pixels could not be read on a page, so a note refers to
 * the {@link DependencyMatrix} instead.</p>
 */
final class DependencyChart {

  private static final String TITLE = "Package dependencies";
  static final String DESCRIPTION = "Dependencies between the packages";
  private static final List<Legend> LEGEND = Stream.concat(Svg.ZONE_LEGEND.stream(),
      Stream.of(Legend.line("cycle", "Closes a cycle"),
          Legend.line("unstable", "Uses less stable"))).toList();
  private static final String TOO_WIDE =
      "This graph would be too wide to read, see the dependency matrix";

  private static final int MAX_WIDTH = 3000;
  private static final double NODE_HEIGHT = 44;
  private static final double LAYER_GAP = 60;
  private static final double NODE_GAP = 22;
  private static final double DUMMY_WIDTH = 6;
  private static final double DUMMY_GAP = 2;
  private static final double MIN_NODE_WIDTH = 84;
  private static final double MIN_STROKE = 1.25;
  private static final double MAX_STROKE = 3.5;
  private static final int ORDER_SWEEPS = 24;
  private static final int POSITION_SWEEPS = 8;

  private final ArchitectureMetrics metrics;
  private final List<Node> nodes = new ArrayList<>();
  private final List<Chain> chains = new ArrayList<>();
  private final List<List<Integer>> layers = new ArrayList<>();
  private final List<Set<Integer>> above = new ArrayList<>();
  private final List<Set<Integer>> below = new ArrayList<>();
  private double[] x = new double[0];

  private DependencyChart(ArchitectureMetrics metrics) {
    this.metrics = metrics;

    List<PackageMetric> packages = metrics.packages();
    Map<String, Integer> indexOf = DependencyGraph.indexes(packages.stream()
        .map(PackageMetric::name)
        .toList());
    List<Edge> edges = metrics.packageDependencies().stream()
        .map(dependency -> Edge.of(dependency, indexOf))
        .toList();
    int[] rank = ranks(metrics.layeredOrder(), indexOf, edges);
    int maxRank = Arrays.stream(rank).max().orElse(0);

    for (int layer = 0; layer <= maxRank; layer++) {
      this.layers.add(new ArrayList<>());
    }

    for (int node = 0; node < packages.size(); node++) {
      PackageMetric metric = packages.get(node);
      add(Node.of(metric, metrics.packageLabel(metric.name()), maxRank - rank[node]));
    }

    edges.forEach(this::chain);
  }

  /**
   * Returns the graph, or a note if it would be too wide.
   */
  static String svg(ArchitectureMetrics metrics) {
    return metrics.packages().isEmpty()
        ? Svg.empty(DESCRIPTION, TITLE, LEGEND, Svg.NO_PACKAGES)
        : new DependencyChart(metrics).draw();
  }

  /**
   * Returns the rank of every package: one more than the deepest package below it that it depends
   * on.
   */
  private static int[] ranks(List<String> layeredOrder, Map<String, Integer> indexOf,
      List<Edge> edges) {
    List<List<Integer>> lowers = new ArrayList<>();

    for (int node = 0; node < indexOf.size(); node++) {
      lowers.add(new ArrayList<>());
    }

    edges.forEach(edge -> lowers.get(edge.upper()).add(edge.lower()));
    int[] rank = new int[indexOf.size()];

    for (int index = layeredOrder.size() - 1; index >= 0; index--) {
      int node = indexOf.get(layeredOrder.get(index));

      for (int lower : lowers.get(node)) {
        rank[node] = Math.max(rank[node], rank[lower] + 1);
      }
    }

    return rank;
  }

  private int add(Node node) {
    int index = this.nodes.size();
    this.nodes.add(node);
    this.above.add(new LinkedHashSet<>());
    this.below.add(new LinkedHashSet<>());
    this.layers.get(node.layer()).add(index);

    return index;
  }

  /**
   * Adds the nodes of a dependency, with a dummy node in every layer between its packages.
   */
  private void chain(Edge edge) {
    List<Integer> chain = new ArrayList<>(List.of(edge.upper()));

    for (int layer = this.nodes.get(edge.upper()).layer() + 1;
        layer < this.nodes.get(edge.lower()).layer(); layer++) {
      chain.add(add(Node.dummy(layer)));
    }

    chain.add(edge.lower());

    for (int index = 0; index + 1 < chain.size(); index++) {
      this.below.get(chain.get(index)).add(chain.get(index + 1));
      this.above.get(chain.get(index + 1)).add(chain.get(index));
    }

    this.chains.add(new Chain(chain, edge.dependency()));
  }

  /**
   * Orders the layers by the average position of the neighbors and keeps the order with the fewest
   * crossings.
   */
  private void orderLayers() {
    List<List<Integer>> best = copy(this.layers);
    long fewest = crossings();

    for (int sweep = 0; sweep < ORDER_SWEEPS && fewest > 0; sweep++) {
      boolean down = sweep % 2 == 0;

      for (int step = 1; step < this.layers.size(); step++) {
        int layer = down ? step : this.layers.size() - 1 - step;
        reorder(layer, down ? this.above : this.below, down ? layer - 1 : layer + 1);
      }

      long current = crossings();

      if (current < fewest) {
        fewest = current;
        best = copy(this.layers);
      }
    }

    for (int layer = 0; layer < this.layers.size(); layer++) {
      this.layers.set(layer, best.get(layer));
    }
  }

  private void reorder(int layer, List<Set<Integer>> neighbors, int neighborLayer) {
    Map<Integer, Integer> indexOf = DependencyGraph.indexes(this.layers.get(neighborLayer));
    List<Integer> nodes = this.layers.get(layer);
    Map<Integer, Double> barycenter = new HashMap<>();

    for (int index = 0; index < nodes.size(); index++) {
      int node = nodes.get(index);
      final int current = index;
      barycenter.put(node, neighbors.get(node).stream()
          .mapToInt(indexOf::get)
          .average()
          .orElse(current));
    }

    List<Integer> ordered = new ArrayList<>(nodes);
    ordered.sort(comparingDouble(barycenter::get));
    this.layers.set(layer, ordered);
  }

  /**
   * Returns the crossings between adjacent layers, counted as inversions with a Fenwick tree.
   */
  private long crossings() {
    long crossings = 0;

    for (int layer = 0; layer + 1 < this.layers.size(); layer++) {
      Map<Integer, Integer> lower = DependencyGraph.indexes(this.layers.get(layer + 1));
      long[] counts = new long[this.layers.get(layer + 1).size() + 1];
      long seen = 0;

      for (int node : this.layers.get(layer)) {
        int[] targets = this.below.get(node).stream().mapToInt(lower::get).sorted().toArray();

        for (int target : targets) {
          crossings += seen - prefix(counts, target + 1);
        }

        for (int target : targets) {
          for (int index = target + 1; index < counts.length; index += index & -index) {
            counts[index]++;
          }

          seen++;
        }
      }
    }

    return crossings;
  }

  private static long prefix(long[] counts, int position) {
    long sum = 0;

    for (int index = position; index > 0; index -= index & -index) {
      sum += counts[index];
    }

    return sum;
  }

  /**
   * Returns the horizontal center of every node, as close to its neighbors as its layer allows.
   */
  private double[] positions() {
    double[] position = new double[this.nodes.size()];

    for (List<Integer> layer : this.layers) {
      double right = 0;

      for (int index = 0; index < layer.size(); index++) {
        int node = layer.get(index);
        position[node] = right + width(node) / 2;

        if (index + 1 < layer.size()) {
          right += width(node) + gap(node, layer.get(index + 1));
        }
      }
    }

    for (int sweep = 0; sweep < POSITION_SWEEPS; sweep++) {
      boolean down = sweep % 2 == 0;

      for (int step = 0; step < this.layers.size(); step++) {
        int layer = down ? step : this.layers.size() - 1 - step;
        List<Set<Integer>> neighbors = down ? this.above : this.below;
        List<Integer> nodes = this.layers.get(layer);
        double[] desired = new double[nodes.size()];

        for (int index = 0; index < nodes.size(); index++) {
          int node = nodes.get(index);
          desired[index] = neighbors.get(node).stream()
              .mapToDouble(other -> position[other])
              .average()
              .orElse(position[node]);
        }

        place(nodes, desired, position);
      }
    }

    double left = Double.MAX_VALUE;

    for (int node = 0; node < this.nodes.size(); node++) {
      left = Math.min(left, position[node] - width(node) / 2);
    }

    for (int node = 0; node < this.nodes.size(); node++) {
      position[node] -= left;
    }

    return position;
  }

  /**
   * Places the nodes of a layer close to their desired positions: halfway between pushing
   * overlapping nodes to the right and to the left.
   */
  private void place(List<Integer> nodes, double[] desired, double[] position) {
    int count = nodes.size();
    double[] right = new double[count];
    double[] left = new double[count];

    for (int index = 0; index < count; index++) {
      right[index] = index == 0
          ? desired[index]
          : Math.max(desired[index], right[index - 1] + spacing(nodes, index - 1));
    }

    for (int index = count - 1; index >= 0; index--) {
      left[index] = index == count - 1
          ? desired[index]
          : Math.min(desired[index], left[index + 1] - spacing(nodes, index));
    }

    for (int index = 0; index < count; index++) {
      position[nodes.get(index)] = (right[index] + left[index]) / 2;
    }
  }

  private double spacing(List<Integer> nodes, int index) {
    int node = nodes.get(index);
    int next = nodes.get(index + 1);

    return (width(node) + width(next)) / 2 + gap(node, next);
  }

  /**
   * Returns the gap between neighbors, which is small between dummy nodes, so that passing arrows
   * take little room.
   */
  private double gap(int node, int next) {
    return this.nodes.get(node).isDummy() && this.nodes.get(next).isDummy()
        ? DUMMY_GAP
        : NODE_GAP;
  }

  /**
   * Lays out and draws the graph. A layer that alone is too wide skips the layout, which would take
   * long.
   */
  private String draw() {
    if (this.layers.stream()
        .anyMatch(layer -> layer.stream().mapToDouble(this::width).sum() > MAX_WIDTH)) {
      return tooWide();
    }

    orderLayers();
    this.x = positions();

    double contentWidth = 0;

    for (int node = 0; node < this.nodes.size(); node++) {
      contentWidth = Math.max(contentWidth, this.x[node] + width(node) / 2);
    }

    double width = Math.max(contentWidth + 2 * Svg.PADDING, Svg.headerWidth(TITLE, LEGEND));

    if (width > MAX_WIDTH) {
      return tooWide();
    }

    double offset = (width - contentWidth) / 2;
    double height = top(this.layers.size() - 1) + NODE_HEIGHT + Svg.PADDING;
    StringBuilder svg = Svg.start(width, height, DESCRIPTION, TITLE, LEGEND);

    for (int node = 0; node < this.x.length; node++) {
      this.x[node] += offset;
    }

    definitions(svg);

    double maxDependencies = this.chains.stream()
        .mapToInt(chain -> chain.dependency().dependencies())
        .max()
        .orElse(1);
    double[] ports = ports();

    for (Kind kind : Kind.values()) {
      for (int index = 0; index < this.chains.size(); index++) {
        Chain chain = this.chains.get(index);

        if (chain.dependency().kind() == kind) {
          edge(svg, chain, index, ports, maxDependencies);
        }
      }
    }

    for (int node = 0; node < this.nodes.size(); node++) {
      if (!this.nodes.get(node).isDummy()) {
        node(svg, this.nodes.get(node), this.x[node]);
      }
    }

    return Svg.end(svg);
  }

  private static String tooWide() {
    return Svg.empty(DESCRIPTION, TITLE, LEGEND, TOO_WIDE);
  }

  private static void definitions(StringBuilder svg) {
    svg.append("  <defs>\n")
        .append("    <filter id=\"shadow\" x=\"-10%\" y=\"-20%\" width=\"120%\" height=\"150%\">"
            + "<feDropShadow dx=\"0\" dy=\"1\" stdDeviation=\"1.5\" flood-color=\"#0b0b0b\" "
            + "flood-opacity=\"0.08\"/></filter>\n");
    for (Kind kind : Kind.values()) {
      svg.append(marker(style(kind)));
    }

    svg.append("  </defs>\n");
  }

  private static String style(Kind kind) {
    return switch (kind) {
      case REGULAR -> "edge";
      case ON_LESS_STABLE -> "unstable";
      case CLOSES_CYCLE -> "cycle";
    };
  }

  private static String marker(String style) {
    return ("    <marker id=\"%1$s-arrow\" viewBox=\"0 0 10 10\" refX=\"10\" refY=\"5\" "
        + "markerWidth=\"9\" markerHeight=\"9\" markerUnits=\"userSpaceOnUse\" orient=\"auto\">"
        + "<path d=\"M0,1 L10,5 L0,9 z\" class=\"%1$s-head\"/></marker>\n").formatted(style);
  }

  /**
   * Returns where every chain leaves and enters its packages, spread over their width, at the index
   * of the chain times two, plus one for the lower end.
   */
  private double[] ports() {
    Map<Integer, List<int[]>> bottoms = new HashMap<>();
    Map<Integer, List<int[]>> tops = new HashMap<>();

    for (int index = 0; index < this.chains.size(); index++) {
      List<Integer> chain = this.chains.get(index).nodes();
      bottoms.computeIfAbsent(chain.get(0), node -> new ArrayList<>())
          .add(new int[]{index * 2, chain.get(1)});
      tops.computeIfAbsent(chain.get(chain.size() - 1), node -> new ArrayList<>())
          .add(new int[]{index * 2 + 1, chain.get(chain.size() - 2)});
    }

    double[] ports = new double[this.chains.size() * 2];

    for (Map<Integer, List<int[]>> side : List.of(bottoms, tops)) {
      side.forEach((node, ends) -> {
        ends.sort(comparingDouble((int[] end) -> this.x[end[1]]).thenComparingInt(end -> end[0]));
        double span = Math.min(width(node) - 28, (ends.size() - 1) * 12.0);

        for (int index = 0; index < ends.size(); index++) {
          double shift = ends.size() == 1 ? 0 : span * index / (ends.size() - 1) - span / 2;
          ports[ends.get(index)[0]] = this.x[node] + shift;
        }
      });
    }

    return ports;
  }

  private void edge(StringBuilder svg, Chain chain, int index, double[] ports,
      double maxDependencies) {
    List<Integer> nodes = chain.nodes();
    List<double[]> points = new ArrayList<>();
    points.add(new double[]{ports[index * 2], top(this.nodes.get(nodes.get(0)).layer())
        + NODE_HEIGHT});

    for (int dummy : nodes.subList(1, nodes.size() - 1)) {
      points.add(new double[]{this.x[dummy],
          top(this.nodes.get(dummy).layer()) + NODE_HEIGHT / 2});
    }

    points.add(new double[]{ports[index * 2 + 1],
        top(this.nodes.get(nodes.get(nodes.size() - 1)).layer())});

    if (chain.isUpwards()) {
      Collections.reverse(points);
    }

    StringBuilder path = new StringBuilder("M%s,%s".formatted(Svg.number(points.get(0)[0]),
        Svg.number(points.get(0)[1])));

    for (int point = 1; point < points.size(); point++) {
      double[] from = points.get(point - 1);
      double[] to = points.get(point);
      double half = (to[1] - from[1]) / 2;
      path.append(" C%s,%s %s,%s %s,%s".formatted(Svg.number(from[0]),
          Svg.number(from[1] + half), Svg.number(to[0]), Svg.number(to[1] - half),
          Svg.number(to[0]), Svg.number(to[1])));
    }

    PackageDependency dependency = chain.dependency();
    String style = style(dependency.kind());
    double stroke = MIN_STROKE + (MAX_STROKE - MIN_STROKE)
        * Math.sqrt(dependency.dependencies() / maxDependencies);

    svg.append(("  <path d=\"%s\" class=\"%s\" stroke-width=\"%s\" stroke-linecap=\"round\" "
            + "marker-end=\"url(#%s-arrow)\"><title>%s</title></path>\n").formatted(path,
        style, Svg.number(stroke), style,
        Svg.escaped(Svg.tooltip(this.metrics.packageLabel(dependency.origin()),
            this.metrics.packageLabel(dependency.target()), dependency))));
  }

  private static void node(StringBuilder svg, Node node, double center) {
    double left = center - node.width() / 2;
    double top = top(node.layer());

    svg.append(Svg.group("class=\"package\" data-zone=\"%s\"".formatted(node.style()),
        node.title(),
        Svg.rect(left, top, node.width(), NODE_HEIGHT,
            "rx=\"8\" class=\"node\" filter=\"url(#shadow)\""),
        Svg.circle(left + 15, top + 15, 4.5, "class=\"%s\"".formatted(node.style())),
        Svg.text(left + 26, top + 19, "class=\"ink\" font-size=\"12.5\" font-weight=\"600\"",
            node.label()),
        Svg.text(left + 26, top + 34, "class=\"muted\" font-size=\"11\"", node.detail())));
  }

  private static double top(int layer) {
    return Svg.CONTENT_TOP + 8 + layer * (NODE_HEIGHT + LAYER_GAP);
  }

  private double width(int node) {
    return this.nodes.get(node).width();
  }

  private static List<List<Integer>> copy(List<List<Integer>> layers) {
    return layers.stream().map(layer -> (List<Integer>) new ArrayList<>(layer)).toList();
  }

  /**
   * A dependency between the upper and the lower package; the upper one depends on the lower one
   * unless the dependency closes a cycle.
   */
  private record Edge(int upper, int lower, PackageDependency dependency) {

    static Edge of(PackageDependency dependency, Map<String, Integer> indexOf) {
      int origin = indexOf.get(dependency.origin());
      int target = indexOf.get(dependency.target());

      return dependency.closesCycle()
          ? new Edge(target, origin, dependency)
          : new Edge(origin, target, dependency);
    }
  }

  private record Chain(List<Integer> nodes, PackageDependency dependency) {

    boolean isUpwards() {
      return this.dependency.closesCycle();
    }
  }

  /**
   * A package, or a dummy node that a dependency passes through.
   */
  private record Node(String label, String detail, String title, String style, double width,
      int layer, boolean isDummy) {

    static Node of(PackageMetric metric, String label, int layer) {
      String detail = Svg.classes(metric.classes());
      double width = Math.max(MIN_NODE_WIDTH, 26 + 14
          + Math.max(Svg.width(label, 12.5, true), Svg.width(detail, 11, false)));

      return new Node(label, detail, "%s: %s, Ca %s, Ce %s".formatted(label, detail,
          metric.afferentCouplings(), metric.efferentCouplings()), metric.style(), width, layer,
          false);
    }

    static Node dummy(int layer) {
      return new Node("", "", "", "", DUMMY_WIDTH, layer, true);
    }
  }
}
