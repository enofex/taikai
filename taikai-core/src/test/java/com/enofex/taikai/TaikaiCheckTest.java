package com.enofex.taikai;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.ForwardingCheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import com.enofex.taikai.checks.spi.TestChecks;
import com.enofex.taikai.java.JavaConfigurer;
import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TaikaiCheckTest {

  private static final String NAMESPACE = "com.enofex.taikai";
  private static final String SERVICES = "META-INF/services/" + TaikaiCheck.class.getName();

  @TempDir
  private Path directory;

  @BeforeEach
  void setUp() {
    ArchConfiguration.get().reset();
    TestChecks.ContextCapturing.reset();
  }

  @Test
  void shouldNotDiscoverChecksByDefault() {
    Taikai taikai = build(Taikai.builder().namespace(NAMESPACE), TestChecks.Passing.class);

    assertTrue(taikai.checks().isEmpty());
  }

  @Test
  void shouldDiscoverRegisteredChecksSortedByDescription() {
    Taikai taikai = discover(TestChecks.Passing.class, TestChecks.Failing.class);

    assertEquals(List.of(TestChecks.Failing.class, TestChecks.Passing.class),
        classes(taikai.checks()));
  }

  @Test
  void shouldExcludeChecksWithoutInstantiatingThem() {
    Taikai taikai = build(Taikai.builder()
            .namespace(NAMESPACE)
            .discoverChecks(checks -> checks
                .exclude(TestChecks.Failing.class, TestChecks.NotInstantiable.class)),
        TestChecks.Passing.class, TestChecks.Failing.class, TestChecks.NotInstantiable.class);

    assertEquals(List.of(TestChecks.Passing.class), classes(taikai.checks()));
  }

  @Test
  void shouldAddUpSettingsOfRepeatedDiscoverChecksCalls() {
    Taikai taikai = build(Taikai.builder()
            .namespace(NAMESPACE)
            .discoverChecks(checks -> checks.exclude(TestChecks.Failing.class))
            .discoverChecks(checks -> checks.exclude(TestChecks.AnotherFailing.class))
            .discoverChecks(),
        TestChecks.Passing.class, TestChecks.Failing.class, TestChecks.AnotherFailing.class);

    assertEquals(List.of(TestChecks.Passing.class), classes(taikai.checks()));
  }

  @Test
  void shouldFailBuildWhenExcludedCheckIsNotRegistered() {
    Taikai.Builder builder = Taikai.builder()
        .namespace(NAMESPACE)
        .discoverChecks(checks -> checks.exclude(TestChecks.Failing.class));

    TaikaiException exception = assertThrows(TaikaiException.class,
        () -> build(builder, TestChecks.Passing.class));

    assertTrue(exception.getMessage().contains("Check " + TestChecks.Failing.class.getName()
        + " is excluded, but not registered"), exception.getMessage());
  }

  @Test
  void shouldThrowWhenRegisteredCheckCannotBeLoaded() {
    Taikai.Builder builder = Taikai.builder()
        .namespace(NAMESPACE)
        .discoverChecks();

    TaikaiException exception = assertThrows(TaikaiException.class,
        () -> build(builder, "com.example.MissingCheck"));

    assertInstanceOf(ServiceConfigurationError.class, exception.getCause());
  }

  @Test
  void shouldThrowWhenRegisteredCheckCannotBeInstantiated() {
    Taikai.Builder builder = Taikai.builder()
        .namespace(NAMESPACE)
        .discoverChecks();

    assertThrows(TaikaiException.class,
        () -> build(builder, TestChecks.NotInstantiable.class.getName()));
  }

  @Test
  void shouldPassWhenChecksPass() {
    Taikai taikai = discover(TestChecks.Passing.class);

    assertDoesNotThrow(taikai::checkAll);
    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldReportCheckViolationsInCheckAll() {
    Taikai taikai = discover(TestChecks.Failing.class, TestChecks.AnotherFailing.class);

    AssertionError error = assertThrows(AssertionError.class, taikai::checkAll);

    assertTrue(error.getMessage().startsWith("Found 3 Taikai violations for 2 checks!"),
        error.getMessage());
    assertTrue(error.getMessage().contains("Check: A failing check"
        + System.lineSeparator() + "\tfirst violation"
        + System.lineSeparator() + "\tsecond violation"), error.getMessage());
    assertTrue(error.getMessage().contains("Check: Another failing check"
        + System.lineSeparator() + "\tthird violation"), error.getMessage());
  }

  @Test
  void shouldCountViolatedRulesAndChecksSeparately() {
    Taikai taikai = build(Taikai.builder()
            .namespace(NAMESPACE)
            .addRule(TaikaiRule.of(ArchRuleDefinition.classes()
                .that().haveSimpleName("Taikai")
                .should().haveSimpleName("Other")))
            .discoverChecks(),
        TestChecks.AnotherFailing.class);

    AssertionError error = assertThrows(AssertionError.class, taikai::checkAll);

    assertTrue(error.getMessage().startsWith("Found 2 Taikai violations for 1 rules and 1 checks!"),
        error.getMessage());
    assertTrue(error.getMessage().contains("Rule: classes that have simple name 'Taikai'"),
        error.getMessage());
    assertTrue(error.getMessage().contains("Check: Another failing check"), error.getMessage());
  }

  @Test
  void shouldKeepReportOfRulesWithoutViolatedChecks() {
    Taikai taikai = build(Taikai.builder()
            .namespace(NAMESPACE)
            .addRule(TaikaiRule.of(ArchRuleDefinition.classes()
                .that().haveSimpleName("Taikai")
                .should().haveSimpleName("Other")))
            .discoverChecks(),
        TestChecks.Passing.class);

    AssertionError error = assertThrows(AssertionError.class, taikai::checkAll);

    assertTrue(error.getMessage().startsWith("Found 1 Taikai violations for 1 rules!"),
        error.getMessage());
  }

  @Test
  void shouldFailCheckOnFirstViolatedCheck() {
    Taikai taikai = discover(TestChecks.AnotherFailing.class, TestChecks.Failing.class);

    AssertionError error = assertThrows(AssertionError.class, taikai::check);

    assertEquals("Check 'A failing check' was violated (2 times):"
        + System.lineSeparator() + "first violation"
        + System.lineSeparator() + "second violation", error.getMessage());
  }

  @Test
  void shouldReportExceptionOfCheckAsViolationAndRunOtherChecks() {
    Taikai taikai = discover(TestChecks.Throwing.class, TestChecks.Failing.class);

    AssertionError error = assertThrows(AssertionError.class, taikai::checkAll);

    assertTrue(error.getMessage().contains("Check: A throwing check"
        + System.lineSeparator()
        + "\tCheck failed with java.lang.IllegalStateException: broken"), error.getMessage());
    assertTrue(error.getMessage().contains("Check: A failing check"), error.getMessage());
  }

  @Test
  void shouldReportFailedAssertionAndMissingClassAsViolationsAndRunOtherChecks() {
    Taikai taikai = discover(TestChecks.AssertionFailing.class, TestChecks.ClassMissing.class,
        TestChecks.Failing.class);

    AssertionError error = assertThrows(AssertionError.class, taikai::checkAll);

    assertTrue(error.getMessage().contains("Check: A check with a failing assertion"
        + System.lineSeparator()
        + "\tCheck failed with java.lang.AssertionError: README.md expected"), error.getMessage());
    assertTrue(error.getMessage().contains("Check: A check with a missing class"
        + System.lineSeparator()
        + "\tCheck failed with java.lang.NoClassDefFoundError: com/example/Missing"),
        error.getMessage());
    assertTrue(error.getMessage().contains("Check: A failing check"), error.getMessage());
  }

  @Test
  void shouldRunChecksAfterRulesInDynamicTests() throws Throwable {
    List<DynamicTest> tests = build(Taikai.builder()
            .namespace(NAMESPACE)
            .java(JavaConfigurer::noUsageOfSystemOutOrErr)
            .discoverChecks(),
        TestChecks.Passing.class, TestChecks.Failing.class)
        .dynamicTests()
        .toList();

    assertEquals(List.of(
            "Classes should not use System.out or System.err",
            "A failing check",
            "A passing check"),
        tests.stream().map(DynamicTest::getDisplayName).toList());

    tests.get(2).getExecutable().execute();
    assertThrows(AssertionError.class, () -> tests.get(1).getExecutable().execute());
  }

  @Test
  void shouldProvideContextToChecks() {
    build(Taikai.builder()
            .namespace(NAMESPACE)
            .excludeClasses(PerCheckContext.class)
            .discoverChecks(checks -> checks.projectDirectory(this.directory)),
        TestChecks.ContextCapturing.class)
        .check();

    CheckContext context = TestChecks.ContextCapturing.lastContext();
    assertEquals(this.directory.toAbsolutePath(), context.projectDirectory());
    assertEquals(NAMESPACE, context.namespace());
    assertSame(Thread.currentThread().getContextClassLoader(), context.classLoader());
    assertTrue(context.classes().contain(Taikai.class));
    assertFalse(context.classes().contain(PerCheckContext.class));
    assertFalse(context.classes().contain(TaikaiCheckTest.class));
  }

  @Test
  void shouldProvideConfiguredClassesToChecks() {
    build(Taikai.builder()
            .classes(Taikai.class)
            .discoverChecks(),
        TestChecks.ContextCapturing.class)
        .check();

    CheckContext context = TestChecks.ContextCapturing.lastContext();
    assertNull(context.namespace());
    assertEquals(1, context.classes().size());
  }

  @Test
  void shouldUseWorkingDirectoryAsDefaultProjectDirectory() {
    discover(TestChecks.ContextCapturing.class).check();

    assertEquals(Path.of("").toAbsolutePath(),
        TestChecks.ContextCapturing.lastContext().projectDirectory());
  }

  @Test
  void shouldCreateContextOnce() {
    Taikai taikai = discover(TestChecks.ContextCapturing.class);

    taikai.check();
    CheckContext context = TestChecks.ContextCapturing.lastContext();
    taikai.checkAll();

    assertSame(context, TestChecks.ContextCapturing.lastContext());
  }

  @Test
  void shouldReportCheckThatNeedsClassesWithoutNamespaceAsViolation() {
    Taikai taikai = build(Taikai.builder().discoverChecks(), TestChecks.ClassesCounting.class);

    AssertionError error = assertThrows(AssertionError.class, taikai::checkAll);

    assertTrue(error.getMessage().contains("Namespace is not provided"), error.getMessage());
  }

  @Test
  void shouldKeepChecksConfigurationInToBuilder() {
    Taikai base = build(Taikai.builder()
            .namespace(NAMESPACE)
            .discoverChecks(checks -> checks
                .projectDirectory(this.directory)
                .exclude(TestChecks.Failing.class)),
        TestChecks.ContextCapturing.class, TestChecks.Failing.class);

    Taikai extended = build(base.toBuilder()
            .discoverChecks(checks -> checks.exclude(TestChecks.AnotherFailing.class)),
        TestChecks.ContextCapturing.class, TestChecks.Failing.class,
        TestChecks.AnotherFailing.class);

    assertEquals(List.of(TestChecks.ContextCapturing.class), classes(extended.checks()));

    extended.check();
    assertEquals(this.directory.toAbsolutePath(),
        TestChecks.ContextCapturing.lastContext().projectDirectory());
  }

  @Test
  void shouldNotChangeBuiltTaikaiWhenBuilderIsCustomizedFurther() {
    Taikai.Builder builder = Taikai.builder()
        .namespace(NAMESPACE)
        .discoverChecks();
    Taikai taikai = build(builder, TestChecks.Passing.class, TestChecks.Failing.class);

    Taikai changed = build(
        builder.discoverChecks(checks -> checks.exclude(TestChecks.Failing.class)),
        TestChecks.Passing.class, TestChecks.Failing.class);

    assertEquals(2, taikai.checks().size());
    assertEquals(1, changed.checks().size());
  }

  @Nested
  class Configurations {

    @Test
    void shouldReturnConfigurationOfCheck() {
      TestChecks.Settings settings = new TestChecks.Settings("valid");

      CheckContext context = capturedContext(checks -> checks
          .configure(TestChecks.ContextCapturing.class, settings));

      assertSame(settings, context.configuration(TestChecks.Settings.class).orElseThrow());
      assertSame(settings, context.requireConfiguration(TestChecks.Settings.class));
    }

    @Test
    void shouldReturnEmptyWithoutConfiguration() {
      CheckContext context = capturedContext(checks -> {
      });

      assertEquals(Optional.empty(), context.configuration(TestChecks.Settings.class));
      IllegalStateException exception = assertThrows(IllegalStateException.class,
          () -> context.requireConfiguration(TestChecks.Settings.class));
      assertTrue(exception.getMessage().contains("Check "
          + TestChecks.ContextCapturing.class.getName() + " is not configured"),
          exception.getMessage());
    }

    @Test
    void shouldOnlyProvideConfigurationToConfiguredCheck() {
      Taikai taikai = build(Taikai.builder()
              .namespace(NAMESPACE)
              .discoverChecks(checks -> checks
                  .configure(TestChecks.SettingsRequiring.class,
                      new TestChecks.Settings("valid"))),
          TestChecks.SettingsRequiring.class, TestChecks.ContextCapturing.class);

      assertDoesNotThrow(taikai::checkAll);
      assertEquals(Optional.empty(), TestChecks.ContextCapturing.lastContext()
          .configuration(TestChecks.Settings.class));
    }

    @Test
    void shouldProvideClassOfCheck() {
      CheckContext context = capturedContext(checks -> checks
          .context(checkContext -> new ForwardingCheckContext(checkContext) {
          }));

      assertEquals(TestChecks.ContextCapturing.class, context.check());
    }

    @Test
    void shouldNameCheckWhenRequiredConfigurationIsMissingBehindContextFunction() {
      Taikai taikai = build(Taikai.builder()
              .namespace(NAMESPACE)
              .discoverChecks(checks -> checks
                  .context(context -> new ForwardingCheckContext(context) {
                  })),
          TestChecks.SettingsRequiring.class);

      AssertionError error = assertThrows(AssertionError.class, taikai::checkAll);

      assertTrue(error.getMessage().contains("Check "
          + TestChecks.SettingsRequiring.class.getName() + " is not configured with "
          + TestChecks.Settings.class.getName()), error.getMessage());
    }

    @Test
    void shouldFindConfigurationBySupertype() {
      List<String> artifacts = new ArrayList<>(List.of("log4j:log4j"));

      CheckContext context = capturedContext(checks -> checks
          .configure(TestChecks.ContextCapturing.class, artifacts));

      assertSame(artifacts, context.requireConfiguration(List.class));
      assertSame(artifacts, context.requireConfiguration(Iterable.class));
    }

    @Test
    void shouldThrowWhenConfigurationHasOtherType() {
      CheckContext context = capturedContext(checks -> checks
          .configure(TestChecks.ContextCapturing.class, "text"));

      IllegalStateException exception = assertThrows(IllegalStateException.class,
          () -> context.configuration(TestChecks.Settings.class));
      assertTrue(exception.getMessage().contains("is configured with java.lang.String, not with "
          + TestChecks.Settings.class.getName()), exception.getMessage());
    }

    @Test
    void shouldReplaceConfigurationWhenConfiguredAgain() {
      TestChecks.Settings second = new TestChecks.Settings("second");

      CheckContext context = capturedContext(checks -> checks
          .configure(TestChecks.ContextCapturing.class, new TestChecks.Settings("first"))
          .configure(TestChecks.ContextCapturing.class, second));

      assertSame(second, context.requireConfiguration(TestChecks.Settings.class));
    }

    @Test
    void shouldRunCheckWithItsConfiguration() {
      Taikai valid = build(Taikai.builder()
              .namespace(NAMESPACE)
              .discoverChecks(checks -> checks
                  .configure(TestChecks.SettingsRequiring.class,
                      new TestChecks.Settings("valid"))),
          TestChecks.SettingsRequiring.class);
      Taikai invalid = build(Taikai.builder()
              .namespace(NAMESPACE)
              .discoverChecks(checks -> checks
                  .configure(TestChecks.SettingsRequiring.class,
                      new TestChecks.Settings("other"))),
          TestChecks.SettingsRequiring.class);

      assertDoesNotThrow(valid::checkAll);
      AssertionError error = assertThrows(AssertionError.class, invalid::checkAll);
      assertTrue(error.getMessage().contains("invalid setting other"), error.getMessage());
    }

    @Test
    void shouldReportMissingRequiredConfigurationAsViolation() {
      Taikai taikai = discover(TestChecks.SettingsRequiring.class);

      AssertionError error = assertThrows(AssertionError.class, taikai::checkAll);

      assertTrue(error.getMessage().contains("Check "
          + TestChecks.SettingsRequiring.class.getName() + " is not configured"),
          error.getMessage());
    }

    @Test
    void shouldFailBuildWhenConfiguredCheckIsNotRegistered() {
      Taikai.Builder builder = Taikai.builder()
          .namespace(NAMESPACE)
          .discoverChecks(checks -> checks
              .configure(TestChecks.Failing.class, new TestChecks.Settings("valid")));

      TaikaiException exception = assertThrows(TaikaiException.class,
          () -> build(builder, TestChecks.Passing.class));

      assertTrue(exception.getMessage().contains("is configured, but not registered"),
          exception.getMessage());
    }

    @Test
    void shouldFailBuildWhenConfiguredCheckIsExcluded() {
      Taikai.Builder builder = Taikai.builder()
          .namespace(NAMESPACE)
          .discoverChecks(checks -> checks
              .configure(TestChecks.Failing.class, new TestChecks.Settings("valid"))
              .exclude(TestChecks.Failing.class));

      TaikaiException exception = assertThrows(TaikaiException.class,
          () -> build(builder, TestChecks.Failing.class));

      assertTrue(exception.getMessage().contains("is configured, but excluded"),
          exception.getMessage());
    }

    @Test
    void shouldForwardConfigurationInForwardingCheckContext() {
      TestChecks.Settings settings = new TestChecks.Settings("valid");
      CheckContext context = capturedContext(checks -> checks
          .configure(TestChecks.ContextCapturing.class, settings));

      CheckContext forwarding = new ForwardingCheckContext(context) {
      };

      assertSame(settings, forwarding.requireConfiguration(TestChecks.Settings.class));
    }

    @Test
    void shouldKeepConfigurationsInToBuilder() {
      TestChecks.Settings settings = new TestChecks.Settings("valid");
      Taikai base = build(Taikai.builder()
              .namespace(NAMESPACE)
              .discoverChecks(checks -> checks
                  .configure(TestChecks.ContextCapturing.class, settings)),
          TestChecks.ContextCapturing.class);

      build(base.toBuilder(), TestChecks.ContextCapturing.class).check();

      assertSame(settings, TestChecks.ContextCapturing.lastContext()
          .requireConfiguration(TestChecks.Settings.class));
    }

    private CheckContext capturedContext(Consumer<ChecksConfigurer> customizer) {
      build(Taikai.builder()
              .namespace(NAMESPACE)
              .discoverChecks(customizer),
          TestChecks.ContextCapturing.class)
          .check();

      return TestChecks.ContextCapturing.lastContext();
    }
  }

  @Nested
  class ContextFunctions {

    @Test
    void shouldReplaceContextWithForwardingCheckContext() {
      ClassLoader classLoader = new ClassLoader() {
      };

      build(Taikai.builder()
              .namespace(NAMESPACE)
              .discoverChecks(checks -> checks
                  .context(context -> new ForwardingCheckContext(context) {
                    @Override
                    public ClassLoader classLoader() {
                      return classLoader;
                    }
                  })),
          TestChecks.ContextCapturing.class)
          .check();

      CheckContext context = TestChecks.ContextCapturing.lastContext();
      assertSame(classLoader, context.classLoader());
      assertEquals(NAMESPACE, context.namespace());
      assertEquals(Path.of("").toAbsolutePath(), context.projectDirectory());
      assertTrue(context.classes().contain(Taikai.class));
    }

    @Test
    void shouldApplyContextFunctionsInOrderToConfiguredContext() {
      build(Taikai.builder()
              .namespace(NAMESPACE)
              .discoverChecks(checks -> checks
                  .projectDirectory(directory)
                  .context(context -> new ForwardingCheckContext(context) {
                    @Override
                    public Path projectDirectory() {
                      return super.projectDirectory().resolve("first");
                    }
                  })
                  .context(context -> new ForwardingCheckContext(context) {
                    @Override
                    public Path projectDirectory() {
                      return super.projectDirectory().resolve("second");
                    }
                  })),
          TestChecks.ContextCapturing.class)
          .check();

      assertEquals(directory.toAbsolutePath().resolve("first").resolve("second"),
          TestChecks.ContextCapturing.lastContext().projectDirectory());
    }

    @Test
    void shouldThrowWhenContextFunctionReturnsNull() {
      Taikai.Builder builder = Taikai.builder()
          .namespace(NAMESPACE)
          .discoverChecks(checks -> checks.context(context -> null));

      assertThrows(NullPointerException.class,
          () -> build(builder, TestChecks.Passing.class));
    }

    @Test
    void shouldApplyContextFunctionOfCheckOnlyToThatCheck() {
      TestChecks.Settings settings = new TestChecks.Settings("valid");

      Taikai taikai = build(Taikai.builder()
              .namespace(NAMESPACE)
              .discoverChecks(checks -> checks
                  .context(TestChecks.SettingsRequiring.class,
                      context -> new ForwardingCheckContext(context) {
                        @Override
                        public <T> Optional<T> configuration(Class<T> type) {
                          return Optional.of(type.cast(settings));
                        }
                      })),
          TestChecks.SettingsRequiring.class, TestChecks.ContextCapturing.class);

      assertDoesNotThrow(taikai::checkAll);
      assertEquals(Optional.empty(), TestChecks.ContextCapturing.lastContext()
          .configuration(TestChecks.Settings.class));
    }

    @Test
    void shouldApplyFunctionsForAllChecksAndSingleChecksInConfiguredOrder() {
      build(Taikai.builder()
              .namespace(NAMESPACE)
              .discoverChecks(checks -> checks
                  .projectDirectory(directory)
                  .context(TestChecks.ContextCapturing.class, resolving("single"))
                  .context(resolving("all"))
                  .context(TestChecks.Passing.class, resolving("other"))
                  .context(TestChecks.ContextCapturing.class, resolving("single again"))),
          TestChecks.ContextCapturing.class, TestChecks.Passing.class)
          .check();

      assertEquals(directory.toAbsolutePath().resolve("single").resolve("all")
              .resolve("single again"),
          TestChecks.ContextCapturing.lastContext().projectDirectory());
    }

    @Test
    void shouldApplyContextFunctionOncePerCheck() {
      AtomicInteger applications = new AtomicInteger();

      Taikai taikai = build(Taikai.builder()
              .namespace(NAMESPACE)
              .discoverChecks(checks -> checks
                  .context(context -> {
                    applications.incrementAndGet();
                    return context;
                  })),
          TestChecks.Passing.class, TestChecks.ContextCapturing.class);

      assertEquals(2, applications.get());

      taikai.check();
      taikai.checkAll();
      taikai.dynamicTests().forEach(test -> assertDoesNotThrow(
          () -> test.getExecutable().execute()));

      assertEquals(2, applications.get());
    }

    @Test
    void shouldPassContextOfCheckWithConfigurationToContextFunction() {
      TestChecks.Settings settings = new TestChecks.Settings("valid");
      List<Object> configurations = new ArrayList<>();

      build(Taikai.builder()
              .namespace(NAMESPACE)
              .discoverChecks(checks -> checks
                  .configure(TestChecks.ContextCapturing.class, settings)
                  .context(context -> {
                    configurations.add(context.requireConfiguration(TestChecks.Settings.class));
                    return context;
                  })),
          TestChecks.ContextCapturing.class);

      assertEquals(List.of(settings), configurations);
    }

    @Test
    void shouldFailBuildWhenContextFunctionIsForUnregisteredCheck() {
      Taikai.Builder builder = Taikai.builder()
          .namespace(NAMESPACE)
          .discoverChecks(checks -> checks
              .context(TestChecks.Failing.class, context -> context));

      TaikaiException exception = assertThrows(TaikaiException.class,
          () -> build(builder, TestChecks.Passing.class));

      assertTrue(exception.getMessage().contains("is configured, but not registered"),
          exception.getMessage());
    }

    @Test
    void shouldFailBuildWhenContextFunctionIsForExcludedCheck() {
      Taikai.Builder builder = Taikai.builder()
          .namespace(NAMESPACE)
          .discoverChecks(checks -> checks
              .context(TestChecks.Failing.class, context -> context)
              .exclude(TestChecks.Failing.class));

      TaikaiException exception = assertThrows(TaikaiException.class,
          () -> build(builder, TestChecks.Failing.class));

      assertTrue(exception.getMessage().contains("is configured, but excluded"),
          exception.getMessage());
    }

    @Test
    void shouldThrowWhenContextFunctionOfCheckReturnsNull() {
      Taikai.Builder builder = Taikai.builder()
          .namespace(NAMESPACE)
          .discoverChecks(checks -> checks
              .context(TestChecks.Passing.class, context -> null));

      NullPointerException exception = assertThrows(NullPointerException.class,
          () -> build(builder, TestChecks.Passing.class));

      assertTrue(exception.getMessage().contains(TestChecks.Passing.class.getName()),
          exception.getMessage());
    }

    @Test
    void shouldKeepContextFunctionsOfChecksInToBuilder() {
      Taikai base = build(Taikai.builder()
              .namespace(NAMESPACE)
              .discoverChecks(checks -> checks
                  .projectDirectory(directory)
                  .context(TestChecks.ContextCapturing.class, resolving("single"))),
          TestChecks.ContextCapturing.class);

      build(base.toBuilder(), TestChecks.ContextCapturing.class).check();

      assertEquals(directory.toAbsolutePath().resolve("single"),
          TestChecks.ContextCapturing.lastContext().projectDirectory());
    }

    private UnaryOperator<CheckContext> resolving(String directoryName) {
      return context -> new ForwardingCheckContext(context) {
        @Override
        public Path projectDirectory() {
          return super.projectDirectory().resolve(directoryName);
        }
      };
    }
  }

  private Taikai discover(Class<?>... checks) {
    return build(Taikai.builder().namespace(NAMESPACE).discoverChecks(), checks);
  }

  private Taikai build(Taikai.Builder builder, Class<?>... checks) {
    return build(builder, Arrays.stream(checks).map(Class::getName).toArray(String[]::new));
  }

  /**
   * Builds with a context class loader that only finds the given checks in
   * {@code META-INF/services}, like a library that registers them on the classpath.
   */
  private Taikai build(Taikai.Builder builder, String... checkClassNames) {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

    try {
      Path registration = Files.writeString(Files.createTempFile(this.directory, "services", ""),
          String.join(System.lineSeparator(), checkClassNames));
      Thread.currentThread().setContextClassLoader(
          new RegisteredChecksClassLoader(contextClassLoader, registration.toUri().toURL()));

      return builder.build();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }

  private static List<Class<?>> classes(Iterable<TaikaiCheck> checks) {
    List<Class<?>> classes = new ArrayList<>();
    checks.forEach(check -> classes.add(check.getClass()));
    return classes;
  }

  private static final class RegisteredChecksClassLoader extends ClassLoader {

    private final URL registration;

    RegisteredChecksClassLoader(ClassLoader parent, URL registration) {
      super(parent);
      this.registration = registration;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
      return SERVICES.equals(name)
          ? Collections.enumeration(List.of(this.registration))
          : super.getResources(name);
    }
  }
}
