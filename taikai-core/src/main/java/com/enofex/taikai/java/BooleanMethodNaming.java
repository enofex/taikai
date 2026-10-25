package com.enofex.taikai.java;

import static com.enofex.taikai.internal.Modifiers.isMethodSynthetic;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.regex.Pattern;

final class BooleanMethodNaming {

  private BooleanMethodNaming() {
  }

  static DescribedPredicate<JavaMethod> haveBooleanReturnType() {
    return new DescribedPredicate<>("have a boolean return type") {
      @Override
      public boolean test(JavaMethod method) {
        JavaClass rawReturnType = method.getRawReturnType();
        return rawReturnType.isEquivalentTo(boolean.class)
            || rawReturnType.isEquivalentTo(Boolean.class);
      }
    };
  }

  static ArchCondition<JavaMethod> shouldStartWith(Collection<String> prefixes) {
    Pattern pattern = Pattern.compile(
        "^(%s)([A-Z].*)?$".formatted(String.join("|", prefixes)));

    return new ArchCondition<>("start with one of %s".formatted(prefixes)) {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        if (!isMethodSynthetic(method)
            && !isEqualsMethod(method)
            && !isRecordComponentAccessor(method)
            && !pattern.matcher(method.getName()).matches()) {
          events.add(SimpleConditionEvent.violated(method,
              "Method %s in class %s returns a boolean but does not start with one of %s".formatted(
                  method.getName(),
                  method.getOwner().getName(),
                  prefixes)));
        }
      }
    };
  }

  private static boolean isEqualsMethod(JavaMethod method) {
    return "equals".equals(method.getName())
        && method.getRawParameterTypes().size() == 1
        && method.getRawParameterTypes().get(0).isEquivalentTo(Object.class);
  }

  private static boolean isRecordComponentAccessor(JavaMethod method) {
    return method.getOwner().isRecord()
        && method.getRawParameterTypes().isEmpty()
        && method.getOwner().tryGetField(method.getName()).isPresent();
  }
}
