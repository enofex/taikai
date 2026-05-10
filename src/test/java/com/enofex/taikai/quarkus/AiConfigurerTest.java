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
  class AnnotatedWithApplicationScoped {

    @Test
    void shouldNotThrowWhenAiServiceIsAnnotatedWithApplicationScoped() {
      Taikai taikai = Taikai.builder()
          .classes(HelloService.class)
          .quarkus(quarkus -> quarkus.ai(AiConfigurer::annotatedWithApplicationScoped))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAiServiceIsNotAnnotatedWithApplicationScoped() {
      Taikai taikai = Taikai.builder()
          .classes(Hello.class)
          .quarkus(quarkus -> quarkus.ai(AiConfigurer::annotatedWithApplicationScoped))
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

  @Nested
  class NamesShouldMatch {

    @Test
    void shouldNotThrowWhenAiServiceNameMatchesRegex() {
      Taikai taikai = Taikai.builder()
          .classes(HelloService.class)
          .quarkus(quarkus -> quarkus.ai(
              ai -> ai.namesShouldMatch(".+(Service|Assistant)")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAiServiceNameDoesNotMatchRegex() {
      Taikai taikai = Taikai.builder()
          .classes(Hello.class)
          .quarkus(quarkus -> quarkus.ai(
              ai -> ai.namesShouldMatch(".+(Service|Assistant)")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class NotUseRegisterAiServiceToDefineTools {

    @Test
    void shouldNotThrowWhenAiServiceNotUsingTools() {
      Taikai taikai = Taikai.builder()
          .classes(HelloService.class)
          .quarkus(quarkus -> quarkus.ai(AiConfigurer::notUseRegisterAiServiceToDefineTools))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAiServiceUsingToolsAttribute() {
      Taikai taikai = Taikai.builder()
          .classes(Hello.class)
          .quarkus(quarkus -> quarkus.ai(AiConfigurer::notUseRegisterAiServiceToDefineTools))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ConfigurationOverloads {

    @Test
    void shouldSupportConfigurationForNamesShouldEndWithAssistantOrResource() {
      Taikai taikai = Taikai.builder()
          .classes(HelloService.class)
          .quarkus(quarkus -> quarkus.ai(
              ai -> ai.namesShouldEndWithAssistantOrResource(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForShouldBeAnnotatedWithApplicationScoped() {
      Taikai taikai = Taikai.builder()
          .classes(HelloService.class)
          .quarkus(quarkus -> quarkus.ai(
              ai -> ai.shouldBeAnnotatedWithApplicationScoped(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForAnnotatedWithApplicationScoped() {
      Taikai taikai = Taikai.builder()
          .classes(HelloService.class)
          .quarkus(quarkus -> quarkus.ai(
              ai -> ai.annotatedWithApplicationScoped(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableAiConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(Hello.class)
          .quarkus(quarkus -> quarkus.ai(ai -> {
            ai.namesShouldEndWithAssistantOrResource();
            ai.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
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
