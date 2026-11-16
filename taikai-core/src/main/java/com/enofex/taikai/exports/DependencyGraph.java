package com.enofex.taikai.exports;

import static java.util.Comparator.comparingInt;
import static java.util.Comparator.reverseOrder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * A directed graph of the nodes {@code 0} to {@code nodes - 1}. Its targets are sorted, so that it
 * does not depend on the order of adding them.
 */
final class DependencyGraph {

  private final List<Set<Integer>> targets;

  DependencyGraph(int nodes) {
    this.targets = new ArrayList<>(nodes);

    for (int node = 0; node < nodes; node++) {
      this.targets.add(new TreeSet<>());
    }
  }

  static <T> Map<T, Integer> indexes(List<T> items) {
    Map<T, Integer> indexOf = new HashMap<>();

    for (int index = 0; index < items.size(); index++) {
      indexOf.put(items.get(index), index);
    }

    return indexOf;
  }

  int nodes() {
    return this.targets.size();
  }

  void add(int origin, int target) {
    if (origin != target) {
      this.targets.get(origin).add(target);
    }
  }

  Set<Integer> targets(int node) {
    return this.targets.get(node);
  }

  /**
   * Returns the strongly connected components with the algorithm of Tarjan, each after the
   * components it depends on. It runs without recursion, so that long chains do not overflow the
   * stack.
   */
  List<List<Integer>> components() {
    int nodes = nodes();
    int[][] adjacency = adjacency();
    int[] index = new int[nodes];
    int[] lowLink = new int[nodes];
    int[] nextTarget = new int[nodes];
    boolean[] onStack = new boolean[nodes];
    Deque<Integer> stack = new ArrayDeque<>();
    Deque<Integer> path = new ArrayDeque<>();
    List<List<Integer>> components = new ArrayList<>();
    int visited = 0;

    Arrays.fill(index, -1);

    for (int root = 0; root < nodes; root++) {
      if (index[root] != -1) {
        continue;
      }

      path.push(root);

      while (!path.isEmpty()) {
        int node = path.peek();

        if (index[node] == -1) {
          index[node] = visited;
          lowLink[node] = visited;
          visited++;
          stack.push(node);
          onStack[node] = true;
        }

        if (nextTarget[node] < adjacency[node].length) {
          int target = adjacency[node][nextTarget[node]++];

          if (index[target] == -1) {
            path.push(target);
          } else if (onStack[target]) {
            lowLink[node] = Math.min(lowLink[node], index[target]);
          }
        } else {
          path.pop();

          if (!path.isEmpty()) {
            int parent = path.peek();
            lowLink[parent] = Math.min(lowLink[parent], lowLink[node]);
          }

          if (lowLink[node] == index[node]) {
            List<Integer> component = new ArrayList<>();
            int member;

            do {
              member = stack.pop();
              onStack[member] = false;
              component.add(member);
            } while (member != node);

            component.sort(null);
            components.add(component);
          }
        }
      }
    }

    return components;
  }

  /**
   * Returns the position of every node in the layered order that the {@link DependencyChart} and
   * the {@link DependencyMatrix} share: the components in the order of their dependencies, and the
   * nodes of a cycle by the heuristic of Eades, Lin and Smyth, which puts few dependencies upwards.
   *
   * @param components the components, as {@link #components()} returns them
   */
  int[] layeredPositions(List<List<Integer>> components) {
    List<List<Integer>> topDown = new ArrayList<>(components);
    Collections.reverse(topDown);

    int[] position = new int[nodes()];
    int next = 0;

    for (List<Integer> component : topDown) {
      for (int node : component.size() == 1 ? component : greedyOrder(component)) {
        position[node] = next++;
      }
    }

    return position;
  }

  /**
   * Orders the nodes of a cycle: sinks to the end, sources to the start, else the node with the
   * most outgoing over incoming dependencies to the start.
   */
  private List<Integer> greedyOrder(List<Integer> component) {
    Set<Integer> remaining = new LinkedHashSet<>(component);
    Map<Integer, List<Integer>> dependents = new HashMap<>();
    int[] outgoing = new int[nodes()];
    int[] incoming = new int[nodes()];

    for (int node : component) {
      for (int target : targets(node)) {
        if (remaining.contains(target)) {
          outgoing[node]++;
          incoming[target]++;
          dependents.computeIfAbsent(target, key -> new ArrayList<>()).add(node);
        }
      }
    }

    List<Integer> first = new ArrayList<>();
    Deque<Integer> last = new ArrayDeque<>();
    Consumer<Integer> take = node -> {
      remaining.remove(node);

      for (int target : targets(node)) {
        if (remaining.contains(target)) {
          incoming[target]--;
        }
      }

      for (int dependent : dependents.getOrDefault(node, List.of())) {
        if (remaining.contains(dependent)) {
          outgoing[dependent]--;
        }
      }
    };

    while (!remaining.isEmpty()) {
      boolean removed = true;

      while (removed) {
        removed = false;

        for (int node : List.copyOf(remaining)) {
          if (outgoing[node] == 0) {
            last.addFirst(node);
          } else if (incoming[node] == 0) {
            first.add(node);
          } else {
            continue;
          }

          take.accept(node);
          removed = true;
        }
      }

      remaining.stream()
          .max(comparingInt((Integer node) -> outgoing[node] - incoming[node])
              .thenComparing(reverseOrder()))
          .ifPresent(best -> {
            first.add(best);
            take.accept(best);
          });
    }

    first.addAll(last);
    return first;
  }

  private int[][] adjacency() {
    int[][] adjacency = new int[nodes()][];

    for (int node = 0; node < nodes(); node++) {
      adjacency[node] = this.targets.get(node).stream().mapToInt(Integer::intValue).toArray();
    }

    return adjacency;
  }
}
