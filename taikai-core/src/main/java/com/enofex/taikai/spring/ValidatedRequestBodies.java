package com.enofex.taikai.spring;

import static com.enofex.taikai.internal.Modifiers.isStatic;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_MODEL_ATTRIBUTE;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_REQUEST_BODY;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_REQUEST_PART;
import static com.enofex.taikai.spring.SpringDescribedPredicates.ANNOTATION_VALIDATED;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.List;
import java.util.Optional;

/**
 * Checks that controller parameters which Spring binds and can validate, annotated with
 * {@code @RequestBody}, {@code @RequestPart} or {@code @ModelAttribute}, trigger the validation of
 * the constraints their type declares. Spring validates such a parameter only if one of its
 * annotations is {@code @Valid}, {@code @Validated}, is annotated with {@code @Validated} or has a
 * name starting with {@code Valid}, the same decision as Spring's
 * {@code ValidationAnnotationUtils}. Only annotations declared directly on the parameters of the
 * controller method are considered. Parameter annotations declared on overridden methods are
 * deliberately ignored, so the result is the same for every Spring version.
 */
final class ValidatedRequestBodies {

  private static final List<String> BINDINGS = List.of(
      ANNOTATION_REQUEST_BODY, ANNOTATION_REQUEST_PART, ANNOTATION_MODEL_ATTRIBUTE);
  private static final List<String> CONSTRAINTS = List.of(
      "jakarta.validation.Constraint", "javax.validation.Constraint");
  private static final List<String> CASCADES = List.of(
      "jakarta.validation.Valid", "javax.validation.Valid");

  private ValidatedRequestBodies() {
  }

  static ArchCondition<JavaMethod> validateConstrainedRequestBodies() {
    return new ArchCondition<>("validate request bodies whose type declares constraints") {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        for (JavaParameter parameter : method.getParameters()) {
          List<JavaClass> annotations = parameter.getAnnotations().stream()
              .map(JavaAnnotation::getRawType)
              .toList();
          Optional<JavaClass> binding = annotations.stream()
              .filter(annotation -> BINDINGS.contains(annotation.getName()))
              .findFirst();

          if (binding.isPresent()
              && annotations.stream().noneMatch(ValidatedRequestBodies::triggersValidation)
              && declaresConstraints(parameter.getRawType())) {
            events.add(SimpleConditionEvent.violated(method,
                ("Parameter %d of method %s is annotated with @%s but not with @Valid or "
                    + "@Validated, so the constraints of %s are not validated").formatted(
                    parameter.getIndex(),
                    method.getFullName(),
                    binding.get().getSimpleName(),
                    parameter.getRawType().getName())));
          }
        }
      }
    };
  }

  private static boolean triggersValidation(JavaClass annotation) {
    return annotation.getSimpleName().startsWith("Valid")
        || annotation.isMetaAnnotatedWith(ANNOTATION_VALIDATED);
  }

  /**
   * Returns whether Bean Validation finds anything to validate in the type, including what it
   * inherits from its superclasses and interfaces.
   */
  private static boolean declaresConstraints(JavaClass type) {
    return type.getAllClassesSelfIsAssignableTo().stream()
        .anyMatch(ValidatedRequestBodies::declaresOwnConstraints);
  }

  /**
   * Returns whether the class itself carries a constraint, or one of its instance fields or methods
   * without parameters carries a constraint or {@code @Valid}.
   */
  private static boolean declaresOwnConstraints(JavaClass javaClass) {
    return CONSTRAINTS.stream().anyMatch(javaClass::isMetaAnnotatedWith)
        || javaClass.getFields().stream().anyMatch(ValidatedRequestBodies::isValidated)
        || javaClass.getMethods().stream()
            .filter(method -> method.getRawParameterTypes().isEmpty())
            .anyMatch(ValidatedRequestBodies::isValidated);
  }

  private static boolean isValidated(JavaMember member) {
    return !isStatic(member)
        && (CONSTRAINTS.stream().anyMatch(member::isMetaAnnotatedWith)
        || CASCADES.stream().anyMatch(member::isAnnotatedWith));
  }
}
