package com.enofex.taikai.internal;

/**
 * The names of the annotations of Jakarta EE, and of their {@code javax} predecessors where the
 * rules support both, which the rules look for. They are names rather than classes, so that
 * Jakarta EE need not be on the classpath.
 */
public final class JakartaAnnotations {

  public static final String ANNOTATION_INJECT = "jakarta.inject.Inject";
  public static final String ANNOTATION_JAVAX_INJECT = "javax.inject.Inject";
  public static final String ANNOTATION_APPLICATION_SCOPED =
      "jakarta.enterprise.context.ApplicationScoped";
  public static final String ANNOTATION_TRANSACTIONAL = "jakarta.transaction.Transactional";
  public static final String ANNOTATION_PATH = "jakarta.ws.rs.Path";
  public static final String ANNOTATION_ENTITY = "jakarta.persistence.Entity";
  public static final String ANNOTATION_MAPPED_SUPERCLASS =
      "jakarta.persistence.MappedSuperclass";
  public static final String ANNOTATION_ID = "jakarta.persistence.Id";
  public static final String ANNOTATION_EMBEDDED_ID = "jakarta.persistence.EmbeddedId";
  public static final String ANNOTATION_VERSION = "jakarta.persistence.Version";
  public static final String ANNOTATION_CONSTRAINT = "jakarta.validation.Constraint";
  public static final String ANNOTATION_JAVAX_CONSTRAINT = "javax.validation.Constraint";
  public static final String ANNOTATION_VALID = "jakarta.validation.Valid";
  public static final String ANNOTATION_JAVAX_VALID = "javax.validation.Valid";

  private JakartaAnnotations() {
  }
}
