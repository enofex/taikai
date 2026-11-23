package com.enofex.taikai.spring;

import static com.enofex.taikai.internal.Modifiers.isSynthetic;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_PATH_VARIABLE;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_REQUEST_PARAM;
import static com.enofex.taikai.internal.SpringAnnotations.ANNOTATION_VALIDATED;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.List;
import java.util.stream.Stream;

final class ValidatedController {

  /**
   * The constraints of Bean Validation, in the packages of both {@code javax} and {@code jakarta}.
   */
  private static final List<String> VALIDATION_ANNOTATIONS = Stream.of("javax", "jakarta")
      .flatMap(namespace -> Stream.of("NotNull", "Min", "Max", "Size", "NotBlank", "Pattern")
          .map(constraint -> namespace + ".validation.constraints." + constraint))
      .toList();

  private ValidatedController() {
  }

  static ArchCondition<JavaClass> beAnnotatedWithValidated() {
    return new ArchCondition<>(
        "be annotated with @Validated if @RequestParam or @PathVariable has validation "
            + "annotations") {
      @Override
      public void check(JavaClass controllerClass, ConditionEvents events) {
        if (controllerClass.isMetaAnnotatedWith(ANNOTATION_VALIDATED)) {
          return;
        }

        for (JavaMethod method : controllerClass.getMethods()) {
          if (isSynthetic(method)) {
            continue;
          }

          for (JavaParameter parameter : method.getParameters()) {
            if ((parameter.isMetaAnnotatedWith(ANNOTATION_REQUEST_PARAM)
                || parameter.isMetaAnnotatedWith(ANNOTATION_PATH_VARIABLE))
                && VALIDATION_ANNOTATIONS.stream().anyMatch(parameter::isMetaAnnotatedWith)) {
              events.add(SimpleConditionEvent.violated(controllerClass,
                  ("Controller %s is missing @Validated but has a method parameter in %s "
                      + "annotated with @PathVariable or @RequestParam and a validation "
                      + "annotation").formatted(
                      controllerClass.getName(),
                      method.getFullName())));
            }
          }
        }
      }
    };
  }
}

