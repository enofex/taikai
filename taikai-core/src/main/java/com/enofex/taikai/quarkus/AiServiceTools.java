package com.enofex.taikai.quarkus;

import static com.enofex.taikai.quarkus.QuarkusDescribedPredicates.ANNOTATION_REGISTER_AI_SERVICE;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

final class AiServiceTools {

  private AiServiceTools() {
  }

  static ArchCondition<JavaClass> notUseToolsAttribute() {
    return new ArchCondition<>("not configure tools in @RegisterAiService") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        JavaAnnotation<JavaClass> annotation =
            javaClass.getAnnotationOfType(ANNOTATION_REGISTER_AI_SERVICE);
        JavaClass[] tools = (JavaClass[]) annotation.get("tools").get();

        if (tools.length > 0) {
          events.add(SimpleConditionEvent.violated(javaClass,
              "%s must not configure 'tools' in @RegisterAiService".formatted(
                  javaClass.getName())));
        }
      }
    };
  }
}
