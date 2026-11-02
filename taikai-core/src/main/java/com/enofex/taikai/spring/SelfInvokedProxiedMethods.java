package com.enofex.taikai.spring;

import static com.enofex.taikai.internal.DescribedPredicates.annotatedWith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaCodeUnitAccess;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

final class SelfInvokedProxiedMethods {

  private SelfInvokedProxiedMethods() {
  }

  static ArchCondition<JavaClass> notSelfInvokeMethodsAnnotatedWith(
      Collection<String> annotations) {
    Map<String, DescribedPredicate<CanBeAnnotated>> predicates = new LinkedHashMap<>();

    for (String annotation : annotations) {
      predicates.put(annotation, annotatedWith(annotation, true));
    }

    return new ArchCondition<>("not self-invoke methods annotated with %s".formatted(
        String.join(" or ", annotations))) {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        for (JavaCodeUnitAccess<?> call : methodCallsAndReferences(javaClass)) {
          if (!call.getTargetOwner().equals(javaClass)
              || call.getOrigin().getModifiers().contains(JavaModifier.SYNTHETIC)) {
            continue;
          }

          Optional<? extends JavaCodeUnit> target = call.getTarget().resolveMember();

          if (target.isEmpty()) {
            continue;
          }

          for (Entry<String, DescribedPredicate<CanBeAnnotated>> predicate : predicates.entrySet()) {
            if (predicate.getValue().test(target.get())) {
              events.add(SimpleConditionEvent.violated(javaClass,
                  "Method %s calls %s in line %d, the self-invocation bypasses the Spring proxy and %s has no effect".formatted(
                      call.getOrigin().getFullName(),
                      target.get().getFullName(),
                      call.getLineNumber(),
                      predicate.getKey())));

              break;
            }
          }
        }
      }
    };
  }

  /**
   * Returns the method calls and the method references, such as {@code orders.forEach(this::save)},
   * which invoke the referenced method on {@code this} as well.
   */
  private static List<JavaCodeUnitAccess<?>> methodCallsAndReferences(JavaClass javaClass) {
    return Stream.<JavaCodeUnitAccess<?>>concat(javaClass.getMethodCallsFromSelf().stream(),
            javaClass.getMethodReferencesFromSelf().stream())
        .toList();
  }
}
