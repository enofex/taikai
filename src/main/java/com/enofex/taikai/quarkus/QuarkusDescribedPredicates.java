package com.enofex.taikai.quarkus;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.enofex.taikai.internal.DescribedPredicates.annotatedWith;

final class QuarkusDescribedPredicates {


  static final String ANNOTATION_INJECT = "jakarta.inject.Inject";
  static final String ANNOTATION_PATH = "jakarta.ws.rs.Path";
  static final String ANNOTATION_ENTITY = "jakarta.persistence.Entity";
  static final String PANACHE_REPOSITORY_INTERFACE = "io.quarkus.hibernate.orm.panache.PanacheRepository";
  static final String PANACHE_ENTITY = "io.quarkus.hibernate.orm.panache.PanacheEntity";
  static final String LANGCHAIN4J_AI_SERVICE = "io.quarkiverse.langchain4j.RegisterAiService";
  static final String APPLICATION_SCOPED = "jakarta.enterprise.context.ApplicationScoped";

  private QuarkusDescribedPredicates() {
  }

  static ArchCondition<JavaClass> notUseToolsAttribute() {
    return new ArchCondition<>("not define model in @RegisterAiService") {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {

        JavaAnnotation<JavaClass> annotation =
            javaClass.getAnnotationOfType(LANGCHAIN4J_AI_SERVICE);

        JavaClass[] toolsValue =
            (JavaClass[]) annotation.get("tools").get();

        events.add(new SimpleConditionEvent(
            javaClass,
            toolsValue.length == 0,
            javaClass.getName() +
                " must not configure 'tools' in @RegisterAiService"
        ));
      }
    };
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithApplicationScope(boolean isMetaAnnotated) {
    return annotatedWith(APPLICATION_SCOPED, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithRegisterAiService(boolean isMetaAnnotated) {
    return annotatedWith(LANGCHAIN4J_AI_SERVICE, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithEntity(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_ENTITY, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithInject(boolean isMetaAnnotated) {
    return annotatedWith(ANNOTATION_INJECT, isMetaAnnotated);
  }

  static DescribedPredicate<CanBeAnnotated> annotatedWithPath(
      boolean isMetaAnnotated) {

    return annotatedWith(ANNOTATION_PATH, isMetaAnnotated);
  }
}
