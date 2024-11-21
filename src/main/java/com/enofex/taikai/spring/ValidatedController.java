package com.enofex.taikai.spring;

import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_VALIDATED;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class ValidatedController {

  private static final String REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";
  private static final String PATH_VARIABLE = "org.springframework.web.bind.annotation.PathVariable";

  private static final String JAVAX_VALIDATION_NOT_NULL = "javax.validation.constraints.NotNull";
  private static final String JAVAX_VALIDATION_MIN = "javax.validation.constraints.Min";
  private static final String JAVAX_VALIDATION_MAX = "javax.validation.constraints.Max";
  private static final String JAVAX_VALIDATION_SIZE = "javax.validation.constraints.Size";
  private static final String JAVAX_VALIDATION_NOT_BLANK = "javax.validation.constraints.NotBlank";
  private static final String JAVAX_VALIDATION_PATTERN = "javax.validation.constraints.Pattern";

  private static final String JAKARTA_VALIDATION_NOT_NULL = "jakarta.validation.constraints.NotNull";
  private static final String JAKARTA_VALIDATION_MIN = "jakarta.validation.constraints.Min";
  private static final String JAKARTA_VALIDATION_MAX = "jakarta.validation.constraints.Max";
  private static final String JAKARTA_VALIDATION_SIZE = "jakarta.validation.constraints.Size";
  private static final String JAKARTA_VALIDATION_NOT_BLANK = "jakarta.validation.constraints.NotBlank";
  private static final String JAKARTA_VALIDATION_PATTERN = "jakarta.validation.constraints.Pattern";

  private ValidatedController() {
  }

  static ArchCondition<JavaClass> beAnnotatedWithValidated() {
    return new ArchCondition<>(
        "be annotated with @Validated if @RequestParam or @PathVariable has validation annotations") {
      @Override
      public void check(JavaClass controllerClass, ConditionEvents events) {
        boolean hasValidatedAnnotation = controllerClass.isMetaAnnotatedWith(ANNOTATION_VALIDATED);

        for (JavaMethod method : controllerClass.getMethods()) {
          for (JavaParameter parameter : method.getParameters()) {
            if ((parameter.isMetaAnnotatedWith(REQUEST_PARAM)
                || parameter.isMetaAnnotatedWith(PATH_VARIABLE))
                && (hasJavaXValidationAnnotations(parameter)
                || hasJakartaValidationAnnotations(parameter))
            ) {

              if (!hasValidatedAnnotation) {
                events.add(SimpleConditionEvent.violated(controllerClass,
                    "Controller %s is missing @Validated but has a method parameter in %s annotated with @PathVariable or @RequestParam and a validation annotation.".formatted(
                        controllerClass.getName(),
                        method.getFullName())));
              }
            }
          }
        }
      }

      private boolean hasJavaXValidationAnnotations(JavaParameter parameter) {
        return parameter.isMetaAnnotatedWith(JAVAX_VALIDATION_NOT_NULL)
            || parameter.isMetaAnnotatedWith(JAVAX_VALIDATION_MIN)
            || parameter.isMetaAnnotatedWith(JAVAX_VALIDATION_MAX)
            || parameter.isMetaAnnotatedWith(JAVAX_VALIDATION_SIZE)
            || parameter.isMetaAnnotatedWith(JAVAX_VALIDATION_NOT_BLANK)
            || parameter.isMetaAnnotatedWith(JAVAX_VALIDATION_PATTERN);
      }

      private boolean hasJakartaValidationAnnotations(JavaParameter parameter) {
        return parameter.isMetaAnnotatedWith(JAKARTA_VALIDATION_NOT_NULL)
            || parameter.isMetaAnnotatedWith(JAKARTA_VALIDATION_MIN)
            || parameter.isMetaAnnotatedWith(JAKARTA_VALIDATION_MAX)
            || parameter.isMetaAnnotatedWith(JAKARTA_VALIDATION_SIZE)
            || parameter.isMetaAnnotatedWith(JAKARTA_VALIDATION_NOT_BLANK)
            || parameter.isMetaAnnotatedWith(JAKARTA_VALIDATION_PATTERN);
      }
    };
  }
}

