package com.enofex.taikai.exports;

import static com.enofex.taikai.internal.Classes.bridgeTarget;
import static com.enofex.taikai.internal.Classes.signature;
import static com.enofex.taikai.internal.Modifiers.isAbstract;
import static com.enofex.taikai.internal.Modifiers.isBridge;
import static com.enofex.taikai.internal.Modifiers.isPrivate;
import static com.enofex.taikai.internal.Modifiers.isStatic;
import static com.enofex.taikai.internal.Modifiers.isSynthetic;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toMap;

import com.enofex.taikai.exports.ArchitectureMetrics.ClassMetric;
import com.enofex.taikai.exports.ArchitectureMetrics.EntryPointMetric;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Counts the methods of the concrete classes that a framework calls, each for its top-level
 * class, as the coupling counts. A class counts the methods it inherits as well, as the framework
 * calls them on its objects, while abstract classes and interfaces count none.
 */
final class EntryPointMethods {

  private EntryPointMethods() {
  }

  static List<EntryPointMetric> metrics(Map<String, JavaClass> measured,
      Map<String, String> topLevelClassOf, List<ClassMetric> classMetrics,
      List<EntryPoints> entryPoints) {
    if (entryPoints.isEmpty()) {
      return List.of();
    }

    Map<String, List<Set<String>>> kindsOf = new TreeMap<>();

    for (JavaClass javaClass : measured.values()) {
      if (!javaClass.isInterface() && !isAbstract(javaClass)) {
        Collection<Set<String>> kinds = entryPointKinds(javaClass, entryPoints);

        if (!kinds.isEmpty()) {
          kindsOf.computeIfAbsent(topLevelClassOf.get(javaClass.getName()),
              topLevelClass -> new ArrayList<>()).addAll(kinds);
        }
      }
    }

    Map<String, Integer> fanIn = classMetrics.stream()
        .collect(toMap(ClassMetric::name, ClassMetric::fanIn));

    return kindsOf.entrySet().stream()
        .map(entry -> new EntryPointMetric(entry.getKey(),
            measured.get(entry.getKey()).getPackageName(), fanIn.get(entry.getKey()),
            entry.getValue().size(),
            entry.getValue().stream()
                .flatMap(Set::stream)
                .collect(collectingAndThen(
                    groupingBy(identity(), TreeMap::new, summingInt(kind -> 1)),
                    Collections::unmodifiableSortedMap))))
        .toList();
  }

  /**
   * Returns the kinds of every entry point of the class.
   */
  private static Collection<Set<String>> entryPointKinds(JavaClass javaClass,
      List<EntryPoints> entryPoints) {
    Map<String, String> overridingSignatures = overridingSignatures(javaClass);
    Map<String, Set<String>> kindsBySignature = new HashMap<>();

    // from the class up through its superclasses and then its interfaces, so that the first
    // method of a signature that is an entry point counts, and the methods it overrides do not
    for (JavaMethod method : javaClass.getAllMethods()) {
      Set<String> kinds = isCalledOnObjects(javaClass, method)
          ? kinds(javaClass, method, entryPoints)
          : Set.of();

      if (!kinds.isEmpty()) {
        String signature = signature(method);

        kindsBySignature.putIfAbsent(overridingSignatures.getOrDefault(signature, signature),
            kinds);
      }
    }

    return kindsBySignature.values();
  }

  /**
   * Returns whether the framework can call the method on the objects of the class. Static methods
   * are called on no object, and bridge methods, which the compiler generates for generic types,
   * would count a method twice.
   */
  private static boolean isCalledOnObjects(JavaClass javaClass, JavaMethod method) {
    return !isStatic(method) && !isSynthetic(method)
        && (!isPrivate(method) || method.getOwner().equals(javaClass));
  }

  /**
   * Returns the signatures of the methods that override a method of a generic type with other
   * parameter types after erasure, by the signature of the overridden method, as their bridge
   * methods tell. A class that overrides such a method has a bridge method for every method that
   * it overrides, so the first one from the class up names the method that overrides it last.
   */
  private static Map<String, String> overridingSignatures(JavaClass javaClass) {
    Map<String, String> overridingSignatures = new HashMap<>();

    for (JavaMethod method : javaClass.getAllMethods()) {
      if (isBridge(method)) {
        bridgeTarget(method).ifPresent(target ->
            overridingSignatures.putIfAbsent(signature(method), signature(target)));
      }
    }

    return overridingSignatures;
  }

  private static Set<String> kinds(JavaClass javaClass, JavaMethod method,
      List<EntryPoints> entryPoints) {
    Set<String> kinds = new HashSet<>();

    for (EntryPoints entryPoint : entryPoints) {
      for (String kind : requireNonNull(entryPoint.kinds(javaClass, method),
          () -> nullKinds(method))) {
        kinds.add(requireNonNull(kind, () -> nullKinds(method)));
      }
    }

    return kinds;
  }

  private static String nullKinds(JavaMethod method) {
    return "The kinds of the entry points of %s must not be null".formatted(
        method.getFullName());
  }
}
