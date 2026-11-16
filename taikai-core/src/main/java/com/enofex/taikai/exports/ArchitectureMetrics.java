package com.enofex.taikai.exports;

import static com.enofex.taikai.internal.Modifiers.isSynthetic;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;

/**
 * The metrics of the measured classes, which are all classes but the synthetic ones. Every package
 * counts on its own, without its subpackages, and nested classes count for their top-level class.
 * Only dependencies between measured classes count, but for the libraries. The component metrics of
 * ArchUnit are not used, as they count packages instead of classes.
 *
 * @param prefix the package that all packages are in, or an empty string if they share none
 * @param layeredOrder the packages from top to bottom, each before the packages it depends on but
 *     for the dependencies that close a cycle
 */
record ArchitectureMetrics(String prefix, List<PackageMetric> packages, List<ClassMetric> classes,
    List<InheritanceMetric> inheritance, List<CohesionMetric> cohesion,
    List<PackageDependency> packageDependencies, List<PackageCycle> packageCycles,
    List<String> layeredOrder, List<LibraryUsage> libraryUsages) {

  /**
   * The distance from the main sequence above which a package is in a zone.
   */
  static final double ZONE_DISTANCE = 0.5;

  static ArchitectureMetrics of(JavaClasses javaClasses) {
    Map<String, JavaClass> measured = new TreeMap<>();
    javaClasses.stream()
        .filter(javaClass -> !isSynthetic(javaClass))
        .forEach(javaClass -> measured.put(javaClass.getName(), javaClass));

    Map<String, String> topLevelClassOf = new HashMap<>();
    measured.values().forEach(javaClass ->
        topLevelClassOf.put(javaClass.getName(), topLevelClass(javaClass, measured)));

    List<JavaClass> topLevelClasses = measured.values().stream()
        .filter(javaClass -> topLevelClassOf.get(javaClass.getName()).equals(javaClass.getName()))
        .toList();
    Map<String, Integer> indexOf = DependencyGraph.indexes(topLevelClasses.stream()
        .map(JavaClass::getName)
        .toList());

    DependencyGraph graph = new DependencyGraph(topLevelClasses.size());
    measured.values().forEach(javaClass -> {
      int origin = indexOf.get(topLevelClassOf.get(javaClass.getName()));

      for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
        String target = topLevelClassOf.get(
            dependency.getTargetClass().getBaseComponentType().getName());

        if (target != null) {
          graph.add(origin, indexOf.get(target));
        }
      }
    });

    Packages packages = new Packages(topLevelClasses, graph);
    List<PackageDependency> packageDependencies = packages.dependencies();
    String prefix = commonPrefix(packages.names());

    return new ArchitectureMetrics(
        prefix,
        packages.metrics(),
        classMetrics(topLevelClasses, graph),
        inheritanceMetrics(measured),
        Cohesion.of(measured.values()),
        packageDependencies,
        packages.cycles(packageDependencies),
        packages.layeredOrder(),
        Libraries.usages(measured.values(), topLevelClassOf, prefix, packages.names()));
  }

  /**
   * Returns the libraries, the one used by the most classes first.
   */
  List<Library> libraries() {
    return this.libraryUsages.stream()
        .collect(groupingBy(LibraryUsage::library, TreeMap::new, toList()))
        .entrySet().stream()
        .map(entry -> new Library(entry.getKey(), entry.getValue().size(),
            entry.getValue().stream().mapToInt(usage -> usage.classes().size()).sum()))
        .sorted(comparingInt(Library::classes).reversed())
        .toList();
  }

  /**
   * Returns the name of a package relative to the {@link #prefix()}.
   */
  String packageLabel(String packageName) {
    return packageName.isEmpty() ? "(default package)" : classLabel(packageName);
  }

  /**
   * Returns the name of a class relative to the {@link #prefix()}.
   */
  String classLabel(String className) {
    return this.prefix.isEmpty() || !className.startsWith(this.prefix + ".")
        ? className
        : className.substring(this.prefix.length() + 1);
  }

  int abstractClasses() {
    return this.packages.stream().mapToInt(PackageMetric::abstractClasses).sum();
  }

  /**
   * Returns the average distance of the packages that are coupled to others.
   */
  OptionalDouble averageDistance() {
    return this.packages.stream()
        .map(PackageMetric::distance)
        .filter(OptionalDouble::isPresent)
        .mapToDouble(OptionalDouble::getAsDouble)
        .average();
  }

  /**
   * Rounds to the two decimal places that are shown, so that values compare as they are shown.
   */
  static double rounded(double value) {
    return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  static String formatted(double value) {
    return String.format(Locale.ROOT, "%.2f", rounded(value));
  }

  /**
   * Formats a missing metric as {@code -}.
   */
  static String formatted(OptionalDouble value) {
    return value.isPresent() ? formatted(value.getAsDouble()) : "-";
  }

  /**
   * Returns the outermost measured class that the class is nested in, or the class itself.
   */
  private static String topLevelClass(JavaClass javaClass, Map<String, JavaClass> measured) {
    JavaClass topLevelClass = javaClass;

    while (topLevelClass.getEnclosingClass().isPresent()
        && measured.containsKey(topLevelClass.getEnclosingClass().get().getName())) {
      topLevelClass = topLevelClass.getEnclosingClass().get();
    }

    return topLevelClass.getName();
  }

  private static List<ClassMetric> classMetrics(List<JavaClass> topLevelClasses,
      DependencyGraph graph) {
    int[] fanIn = new int[graph.nodes()];

    for (int origin = 0; origin < graph.nodes(); origin++) {
      graph.targets(origin).forEach(target -> fanIn[target]++);
    }

    List<ClassMetric> metrics = new ArrayList<>();

    for (int node = 0; node < graph.nodes(); node++) {
      metrics.add(new ClassMetric(topLevelClasses.get(node).getName(), fanIn[node],
          graph.targets(node).size()));
    }

    return List.copyOf(metrics);
  }

  private static List<InheritanceMetric> inheritanceMetrics(Map<String, JavaClass> measured) {
    return measured.values().stream()
        .filter(javaClass -> !javaClass.isInterface() && !javaClass.isAnonymousClass())
        .map(javaClass -> new InheritanceMetric(javaClass.getName(),
            (int) javaClass.getAllRawSuperclasses().stream()
                .filter(superclass -> measured.containsKey(superclass.getName()))
                .count(),
            (int) javaClass.getSubclasses().stream()
                .filter(subclass -> !subclass.isAnonymousClass())
                .filter(subclass -> measured.containsKey(subclass.getName()))
                .count()))
        .toList();
  }

  /**
   * Returns the longest package, by segments, that contains all given packages.
   */
  private static String commonPrefix(Collection<String> packageNames) {
    List<String> prefix = null;

    for (String packageName : packageNames) {
      List<String> segments = packageName.isEmpty()
          ? List.of()
          : List.of(packageName.split("\\."));

      if (prefix == null) {
        prefix = segments;
      } else {
        int common = 0;

        while (common < prefix.size() && common < segments.size()
            && prefix.get(common).equals(segments.get(common))) {
          common++;
        }

        prefix = prefix.subList(0, common);
      }
    }

    return prefix == null ? "" : String.join(".", prefix);
  }

  /**
   * The zones of packages far from the main sequence.
   */
  enum Zone {

    /**
     * Concrete and stable.
     */
    PAIN,

    /**
     * Abstract and unstable.
     */
    USELESSNESS;

    String label() {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  /**
   * The metrics of a package of Robert C. Martin, counted in top-level classes.
   *
   * @param afferentCouplings Ca, the classes of other packages that depend on the package
   * @param efferentCouplings Ce, the classes of the package that depend on other packages
   */
  record PackageMetric(String name, int classes, int abstractClasses, int publicClasses,
      int afferentCouplings, int efferentCouplings) {

    /**
     * Returns A, the share of abstract classes and interfaces.
     */
    double abstractness() {
      return (double) this.abstractClasses / this.classes;
    }

    /**
     * Returns I = Ce / (Ca + Ce), or nothing without couplings.
     */
    OptionalDouble instability() {
      int couplings = this.afferentCouplings + this.efferentCouplings;

      return couplings == 0
          ? OptionalDouble.empty()
          : OptionalDouble.of((double) this.efferentCouplings / couplings);
    }

    /**
     * Returns D = |A + I - 1|, or nothing without couplings.
     */
    OptionalDouble distance() {
      OptionalDouble instability = instability();

      return instability.isPresent()
          ? OptionalDouble.of(Math.abs(abstractness() + instability.getAsDouble() - 1))
          : OptionalDouble.empty();
    }

    /**
     * Returns the relative visibility of Herbert Dowalil, the share of public classes.
     */
    double relativeVisibility() {
      return (double) this.publicClasses / this.classes;
    }

    /**
     * Returns the zone of the package if its rounded distance is above {@link #ZONE_DISTANCE}.
     */
    Optional<Zone> zone() {
      OptionalDouble instability = instability();

      if (instability.isEmpty() || rounded(distance().getAsDouble()) <= ZONE_DISTANCE) {
        return Optional.empty();
      }

      return Optional.of(abstractness() + instability.getAsDouble() < 1
          ? Zone.PAIN
          : Zone.USELESSNESS);
    }

    /**
     * Returns the CSS class of the package in the charts: its zone, {@code balanced} or
     * {@code isolated} without couplings.
     */
    String style() {
      return instability().isEmpty() ? "isolated" : zone().map(Zone::label).orElse("balanced");
    }
  }

  /**
   * The coupling of a top-level class, including its nested classes.
   */
  record ClassMetric(String name, int fanIn, int fanOut) {

  }

  /**
   * The superclasses and subclasses of a class among the measured classes.
   */
  record InheritanceMetric(String name, int depth, int subclasses) {

  }

  /**
   * The cohesion of a class: LCOM4 is the number of {@code groups} of its methods that count.
   */
  record CohesionMetric(String name, int methods, int groups) {

  }

  /**
   * The dependencies of the classes of one package on those of another.
   *
   * @param closesCycle whether the dependency points upwards in the layered order
   * @param originInstability the instability of the origin, rounded as it is shown
   * @param targetInstability the instability of the target, rounded as it is shown
   */
  record PackageDependency(String origin, String target, List<ClassDependency> classDependencies,
      boolean closesCycle, double originInstability, double targetInstability) {

    /**
     * Returns the number of pairs of classes.
     */
    int dependencies() {
      return this.classDependencies.size();
    }

    /**
     * Returns whether the target is less stable, against the stable dependencies principle.
     */
    boolean dependsOnLessStable() {
      return this.targetInstability > this.originInstability;
    }

    double instabilityIncrease() {
      return rounded(this.targetInstability - this.originInstability);
    }

    /**
     * Returns the kind, in which closing a cycle wins over depending on a less stable package.
     */
    Kind kind() {
      if (this.closesCycle) {
        return Kind.CLOSES_CYCLE;
      }

      return dependsOnLessStable() ? Kind.ON_LESS_STABLE : Kind.REGULAR;
    }

    /**
     * The kinds of dependencies in the order in which the charts draw them, the most severe on top.
     */
    enum Kind {
      REGULAR, ON_LESS_STABLE, CLOSES_CYCLE
    }
  }

  /**
   * A dependency of a top-level class on another, including their nested classes.
   */
  record ClassDependency(String origin, String target) {

  }

  /**
   * A library, named by the first two segments of its packages.
   */
  record Library(String name, int packages, int classes) {

  }

  /**
   * The top-level classes of a package that use a library.
   */
  record LibraryUsage(String packageName, String library, List<String> classes) {

  }

  /**
   * Packages in a cycle, with the dependencies that close it.
   */
  record PackageCycle(List<String> packages, List<PackageDependency> closingDependencies) {

  }

  /**
   * The packages of the top-level classes and the dependencies between them.
   */
  private static final class Packages {

    private final List<String> names;
    private final List<JavaClass> topLevelClasses;
    private final int[] packageOf;
    private final List<Map<Integer, List<ClassDependency>>> dependencies = new ArrayList<>();
    private final List<List<Integer>> components;
    private final int[] position;
    private final List<PackageMetric> metrics;

    /**
     * Visits the classes and their targets in the order of their names, so that the results do not
     * depend on the order of ArchUnit.
     */
    Packages(List<JavaClass> topLevelClasses, DependencyGraph classes) {
      this.topLevelClasses = topLevelClasses;
      this.names = topLevelClasses.stream()
          .map(JavaClass::getPackageName)
          .distinct()
          .sorted()
          .toList();

      Map<String, Integer> indexOf = DependencyGraph.indexes(this.names);
      this.packageOf = topLevelClasses.stream()
          .mapToInt(javaClass -> indexOf.get(javaClass.getPackageName()))
          .toArray();
      this.names.forEach(name -> this.dependencies.add(new TreeMap<>()));

      for (int origin = 0; origin < classes.nodes(); origin++) {
        for (int target : classes.targets(origin)) {
          if (this.packageOf[origin] != this.packageOf[target]) {
            this.dependencies.get(this.packageOf[origin])
                .computeIfAbsent(this.packageOf[target], key -> new ArrayList<>())
                .add(new ClassDependency(topLevelClasses.get(origin).getName(),
                    topLevelClasses.get(target).getName()));
          }
        }
      }

      DependencyGraph packages = new DependencyGraph(this.names.size());

      for (int origin = 0; origin < this.names.size(); origin++) {
        for (int target : this.dependencies.get(origin).keySet()) {
          packages.add(origin, target);
        }
      }

      this.components = packages.components();
      this.position = packages.layeredPositions(this.components);
      this.metrics = measure();
    }

    List<String> names() {
      return this.names;
    }

    List<PackageMetric> metrics() {
      return this.metrics;
    }

    private List<PackageMetric> measure() {
      int[] classes = new int[this.names.size()];
      int[] abstractClasses = new int[this.names.size()];
      int[] publicClasses = new int[this.names.size()];
      List<Set<String>> afferentClasses = new ArrayList<>();
      List<Set<String>> efferentClasses = new ArrayList<>();

      this.names.forEach(name -> {
        afferentClasses.add(new HashSet<>());
        efferentClasses.add(new HashSet<>());
      });

      for (int index = 0; index < this.topLevelClasses.size(); index++) {
        JavaClass javaClass = this.topLevelClasses.get(index);
        int packageIndex = this.packageOf[index];

        classes[packageIndex]++;

        if (javaClass.isInterface() || javaClass.getModifiers().contains(JavaModifier.ABSTRACT)) {
          abstractClasses[packageIndex]++;
        }

        if (javaClass.getModifiers().contains(JavaModifier.PUBLIC)) {
          publicClasses[packageIndex]++;
        }
      }

      for (int origin = 0; origin < this.names.size(); origin++) {
        for (Map.Entry<Integer, List<ClassDependency>> entry
            : this.dependencies.get(origin).entrySet()) {
          for (ClassDependency dependency : entry.getValue()) {
            efferentClasses.get(origin).add(dependency.origin());
            afferentClasses.get(entry.getKey()).add(dependency.origin());
          }
        }
      }

      List<PackageMetric> metrics = new ArrayList<>();

      for (int index = 0; index < this.names.size(); index++) {
        metrics.add(new PackageMetric(this.names.get(index), classes[index],
            abstractClasses[index], publicClasses[index], afferentClasses.get(index).size(),
            efferentClasses.get(index).size()));
      }

      return List.copyOf(metrics);
    }

    List<PackageDependency> dependencies() {
      List<PackageDependency> dependencies = new ArrayList<>();

      for (int origin = 0; origin < this.names.size(); origin++) {
        for (Map.Entry<Integer, List<ClassDependency>> entry
            : this.dependencies.get(origin).entrySet()) {
          int target = entry.getKey();

          dependencies.add(new PackageDependency(this.names.get(origin), this.names.get(target),
              List.copyOf(entry.getValue()), this.position[origin] > this.position[target],
              rounded(this.metrics.get(origin).instability().getAsDouble()),
              rounded(this.metrics.get(target).instability().getAsDouble())));
        }
      }

      return List.copyOf(dependencies);
    }

    List<String> layeredOrder() {
      String[] order = new String[this.names.size()];

      for (int index = 0; index < order.length; index++) {
        order[this.position[index]] = this.names.get(index);
      }

      return List.of(order);
    }

    /**
     * Returns the components of more than one package, with the dependencies that close them.
     */
    List<PackageCycle> cycles(List<PackageDependency> dependencies) {
      Map<String, List<PackageDependency>> closingByOrigin = dependencies.stream()
          .filter(PackageDependency::closesCycle)
          .collect(groupingBy(PackageDependency::origin));

      return this.components.stream()
          .filter(component -> component.size() > 1)
          .map(component -> component.stream().map(this.names::get).toList())
          .map(packages -> new PackageCycle(packages, packages.stream()
              .flatMap(name -> closingByOrigin.getOrDefault(name, List.of()).stream())
              .toList()))
          .sorted(comparing((PackageCycle cycle) -> cycle.packages().get(0)))
          .toList();
    }
  }
}
