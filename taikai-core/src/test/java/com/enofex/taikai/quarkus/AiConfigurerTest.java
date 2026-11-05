package com.enofex.taikai.quarkus;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class AiConfigurerTest {

  @Nested
  class NamesShouldEndWithAssistantOrService {

    static Stream<Named<Customizer<AiConfigurer>>> rules() {
      return Stream.of(
          Named.of("namesShouldEndWithAssistantOrService()",
              ai -> ai.namesShouldEndWithAssistantOrService()),
          Named.of("namesShouldEndWithAssistantOrService(Configuration)",
              ai -> ai.namesShouldEndWithAssistantOrService(defaultConfiguration())),
          Named.of("namesShouldMatch(String)",
              ai -> ai.namesShouldMatch(".+(Assistant|Service)")),
          Named.of("namesShouldMatch(String, Configuration)",
              ai -> ai.namesShouldMatch(".+(Assistant|Service)", defaultConfiguration())));
    }

    @Test
    void shouldNotThrowWhenAiServiceEndsWithAssistant() {
      Taikai taikai = Taikai.builder()
          .classes(HelloAssistant.class)
          .quarkus(quarkus -> quarkus.ai(AiConfigurer::namesShouldEndWithAssistantOrService))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenAiServiceEndsWithService(Customizer<AiConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(HelloService.class)
          .quarkus(quarkus -> quarkus.ai(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenAiServiceDoesNotEndWithAssistantOrService(Customizer<AiConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(Hello.class)
          .quarkus(quarkus -> quarkus.ai(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class AiServicesShouldBeAnnotatedWithApplicationScoped {

    static Stream<Named<Customizer<AiConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBeAnnotatedWithApplicationScoped()",
              ai -> ai.shouldBeAnnotatedWithApplicationScoped()),
          Named.of("shouldBeAnnotatedWithApplicationScoped(Configuration)",
              ai -> ai.shouldBeAnnotatedWithApplicationScoped(defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenAiServiceIsAnnotatedWithApplicationScoped(
        Customizer<AiConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(HelloService.class)
          .quarkus(quarkus -> quarkus.ai(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenAiServiceIsNotAnnotatedWithApplicationScoped(
        Customizer<AiConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(Hello.class)
          .quarkus(quarkus -> quarkus.ai(rule))
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
  class Disable {

    @Test
    void shouldDisableAiConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(Hello.class)
          .quarkus(quarkus -> quarkus.ai(ai -> {
            ai.namesShouldEndWithAssistantOrService();
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
