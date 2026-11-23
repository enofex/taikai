package com.enofex.taikai.internal;

/**
 * The names of the annotations of Spring, which the rules and exports look for. They are names
 * rather than classes, so that Spring need not be on the classpath. Those of Jakarta EE that Spring
 * supports are in {@link JakartaAnnotations}.
 */
public final class SpringAnnotations {

  public static final String ANNOTATION_CONFIGURATION =
      "org.springframework.context.annotation.Configuration";
  public static final String ANNOTATION_CONFIGURATION_PROPERTIES =
      "org.springframework.boot.context.properties.ConfigurationProperties";
  public static final String ANNOTATION_CONTROLLER = "org.springframework.stereotype.Controller";
  public static final String ANNOTATION_REST_CONTROLLER =
      "org.springframework.web.bind.annotation.RestController";
  public static final String ANNOTATION_SERVICE = "org.springframework.stereotype.Service";
  public static final String ANNOTATION_REPOSITORY = "org.springframework.stereotype.Repository";
  public static final String ANNOTATION_DATA_REPOSITORY =
      "org.springframework.data.repository.Repository";
  public static final String ANNOTATION_SPRING_BOOT_APPLICATION =
      "org.springframework.boot.autoconfigure.SpringBootApplication";
  public static final String ANNOTATION_AUTOWIRED =
      "org.springframework.beans.factory.annotation.Autowired";
  public static final String ANNOTATION_VALUE =
      "org.springframework.beans.factory.annotation.Value";
  public static final String ANNOTATION_VALIDATED =
      "org.springframework.validation.annotation.Validated";
  public static final String ANNOTATION_REQUEST_MAPPING =
      "org.springframework.web.bind.annotation.RequestMapping";
  public static final String ANNOTATION_REQUEST_BODY =
      "org.springframework.web.bind.annotation.RequestBody";
  public static final String ANNOTATION_REQUEST_PART =
      "org.springframework.web.bind.annotation.RequestPart";
  public static final String ANNOTATION_REQUEST_PARAM =
      "org.springframework.web.bind.annotation.RequestParam";
  public static final String ANNOTATION_PATH_VARIABLE =
      "org.springframework.web.bind.annotation.PathVariable";
  public static final String ANNOTATION_MODEL_ATTRIBUTE =
      "org.springframework.web.bind.annotation.ModelAttribute";
  public static final String ANNOTATION_TRANSACTIONAL =
      "org.springframework.transaction.annotation.Transactional";
  public static final String ANNOTATION_ASYNC = "org.springframework.scheduling.annotation.Async";
  public static final String ANNOTATION_CACHEABLE =
      "org.springframework.cache.annotation.Cacheable";
  public static final String ANNOTATION_CACHE_EVICT =
      "org.springframework.cache.annotation.CacheEvict";
  public static final String ANNOTATION_CACHE_PUT =
      "org.springframework.cache.annotation.CachePut";
  public static final String ANNOTATION_PRE_AUTHORIZE =
      "org.springframework.security.access.prepost.PreAuthorize";
  public static final String ANNOTATION_POST_AUTHORIZE =
      "org.springframework.security.access.prepost.PostAuthorize";
  public static final String ANNOTATION_RETRYABLE =
      "org.springframework.retry.annotation.Retryable";

  private SpringAnnotations() {
  }
}
