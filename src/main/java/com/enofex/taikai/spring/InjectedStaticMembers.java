package com.enofex.taikai.spring;

import static com.enofex.taikai.internal.DescribedPredicates.annotatedWith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

final class InjectedStaticMembers {

  private InjectedStaticMembers() {
  }

  static ArchCondition<JavaMember> notBeAnnotatedWithAnyOf(Collection<String> annotations) {
    Map<String, DescribedPredicate<CanBeAnnotated>> predicates = new LinkedHashMap<>();

    for (String annotation : annotations) {
      predicates.put(annotation, annotatedWith(annotation, true));
    }

    return new ArchCondition<>("not be annotated with %s".formatted(
        String.join(" or ", annotations))) {
      @Override
      public void check(JavaMember member, ConditionEvents events) {
        for (Entry<String, DescribedPredicate<CanBeAnnotated>> predicate : predicates.entrySet()) {
          if (predicate.getValue().test(member)) {
            events.add(SimpleConditionEvent.violated(member,
                "%s is static and annotated with %s in %s".formatted(
                    member.getDescription(),
                    predicate.getKey(),
                    member.getSourceCodeLocation())));

            break;
          }
        }
      }
    };
  }
}
