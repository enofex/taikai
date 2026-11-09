package com.enofex.taikai.java;

import static com.enofex.taikai.internal.Classes.constructorCallsAndReferences;
import static com.enofex.taikai.internal.Classes.methodCallsAndReferences;
import static com.enofex.taikai.internal.Classes.topLevelClass;

import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class Deprecations {

  private Deprecations() {
  }

  static ArchCondition<JavaClass> notUseDeprecatedAPIs() {
    return new ArchCondition<JavaClass>("not use deprecated APIs") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        javaClass.getFieldAccessesFromSelf().stream()
            .filter(access -> !isOwnMember(javaClass, access))
            .filter(access -> access.getTarget().isAnnotatedWith(Deprecated.class))
            .forEach(access -> events.add(SimpleConditionEvent.violated(access.getTarget(),
                "Field %s in class %s is deprecated and is being accessed by %s".formatted(
                    access.getTarget().getName(),
                    access.getTarget().getOwner().getName(),
                    javaClass.getName()))));

        // Method references such as Date::getYear use a method just like a call does.
        methodCallsAndReferences(javaClass).stream()
            .filter(method -> !isOwnMember(javaClass, method))
            .filter(method -> method.getTarget().isAnnotatedWith(Deprecated.class) ||
                method.getTarget().getRawReturnType().isAnnotatedWith(Deprecated.class) ||
                method.getTarget().getParameterTypes().stream()
                    .anyMatch(Deprecations::isDeprecated))
            .forEach(method -> events.add(SimpleConditionEvent.violated(method,
                "Method %s used in class %s is deprecated".formatted(
                    method.getName(),
                    javaClass.getName()))));

        constructorCallsAndReferences(javaClass).stream()
            .filter(constructor -> !isOwnMember(javaClass, constructor))
            .filter(constructor -> constructor.getTarget().isAnnotatedWith(Deprecated.class) ||
                constructor.getTarget().getParameterTypes().stream()
                    .anyMatch(Deprecations::isDeprecated))
            .forEach(constructor -> events.add(SimpleConditionEvent.violated(constructor,
                "Constructor %s in class %s uses deprecated APIs".formatted(
                    constructor.getTarget().getFullName(),
                    javaClass.getName()))));

        javaClass.getDirectDependenciesFromSelf().stream()
            .filter(dependency -> !isInSameTopLevelClass(javaClass, dependency.getTargetClass()))
            .filter(dependency -> dependency.getTargetClass().isAnnotatedWith(Deprecated.class))
            .forEach(dependency -> events.add(
                SimpleConditionEvent.violated(dependency.getTargetClass(),
                    "Class %s depends on deprecated class %s".formatted(
                        javaClass.getName(),
                        dependency.getTargetClass().getName()))));
      }
    }.as("no usage of deprecated APIs");
  }

  /**
   * Returns whether an access targets a member declared in the same top-level class, such as the
   * deprecated constant of an enum that its compiler-generated code accesses. As for the
   * deprecation warnings of javac, such uses do not count. The member is resolved, so that an
   * inherited deprecated method of another class still counts when it is called on {@code this}.
   */
  private static boolean isOwnMember(JavaClass javaClass, JavaAccess<?> access) {
    JavaClass declaringClass = access.getTarget().resolveMember()
        .<JavaClass>map(JavaMember::getOwner)
        .orElse(access.getTargetOwner());

    return isInSameTopLevelClass(javaClass, declaringClass);
  }

  private static boolean isInSameTopLevelClass(JavaClass javaClass, JavaClass other) {
    return topLevelClass(javaClass).getName().equals(topLevelClass(other).getName());
  }

  private static boolean isDeprecated(JavaType javaType) {
    return javaType.toErasure().isAnnotatedWith(Deprecated.class);
  }
}
