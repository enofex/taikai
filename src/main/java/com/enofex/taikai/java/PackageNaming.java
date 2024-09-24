package com.enofex.taikai.java;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.regex.Pattern;

final class PackageNaming {

  private PackageNaming() {
  }

  static ArchCondition<JavaClass> resideInPackageWithProperNamingConvention(String regex) {
    return new ArchCondition<>("reside in package with proper naming convention") {
      private final Pattern pattern = Pattern.compile(regex);

      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        String packageName = javaClass.getPackageName();
        if (!this.pattern.matcher(packageName).matches()) {
          events.add(SimpleConditionEvent.violated(javaClass,
              "Package '%s' does not follow the naming convention".formatted(
                  packageName)));
        }
      }
    };
  }
}