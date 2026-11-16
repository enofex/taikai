package com.enofex.taikai.exports;

import static com.enofex.taikai.internal.Modifiers.isStatic;
import static com.enofex.taikai.internal.Modifiers.isSynthetic;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import com.enofex.taikai.exports.ArchitectureMetrics.CohesionMetric;
import com.enofex.taikai.internal.Classes;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Measures LCOM4 of Hitz and Montazeri: the number of groups of methods that are connected by using
 * the same field or method of their object.
 *
 * <p>Only instance methods with a body count, but not getters, setters, generated methods and
 * methods connected to nothing. Generated methods connect others, generated fields such as
 * {@code this$0} do not.</p>
 */
final class Cohesion {

  /**
   * Methods with these modifiers connect others, but do not count.
   */
  private static final Set<JavaModifier> WITHOUT_OBJECT_OR_BODY = Set.of(JavaModifier.STATIC,
      JavaModifier.ABSTRACT, JavaModifier.NATIVE);

  private Cohesion() {
  }

  /**
   * Returns the cohesion of the classes but for interfaces, records, anonymous classes and classes
   * without a counted method.
   */
  static List<CohesionMetric> of(Collection<JavaClass> classes) {
    return classes.stream()
        .filter(javaClass -> !javaClass.isInterface() && !javaClass.isRecord()
            && !javaClass.isAnonymousClass())
        .map(Cohesion::measure)
        .flatMap(Optional::stream)
        .toList();
  }

  private static Optional<CohesionMetric> measure(JavaClass javaClass) {
    Map<String, JavaField> instanceFields = new HashMap<>();
    javaClass.getFields().stream()
        .filter(field -> !isStatic(field) && !isSynthetic(field))
        .forEach(field -> instanceFields.put(field.getFullName(), field));
    Map<JavaMethod, Set<String>> membersOf = new HashMap<>();
    javaClass.getMethods()
        .forEach(method -> membersOf.put(method, usedMembers(method, javaClass)));
    List<JavaMethod> methods = javaClass.getMethods().stream()
        .filter(method -> !isSynthetic(method))
        .toList();

    List<String> ownMembers = Stream.concat(methods.stream().map(JavaMethod::getFullName),
        instanceFields.keySet().stream()).toList();
    Map<String, Integer> nodeOf = DependencyGraph.indexes(Stream.of(ownMembers.stream(),
            membersOf.keySet().stream().map(JavaMethod::getFullName),
            membersOf.values().stream().flatMap(Set::stream))
        .flatMap(identity())
        .distinct()
        .toList());
    int[] groupOf = groups(nodeOf, membersOf);

    Map<Integer, Long> ownMembersOf = ownMembers.stream()
        .collect(groupingBy(member -> groupOf[nodeOf.get(member)], counting()));
    List<Integer> groupsOfCounted = methods.stream()
        .filter(method -> Collections.disjoint(method.getModifiers(), WITHOUT_OBJECT_OR_BODY))
        .filter(method -> !isAccessor(method, membersOf.get(method), instanceFields))
        .map(method -> groupOf[nodeOf.get(method.getFullName())])
        .filter(group -> ownMembersOf.get(group) > 1)
        .toList();

    return groupsOfCounted.isEmpty()
        ? Optional.empty()
        : Optional.of(new CohesionMetric(javaClass.getName(), groupsOfCounted.size(),
            (int) groupsOfCounted.stream().distinct().count()));
  }

  /**
   * Returns the group of every node: the components of the uses in both directions.
   */
  private static int[] groups(Map<String, Integer> nodeOf, Map<JavaMethod, Set<String>> membersOf) {
    DependencyGraph uses = new DependencyGraph(nodeOf.size());

    membersOf.forEach((method, members) -> members.forEach(member -> {
      uses.add(nodeOf.get(method.getFullName()), nodeOf.get(member));
      uses.add(nodeOf.get(member), nodeOf.get(method.getFullName()));
    }));

    List<List<Integer>> components = uses.components();
    int[] groupOf = new int[nodeOf.size()];

    for (int group = 0; group < components.size(); group++) {
      for (int node : components.get(group)) {
        groupOf[node] = group;
      }
    }

    return groupOf;
  }

  /**
   * Returns whether a method is a getter or setter: it uses at most one field and is named like
   * one, such as {@code getName}, or like its field, such as {@code name()}.
   */
  private static boolean isAccessor(JavaMethod method, Set<String> members,
      Map<String, JavaField> instanceFields) {
    if (members.size() > 1 || !instanceFields.keySet().containsAll(members)) {
      return false;
    }

    String name = method.getName();

    if (Stream.of("get", "is", "set").anyMatch(prefix -> name.length() > prefix.length()
        && name.startsWith(prefix) && Character.isUpperCase(name.charAt(prefix.length())))) {
      return true;
    }

    if (members.isEmpty() || !Classes.methodCallsAndReferences(method).isEmpty()) {
      return false;
    }

    JavaField field = instanceFields.get(members.iterator().next());
    List<JavaClass> parameters = method.getRawParameterTypes();
    boolean getter = parameters.isEmpty() && method.getRawReturnType().equals(field.getRawType());
    boolean setter = parameters.size() == 1 && parameters.get(0).equals(field.getRawType())
        && method.getRawReturnType().isEquivalentTo(void.class);

    return name.equals(field.getName()) && (getter || setter);
  }

  /**
   * Returns the members of its class that a method uses, also inherited ones and those in lambdas.
   * Static and generated fields, methods of {@code Object} and members used through {@code super}
   * do not count, as ArchUnit cannot tell the latter from those of other objects.
   */
  private static Set<String> usedMembers(JavaMethod method, JavaClass javaClass) {
    return Stream.<JavaAccess<?>>concat(method.getFieldAccesses().stream(),
            Classes.methodCallsAndReferences(method).stream())
        .filter(access -> access.getTargetOwner().equals(javaClass))
        .map(access -> access.getTarget().resolveMember())
        .flatMap(Optional::stream)
        .filter(member -> !member.getOwner().isEquivalentTo(Object.class)
            && !(member instanceof JavaField field && (isStatic(field) || isSynthetic(field))))
        .map(JavaMember::getFullName)
        .collect(toSet());
  }
}
