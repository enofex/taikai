package com.enofex.taikai.spring;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.lang.reflect.Method;
import java.util.Optional;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionAttribute;

/**
 * Checks the transaction attribute that Spring's own {@link AnnotationTransactionAttributeSource}
 * derives for a method, configured like Spring 6 and later for class-based proxies, the default in
 * Spring Boot, which also apply transactions to non-public methods. This is the transaction
 * metadata, not proof that a call is intercepted: self-invocations and calls to final methods
 * bypass the proxy. Requires spring-tx and spring-context on the classpath, which Taikai declares
 * as optional dependencies. Spring's types are only loaded when the rule is created.
 */
final class ReadOnlyTransactionalMethods {

  private ReadOnlyTransactionalMethods() {
  }

  static ArchCondition<JavaMethod> beReadOnly() {
    TransactionAttributes transactionAttributes;

    try {
      transactionAttributes = new TransactionAttributes();
    } catch (LinkageError e) {
      throw new IllegalStateException(
          "methodsShouldBeReadOnly requires spring-tx and spring-context on the classpath", e);
    }

    return new ArchCondition<>("be read-only") {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        transactionAttributes.violation(method)
            .ifPresent(message -> events.add(SimpleConditionEvent.violated(method, message)));
      }
    };
  }

  /**
   * Spring's lookup, subclassed to tell for the message whether the transaction is declared on the
   * method or on its class.
   */
  private static final class TransactionAttributes extends AnnotationTransactionAttributeSource {

    TransactionAttributes() {
      super(false);
    }

    Optional<String> violation(JavaMethod javaMethod) {
      try {
        return violation(javaMethod, javaMethod.getOwner().reflect(), javaMethod.reflect());
      } catch (RuntimeException | LinkageError e) {
        return Optional.of("Could not resolve the transaction of method %s: %s".formatted(
            javaMethod.getFullName(), e));
      }
    }

    private Optional<String> violation(JavaMethod javaMethod, Class<?> type, Method method) {
      TransactionAttribute attribute = getTransactionAttribute(method, type);

      if (attribute == null || attribute.isReadOnly()) {
        return Optional.empty();
      }

      String prefix = "Method %s runs in a transaction that is not read-only (%s)".formatted(
          javaMethod.getFullName(), attribute);

      // Spring's first lookup, which also searches the methods that the method overrides
      if (findTransactionAttribute(method) == null) {
        return Optional.of("%s, declared on %s or one of its supertypes".formatted(prefix,
            type.getName()));
      }

      TransactionAttribute classAttribute = findTransactionAttribute(type);
      String replaced = classAttribute != null && classAttribute.isReadOnly()
          ? ", which replaces the read-only transaction declared on its class"
          : "";

      return Optional.of("%s, declared on the method or a method it overrides%s".formatted(prefix,
          replaced));
    }
  }
}
