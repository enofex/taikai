package com.enofex.taikai.spring;

import static com.enofex.taikai.internal.DescribedPredicates.annotatedWith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

final class SelfInvokedProxiedMethods {

  private SelfInvokedProxiedMethods() {
  }

  static ArchCondition<JavaClass> notSelfInvokeMethodsAnnotatedWith(
      Collection<String> annotations) {
    Map<String, DescribedPredicate<CanBeAnnotated>> predicates = new LinkedHashMap<>();

    for (String annotation : annotations) {
      predicates.put(annotation, annotatedWith(annotation, true));
    }

    return new ArchCondition<>("not self invoke methods annotated with %s".formatted(annotations)) {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        for (JavaMethodCall call : javaClass.getMethodCallsFromSelf()) {
          if (!call.getTargetOwner().equals(javaClass)) {
            continue;
          }

          Optional<JavaMethod> target = call.getTarget().resolveMember();

          if (target.isEmpty()) {
            continue;
          }

          for (Entry<String, DescribedPredicate<CanBeAnnotated>> predicate : predicates.entrySet()) {
            if (predicate.getValue().test(target.get())) {
              events.add(SimpleConditionEvent.violated(javaClass,
                  "Method %s calls %s in line %d, the self invocation bypasses the Spring proxy and %s has no effect.".formatted(
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
}
