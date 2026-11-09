package com.enofex.taikai.spring;

import static com.enofex.taikai.internal.Modifiers.isSynthetic;
import static com.enofex.taikai.spring.SpringDescribedPredicates.annotatedWithTransactional;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class TransactionalControllers {

  private TransactionalControllers() {
  }

  static ArchCondition<JavaClass> notBeTransactional() {
    DescribedPredicate<CanBeAnnotated> transactional = annotatedWithTransactional(true);

    return new ArchCondition<>(
        "not be annotated with @Transactional and not declare @Transactional methods") {
      @Override
      public void check(JavaClass controllerClass, ConditionEvents events) {
        if (transactional.test(controllerClass)) {
          events.add(SimpleConditionEvent.violated(controllerClass,
              ("Controller %s is annotated with @Transactional, transaction boundaries should be "
                  + "defined in the service layer").formatted(controllerClass.getName())));
        }

        for (JavaMethod method : controllerClass.getMethods()) {
          if (!isSynthetic(method) && transactional.test(method)) {
            events.add(SimpleConditionEvent.violated(controllerClass,
                ("Controller %s declares the @Transactional method %s, transaction boundaries "
                    + "should be defined in the service layer").formatted(
                    controllerClass.getName(),
                    method.getFullName())));
          }
        }
      }
    };
  }
}
