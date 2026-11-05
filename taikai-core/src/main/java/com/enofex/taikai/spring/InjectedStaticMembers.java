package com.enofex.taikai.spring;

import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;

final class InjectedStaticMembers {

  private InjectedStaticMembers() {
  }

  static ArchCondition<JavaMember> notBeAnnotatedWithAnyOf(Collection<String> annotations) {
    return new ArchCondition<>("not be annotated with %s".formatted(
        String.join(" or ", annotations))) {
      @Override
      public void check(JavaMember member, ConditionEvents events) {
        annotations.stream()
            .filter(member::isMetaAnnotatedWith)
            .findFirst()
            .ifPresent(annotation -> events.add(SimpleConditionEvent.violated(member,
                "%s is static and annotated with %s in %s".formatted(
                    member.getDescription(),
                    annotation,
                    member.getSourceCodeLocation()))));
      }
    };
  }
}
