package com.enofex.taikai.test;

import com.enofex.taikai.internal.Classes;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.regex.Pattern;

final class TestClassNaming {

  private TestClassNaming() {
  }

  /**
   * Creates an {@link ArchCondition} that resolves the top-level class of a possibly nested
   * class and fails if its name does not match the given regex.
   *
   * @param regex the regex pattern for valid test class names
   * @return the condition that checks the top-level class name
   */
  static ArchCondition<JavaClass> haveTopLevelClassNameMatching(String regex) {
    Pattern pattern = Pattern.compile(regex);

    return new ArchCondition<>("have a top-level class name matching %s".formatted(regex)) {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        JavaClass topLevelClass = Classes.topLevelClass(javaClass);

        if (!pattern.matcher(topLevelClass.getName()).matches()) {
          events.add(SimpleConditionEvent.violated(javaClass,
              "Class %s contains test methods but its top-level class %s does not match %s"
                  .formatted(javaClass.getName(), topLevelClass.getName(), regex)));
        }
      }
    };
  }
}
