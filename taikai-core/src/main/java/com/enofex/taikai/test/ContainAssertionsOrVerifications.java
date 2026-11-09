package com.enofex.taikai.test;

import static com.enofex.taikai.internal.Classes.methodCallsAndReferences;

import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaCodeUnitAccess;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

final class ContainAssertionsOrVerifications {

  private ContainAssertionsOrVerifications() {
  }

  static ArchCondition<JavaMethod> containAssertionsOrVerifications() {
    return new ArchCondition<>("a unit test should assert or verify something") {
      @Override
      public void check(JavaMethod item, ConditionEvents events) {
        if (!assertsOrVerifies(item, new HashSet<>())) {
          events.add(SimpleConditionEvent.violated(
              item,
              "%s does not assert or verify anything".formatted(item.getDescription()))
          );
        }
      }

      private boolean assertsOrVerifies(JavaCodeUnit method, Set<JavaCodeUnit> visited) {
        if (!visited.add(method)) {
          return false;
        }

        List<JavaCodeUnitAccess<?>> calls = methodCallsAndReferences(method);

        for (JavaCodeUnitAccess<?> call : calls) {
          if (assertionOrVerification(call)) {
            return true;
          }
        }

        // Follow calls into imported methods, for example helper methods that assert on behalf of
        // the test
        for (JavaCodeUnitAccess<?> call : calls) {
          Optional<? extends JavaCodeUnit> target = call.getTarget().resolveMember();
          if (target.isPresent() && assertsOrVerifies(target.get(), visited)) {
            return true;
          }
        }

        return false;
      }

      private boolean assertionOrVerification(JavaCodeUnitAccess<?> call) {
        return junit(call) ||
            mockito(call) ||
            hamcrest(call) ||
            assertJ(call) ||
            truth(call) ||
            cucumber(call) ||
            springMockMvc(call) ||
            archRule(call) ||
            taikai(call);
      }

      private boolean junit(JavaCodeUnitAccess<?> call) {
        return declaredBy(call, "org.junit.jupiter.api.Assertions");
      }

      private boolean mockito(JavaCodeUnitAccess<?> call) {
        return declaredBy(call, "org.mockito.Mockito")
            && (call.getName().startsWith("verify")
            || "inOrder".equals(call.getName())
            || "capture".equals(call.getName()));
      }

      private boolean hamcrest(JavaCodeUnitAccess<?> call) {
        return declaredBy(call, "org.hamcrest.MatcherAssert");
      }

      private boolean assertJ(JavaCodeUnitAccess<?> call) {
        return declaredBy(call, "org.assertj.core.api.Assertions");
      }

      private boolean truth(JavaCodeUnitAccess<?> call) {
        return declaredBy(call, "com.google.common.truth.Truth");
      }

      private boolean cucumber(JavaCodeUnitAccess<?> call) {
        return declaredBy(call, "io.cucumber.java.en.Then") ||
            declaredBy(call, "io.cucumber.java.en.Given");
      }

      private boolean springMockMvc(JavaCodeUnitAccess<?> call) {
        return
            declaredBy(call, "org.springframework.test.web.servlet.ResultActions")
                && ("andExpect".equals(call.getName()) || "andExpectAll".equals(call.getName()));
      }

      private boolean archRule(JavaCodeUnitAccess<?> call) {
        // Fluent rules such as classes().should()...check(classes) are called through subtypes.
        return call.getTargetOwner().isAssignableTo("com.tngtech.archunit.lang.ArchRule")
            && "check".equals(call.getName());
      }

      private boolean taikai(JavaCodeUnitAccess<?> call) {
        return declaredBy(call, "com.enofex.taikai.Taikai")
            && ("check".equals(call.getName()) || "checkAll".equals(call.getName()));
      }

      /**
       * Returns whether the called method belongs to the given class, also when it is called
       * through a subclass, as {@code BDDMockito.verify} calls {@code Mockito.verify}.
       */
      private boolean declaredBy(JavaCodeUnitAccess<?> call, String className) {
        return className.equals(call.getTargetOwner().getName())
            || call.getTarget().resolveMember()
            .map(member -> className.equals(member.getOwner().getName()))
            .orElse(false);
      }
    };
  }
}
