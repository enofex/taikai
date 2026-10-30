package com.enofex.taikai;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.exports.spi.ExportContext;
import com.enofex.taikai.exports.spi.TaikaiExport;
import com.enofex.taikai.exports.spi.TestExports;
import com.enofex.taikai.java.JavaConfigurer;
import com.tngtech.archunit.ArchConfiguration;
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
import java.util.ServiceConfigurationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TaikaiExportTest {

  private static final String NAMESPACE = "com.enofex.taikai";
  private static final String SERVICES = "META-INF/services/" + TaikaiExport.class.getName();

  @TempDir
  private Path directory;

  @BeforeEach
  void setUp() {
    ArchConfiguration.get().reset();
    TestExports.reset();
  }

  @Test
  void shouldNotDiscoverExportsByDefault() {
    Taikai taikai = build(Taikai.builder().namespace(NAMESPACE), TestExports.First.class);

    assertTrue(taikai.exports().isEmpty());
  }

  @Test
  void shouldDiscoverRegisteredExportsSortedByDescription() {
    Taikai taikai = discover(TestExports.Second.class, TestExports.First.class);

    assertEquals(List.of(TestExports.First.class, TestExports.Second.class),
        classes(taikai.exports()));
  }

  @Test
  void shouldRunExportsInOrder() {
    discover(TestExports.Second.class, TestExports.First.class).export();

    assertEquals(List.of("A first export", "A second export"), TestExports.exported());
  }

  @Test
  void shouldExcludeExportsWithoutInstantiatingThem() {
    Taikai taikai = build(Taikai.builder()
            .namespace(NAMESPACE)
            .discoverExports(exports -> exports
                .exclude(TestExports.Second.class, TestExports.NotInstantiable.class)),
        TestExports.First.class, TestExports.Second.class, TestExports.NotInstantiable.class);

    assertEquals(List.of(TestExports.First.class), classes(taikai.exports()));
  }

  @Test
  void shouldAddUpSettingsOfRepeatedDiscoverExportsCalls() {
    Taikai taikai = build(Taikai.builder()
            .namespace(NAMESPACE)
            .discoverExports(exports -> exports.exclude(TestExports.Second.class))
            .discoverExports(exports -> exports.exclude(TestExports.Throwing.class))
            .discoverExports(),
        TestExports.First.class, TestExports.Second.class, TestExports.Throwing.class);

    assertEquals(List.of(TestExports.First.class), classes(taikai.exports()));
  }

  @Test
  void shouldFailBuildWhenExcludedExportIsNotRegistered() {
    Taikai.Builder builder = Taikai.builder()
        .namespace(NAMESPACE)
        .discoverExports(exports -> exports.exclude(TestExports.Second.class));

    TaikaiException exception = assertThrows(TaikaiException.class,
        () -> build(builder, TestExports.First.class));

    assertTrue(exception.getMessage().contains("Export " + TestExports.Second.class.getName()
        + " is excluded, but not registered"), exception.getMessage());
  }

  @Test
  void shouldFailBuildWhenConfiguredExportIsNotRegistered() {
    Taikai.Builder builder = Taikai.builder()
        .namespace(NAMESPACE)
        .discoverExports(exports -> exports
            .configure(TestExports.SettingsRequiring.class, new TestExports.Settings("value")));

    TaikaiException exception = assertThrows(TaikaiException.class,
        () -> build(builder, TestExports.First.class));

    assertTrue(exception.getMessage().contains("Export "
        + TestExports.SettingsRequiring.class.getName() + " is configured, but not registered"),
        exception.getMessage());
  }

  @Test
  void shouldFailBuildWhenConfiguredExportIsExcluded() {
    Taikai.Builder builder = Taikai.builder()
        .namespace(NAMESPACE)
        .discoverExports(exports -> exports
            .exclude(TestExports.SettingsRequiring.class)
            .configure(TestExports.SettingsRequiring.class, new TestExports.Settings("value")));

    TaikaiException exception = assertThrows(TaikaiException.class,
        () -> build(builder, TestExports.SettingsRequiring.class));

    assertTrue(exception.getMessage().contains("Export "
        + TestExports.SettingsRequiring.class.getName() + " is configured, but excluded"),
        exception.getMessage());
  }

  @Test
  void shouldThrowWhenRegisteredExportCannotBeLoaded() {
    Taikai.Builder builder = Taikai.builder()
        .namespace(NAMESPACE)
        .discoverExports();

    TaikaiException exception = assertThrows(TaikaiException.class,
        () -> build(builder, "com.example.MissingExport"));

    assertInstanceOf(ServiceConfigurationError.class, exception.getCause());
    assertTrue(exception.getMessage().contains("Could not discover the exports registered in "
        + SERVICES), exception.getMessage());
  }

  @Test
  void shouldThrowWhenRegisteredExportCannotBeInstantiated() {
    Taikai.Builder builder = Taikai.builder()
        .namespace(NAMESPACE)
        .discoverExports();

    assertThrows(TaikaiException.class,
        () -> build(builder, TestExports.NotInstantiable.class.getName()));
  }

  @Test
  void shouldNameFailingExport() {
    Taikai taikai = discover(TestExports.Throwing.class, TestExports.Second.class);

    TaikaiException exception = assertThrows(TaikaiException.class, taikai::export);

    assertEquals("Export 'A throwing export' failed", exception.getMessage());
    assertInstanceOf(IllegalStateException.class, exception.getCause());
    assertEquals(List.of("A second export"), TestExports.exported());
  }

  @Test
  void shouldNameExportFailingWithAssertionError() {
    Taikai taikai = discover(TestExports.Asserting.class);

    TaikaiException exception = assertThrows(TaikaiException.class, taikai::export);

    assertEquals("Export 'An asserting export' failed", exception.getMessage());
    assertInstanceOf(AssertionError.class, exception.getCause());
  }

  @Test
  void shouldGiveExportsTheirContext() {
    Taikai taikai = build(Taikai.builder()
            .namespace(NAMESPACE)
            .java(JavaConfigurer::noUsageOfSystemOutOrErr)
            .discoverExports(),
        TestExports.ContextCapturing.class);

    taikai.export();
    ExportContext context = TestExports.ContextCapturing.lastContext();

    assertEquals(TestExports.ContextCapturing.class, context.export());
    assertEquals(Path.of("").toAbsolutePath(), context.projectDirectory());
    assertEquals(NAMESPACE, context.namespace());
    assertEquals(taikai.rules(), context.rules());
    assertSame(Thread.currentThread().getContextClassLoader(), context.classLoader());
    assertTrue(context.classes().contain(Taikai.class));
    assertFalse(context.classes().contain(TaikaiExportTest.class));
  }

  @Test
  void shouldGiveExportsTheDirectlySuppliedClasses() {
    build(Taikai.builder().classes(Taikai.class).discoverExports(),
        TestExports.ContextCapturing.class).export();
    ExportContext context = TestExports.ContextCapturing.lastContext();

    assertNull(context.namespace());
    assertEquals(1, context.classes().size());
  }

  @Test
  void shouldGiveExportsTheirConfiguration() {
    build(Taikai.builder()
            .namespace(NAMESPACE)
            .discoverExports(exports -> exports
                .configure(TestExports.SettingsRequiring.class, new TestExports.Settings("value"))),
        TestExports.SettingsRequiring.class).export();

    assertEquals(List.of("value"), TestExports.exported());
  }

  @Test
  void shouldFailExportWithoutRequiredConfiguration() {
    Taikai taikai = discover(TestExports.SettingsRequiring.class);

    TaikaiException exception = assertThrows(TaikaiException.class, taikai::export);

    assertEquals("Export " + TestExports.SettingsRequiring.class.getName()
            + " is not configured with " + TestExports.Settings.class.getName()
            + ", add it with discoverExports(exports -> exports.configure("
            + "SettingsRequiring.class, ...))",
        exception.getCause().getMessage());
  }

  @Test
  void shouldFailWhenConfigurationHasAnotherType() {
    Taikai taikai = build(Taikai.builder()
            .namespace(NAMESPACE)
            .discoverExports(exports -> exports
                .configure(TestExports.SettingsRequiring.class, "value")),
        TestExports.SettingsRequiring.class);

    TaikaiException exception = assertThrows(TaikaiException.class, taikai::export);

    assertEquals("Export " + TestExports.SettingsRequiring.class.getName()
            + " is configured with java.lang.String, not with "
            + TestExports.Settings.class.getName(),
        exception.getCause().getMessage());
  }

  @Test
  void shouldNotRunRulesOrChecksWhenExporting() {
    Taikai taikai = build(Taikai.builder()
            .classes(Printer.class)
            .java(JavaConfigurer::noUsageOfSystemOutOrErr)
            .discoverExports(),
        TestExports.First.class);

    assertDoesNotThrow(taikai::export);
    assertEquals(List.of("A first export"), TestExports.exported());
  }

  @Test
  void shouldNotRunExportsWhenChecking() {
    Taikai taikai = discover(TestExports.First.class);

    taikai.checkAll();
    taikai.check();
    taikai.dynamicTests().forEach(test -> assertDoesNotThrow(() -> test.getExecutable().execute()));

    assertEquals(List.of(), TestExports.exported());
  }

  @Test
  void shouldKeepExportsInToBuilder() {
    Taikai taikai = build(Taikai.builder()
            .namespace(NAMESPACE)
            .discoverExports(exports -> exports.exclude(TestExports.Second.class)),
        TestExports.First.class, TestExports.Second.class);

    assertEquals(List.of(TestExports.First.class),
        classes(build(taikai.toBuilder(), TestExports.First.class, TestExports.Second.class)
            .exports()));
  }

  private Taikai discover(Class<?>... exports) {
    return build(Taikai.builder().namespace(NAMESPACE).discoverExports(), exports);
  }

  private Taikai build(Taikai.Builder builder, Class<?>... exports) {
    return build(builder, Arrays.stream(exports).map(Class::getName).toArray(String[]::new));
  }

  /**
   * Builds with a context class loader that only finds the given exports in
   * {@code META-INF/services}, like a library that registers them on the classpath.
   */
  private Taikai build(Taikai.Builder builder, String... exportClassNames) {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

    try {
      Path registration = Files.writeString(Files.createTempFile(this.directory, "services", ""),
          String.join(System.lineSeparator(), exportClassNames));
      Thread.currentThread().setContextClassLoader(
          new RegisteredExportsClassLoader(contextClassLoader, registration.toUri().toURL()));

      return builder.build();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }

  private static List<Class<?>> classes(Iterable<TaikaiExport> exports) {
    List<Class<?>> classes = new ArrayList<>();
    exports.forEach(export -> classes.add(export.getClass()));
    return classes;
  }

  static final class Printer {

    void print() {
      System.out.println("printed");
    }
  }

  private static final class RegisteredExportsClassLoader extends ClassLoader {

    private final URL registration;

    RegisteredExportsClassLoader(ClassLoader parent, URL registration) {
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
