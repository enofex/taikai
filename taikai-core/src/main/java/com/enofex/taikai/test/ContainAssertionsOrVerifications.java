package com.enofex.taikai.test;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.HashSet;
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

      private boolean assertsOrVerifies(JavaMethod method, Set<JavaMethod> visited) {
        if (!visited.add(method)) {
          return false;
        }

        for (JavaMethodCall call : method.getMethodCallsFromSelf()) {
          if (assertionOrVerification(call)) {
            return true;
          }
        }

        // Follow calls into imported methods, e.g. helper methods that assert on behalf of the test
        for (JavaMethodCall call : method.getMethodCallsFromSelf()) {
          Optional<JavaMethod> target = call.getTarget().resolveMember();
          if (target.isPresent() && assertsOrVerifies(target.get(), visited)) {
            return true;
          }
        }

        return false;
      }

      private boolean assertionOrVerification(JavaMethodCall call) {
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

      private boolean junit(JavaMethodCall call) {
        return "org.junit.jupiter.api.Assertions".equals(call.getTargetOwner().getName());
      }

      private boolean mockito(JavaMethodCall call) {
        return "org.mockito.Mockito".equals(call.getTargetOwner().getName())
            && (call.getName().startsWith("verify")
            || "inOrder".equals(call.getName())
            || "capture".equals(call.getName()));
      }

      private boolean hamcrest(JavaMethodCall call) {
        return "org.hamcrest.MatcherAssert".equals(call.getTargetOwner().getName());
      }

      private boolean assertJ(JavaMethodCall call) {
        return "org.assertj.core.api.Assertions".equals(call.getTargetOwner().getName());
      }

      private boolean truth(JavaMethodCall call) {
        return "com.google.common.truth.Truth".equals(call.getTargetOwner().getName());
      }

      private boolean cucumber(JavaMethodCall call) {
        return "io.cucumber.java.en.Then".equals(call.getTargetOwner().getName()) ||
            "io.cucumber.java.en.Given".equals(call.getTargetOwner().getName());
      }

      private boolean springMockMvc(JavaMethodCall call) {
        return
            "org.springframework.test.web.servlet.ResultActions".equals(call.getTargetOwner().getName())
                && ("andExpect".equals(call.getName()) || "andExpectAll".equals(call.getName()));
      }

      private boolean archRule(JavaMethodCall call) {
        return "com.tngtech.archunit.lang.ArchRule".equals(call.getTargetOwner().getName())
            && "check".equals(call.getName());
      }

      private boolean taikai(JavaMethodCall call) {
        return "com.enofex.taikai.Taikai".equals(call.getTargetOwner().getName())
            && ("check".equals(call.getName()) || "checkAll".equals(call.getName())) ;
      }
    };
  }
}
