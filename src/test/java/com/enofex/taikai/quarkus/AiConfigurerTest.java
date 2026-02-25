package com.enofex.taikai.quarkus;

import com.enofex.taikai.Taikai;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AiConfigurerTest {

  @Nested
  class NamesShouldEndWithAssistantOrService {
    @Test
    void shouldNotThrowWhenAiServiceEndsWithAssistant() {
      Taikai taikai = Taikai.builder()
          .classes(HelloAssistant.class)
          .quarkus(quarkus -> quarkus.ai(AiConfigurer::namesShouldEndWithAssistantOrResource))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenAiServiceEndsWithService() {
      Taikai taikai = Taikai.builder()
          .classes(HelloService.class)
          .quarkus(quarkus -> quarkus.ai(AiConfigurer::namesShouldEndWithAssistantOrResource))
          .build();

      assertDoesNotThrow(taikai::check);
    }


    @Test
    void shouldThrowWhenAiServiceNotEndsWithAssistantOrService() {
      Taikai taikai = Taikai.builder()
          .classes(Hello.class)
          .quarkus(quarkus -> quarkus.ai(AiConfigurer::namesShouldEndWithAssistantOrResource))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class AiServicesShouldBeAnnotatedWithApplicationScoped {

    @Test
    void shouldNotThrowWhenAiServiceIsAnnotatedWithApplicationScoped() {
      Taikai taikai = Taikai.builder()
          .classes(HelloService.class)
          .quarkus(quarkus -> quarkus.ai(AiConfigurer::shouldBeAnnotatedWithApplicationScoped))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAiServiceIsAnnotatedWithApplicationScoped() {
      Taikai taikai = Taikai.builder()
          .classes(Hello.class)
          .quarkus(quarkus -> quarkus.ai(AiConfigurer::shouldBeAnnotatedWithApplicationScoped))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class AiServicesShouldNotUseToolsToRegisterTools {
    @Test
    void shouldNotThrowWhenAiServiceIsNotUsingTools() {
      Taikai taikai = Taikai.builder()
          .classes(HelloService.class)
          .quarkus(quarkus -> quarkus.ai(AiConfigurer::shouldNotUseToolsAttributeInAiService))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAiServiceIsUsingTools() {
      Taikai taikai = Taikai.builder()
          .classes(Hello.class)
          .quarkus(quarkus -> quarkus.ai(AiConfigurer::shouldNotUseToolsAttributeInAiService))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

  }

  @RegisterAiService
  @ApplicationScoped
  public interface HelloService {
  }

  @RegisterAiService
  @ApplicationScoped
  public interface HelloAssistant {
  }

  @RegisterAiService(tools = Hello.class)
  public interface Hello {
  }

}
