package com.enofex.taikai.exports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.core.TaikaiException;
import com.enofex.taikai.exports.ArchitectureMetrics.ClassMetric;
import com.enofex.taikai.exports.ArchitectureMetrics.CohesionMetric;
import com.enofex.taikai.exports.ArchitectureMetrics.PackageMetric;
import com.enofex.taikai.exports.spi.ExportContext;
import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * The fixtures are described in their {@code package-info}.
 */
class MetricsMarkdownExportTest {

  private static final String FIXTURES = "com.enofex.taikai.exports.fixtures";
  private static final String ENTRY_POINTS = "com.enofex.taikai.exports.entrypoints";
  private static final String HUBS = "com.enofex.taikai.exports.hubs";
  private static final List<String> CHARTS = List.of("METRICS-main-sequence.svg",
      "METRICS-package-map.svg", "METRICS-dependencies.svg", "METRICS-dependency-matrix.svg",
      "METRICS-libraries.svg", "METRICS-coupling.svg");

  /**
   * The zone of every package of the fixtures.
   */
  private static final Map<String, String> PACKAGE_ZONES = Map.of(
      "api", "balanced",
      "app", "balanced",
      "billing", "balanced",
      "domain", "pain",
      "payment", "balanced",
      "spi", "uselessness");

  @TempDir
  private Path directory;

  @Test
  void shouldWriteMetrics() throws IOException {
    copySources();
    Path file = exportFixtures();

    assertEquals("""
            # Architecture Metrics

            The metrics of the configured classes, measured by Taikai. Every package counts on its \
            own, without its subpackages. Packages and coupling count top-level classes, each \
            together with the classes nested in it, while inheritance and cohesion count every \
            class on its own. Only dependencies between the measured classes count, except for the \
            libraries the packages use, and never those on the JDK. Packages and classes are named \
            relative to `com.enofex.taikai.exports.fixtures`. The lists show at most 20 classes \
            each.

            **Contents:** [Summary](#summary) \u00b7 [Main Sequence](#main-sequence) \u00b7 \
            [Package Map](#package-map) \u00b7 [Packages](#packages) \u00b7 [Package \
            Dependencies](#package-dependencies) \u00b7 [Libraries](#libraries) \u00b7 \
            [Inheritance](#inheritance) \u00b7 [Coupling](#coupling) \u00b7 [Cohesion](#cohesion)

            ## Summary

            | Metric | Value |
            | --- | ---: |
            | Packages | 6 |
            | Classes | 9 |
            | Abstract classes and interfaces | 3 |
            | Average distance from the main sequence | 0.58 |
            | Packages in the zone of pain | 1 |
            | Packages in the zone of uselessness | 1 |
            | Package cycles | 1 |
            | Dependencies on less stable packages | 1 |
            | Libraries | 2 |
            | Maximum inheritance depth | 1 |

            ## Main Sequence

            ![Abstractness and instability of the packages](METRICS-main-sequence.svg)

            Each dot is a package, sized by its number of classes. The abstractness A is the share \
            of abstract classes and interfaces. The instability I = Ce / (Ca + Ce) is the share of \
            outgoing couplings: Ca counts the classes in other packages that depend on the \
            package, Ce the classes of the package that depend on other packages. A dot without \
            room for its name has none: the packages are all listed below.

            | Value | Meaning |
            | --- | --- |
            | A = 0 | Only concrete classes |
            | A = 1 | Only abstract classes and interfaces |
            | I = 0 | Stable: other packages depend on it and it depends on none, so a change \
            affects many classes |
            | I = 1 | Unstable: it depends on other packages and none depends on it, so it is easy \
            to change |
            | D = 0 | On the main sequence, the best |
            | D = 1 | In a corner, as far from the main sequence as possible, the worst |

            A stable package should be abstract, so that it can be extended without being changed, \
            and an unstable package concrete. Packages on the main sequence A + I = 1 balance \
            both, and the distance D = |A + I - 1| shows how far a package is from it. With a \
            distance above 0.50, a concrete and stable package is in the zone of pain, hard to \
            change as many classes depend on it, and an abstract and unstable package in the zone \
            of uselessness, with abstractions that barely anything uses. Packages without \
            couplings to other packages have no instability and are not shown.

            ## Package Map

            ![Map of the packages](METRICS-package-map.svg)

            Each tile is a package: its area is its number of classes and its color its zone, the \
            stronger the farther the package is from the main sequence.

            ## Packages

            | Package | Classes | Abstract | Ca | Ce | I | A | D | Visibility | Libraries | Zone |
            | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
            | [`api`](src/com/enofex/taikai/exports/fixtures/api) | 1 | 1 | 1 | 1 | 0.50 | 1.00 | \
            0.50 | 1.00 | 0 |  |
            | [`app`](src/com/enofex/taikai/exports/fixtures/app) | 3 | 1 | 1 | 2 | 0.67 | 0.33 | \
            0.00 | 0.67 | 1 |  |
            | [`billing`](src/com/enofex/taikai/exports/fixtures/billing) | 1 | 0 | 1 | 1 | 0.50 | \
            0.00 | 0.50 | 1.00 | 1 |  |
            | [`domain`](src/com/enofex/taikai/exports/fixtures/domain) | 2 | 0 | 4 | 0 | 0.00 | \
            0.00 | 1.00 | 0.50 | 0 | pain |
            | [`payment`](src/com/enofex/taikai/exports/fixtures/payment) | 1 | 0 | 1 | 1 | 0.50 | \
            0.00 | 0.50 | 1.00 | 0 |  |
            | [`spi`](src/com/enofex/taikai/exports/fixtures/spi) | 1 | 1 | 0 | 1 | 1.00 | 1.00 | \
            1.00 | 1.00 | 0 | uselessness |

            Visibility is the relative visibility of Herbert Dowalil, the share of public classes, \
            and Libraries the number of libraries that the package uses.

            ## Package Dependencies

            ![Dependencies between the packages](METRICS-dependencies.svg)

            A package is drawn above the packages it depends on, so that the packages that many \
            depend on are at the bottom, and is marked in the color of its zone. An arrow points \
            from a package to a package it depends on, and the thicker it is, the more classes of \
            the one depend on classes of the other. Red arrows point upwards and close a cycle: \
            without them, the packages would have no cycles. Orange arrows point to a less stable \
            package.

            ### Dependency Matrix

            ![Dependency structure matrix of the packages](METRICS-dependency-matrix.svg)

            A row counts the pairs of classes with which its package depends on the package of \
            each column, numbered as the rows. The packages are ordered from the bottom of the \
            chart up, so that a package depends on the packages above it: the red cells above the \
            diagonal close a cycle, as the red arrows do, a red frame surrounds the packages of a \
            cycle, and the orange cells use a less stable package, as the orange arrows do.

            ### Package Cycles

            Under each cycle are the dependencies that close it, the red arrows of the chart, each \
            with up to 20 of the pairs of classes behind it. Without them, the packages would have \
            no cycles, so they are the dependencies to remove or invert.

            - [`billing`](src/com/enofex/taikai/exports/fixtures/billing), \
            [`payment`](src/com/enofex/taikai/exports/fixtures/payment)
              - [`payment`](src/com/enofex/taikai/exports/fixtures/payment) \u2192 \
            [`billing`](src/com/enofex/taikai/exports/fixtures/billing): 1 dependency
                - [`payment.Payment`](src/com/enofex/taikai/exports/fixtures/payment/Payment.java) \
            \u2192 [`billing.Invoice`](src/com/enofex/taikai/exports/fixtures/billing/Invoice.java)

            ### Dependencies on Less Stable Packages

            A package should only depend on packages that are at least as stable as itself, as the \
            stable dependencies principle of Robert C. Martin demands: a package that many classes \
            depend on is hard to change, so it should not depend on a package that changes more \
            easily. These dependencies point to a package with a higher instability I, the largest \
            increase first. They are orange in the chart and the matrix, unless they close a cycle.

            | Package | Depends on | Instability | Dependencies |
            | --- | --- | --- | ---: |
            | [`payment`](src/com/enofex/taikai/exports/fixtures/payment) | \
            [`app`](src/com/enofex/taikai/exports/fixtures/app) | 0.50 \u2192 0.67 | 1 |

            ## Libraries

            ![Libraries used by the packages](METRICS-libraries.svg)

            A row is a package, numbered as in the dependency matrix, and a column a library, \
            named after the first two segments of the packages of its classes, such as \
            `org.springframework` or `jakarta.persistence`. A cell counts the classes of the \
            package that use the library. The JDK does not count, nor do the standard libraries of \
            Kotlin, Groovy and Scala for the classes written in them, nor the classes of the \
            project: those in the measured packages and in the package of at least two segments \
            that they share.

            | Library | Packages | Classes |
            | --- | ---: | ---: |
            | `jakarta.validation` | 1 | 1 |
            | `org.springframework` | 1 | 1 |

            ### Packages Without Libraries

            [`api`](src/com/enofex/taikai/exports/fixtures/api), \
            [`domain`](src/com/enofex/taikai/exports/fixtures/domain), \
            [`payment`](src/com/enofex/taikai/exports/fixtures/payment), \
            [`spi`](src/com/enofex/taikai/exports/fixtures/spi)

            ## Inheritance

            The inheritance depth of a class counts its superclasses, its subclasses only the \
            direct ones. Interfaces and anonymous classes do not count.

            ### Deepest Classes

            | Class | Depth |
            | --- | ---: |
            | [`app.OrderService`](src/com/enofex/taikai/exports/fixtures/app/OrderService.java) | \
            1 |

            ### Most Extended Classes

            | Class | Subclasses |
            | --- | ---: |
            | [`app.BaseService`](src/com/enofex/taikai/exports/fixtures/app/BaseService.java) | 1 |

            ## Coupling

            The fan-in of a class counts the classes that depend on it, its fan-out the classes it \
            depends on.

            ![Fan-in and fan-out of the classes](METRICS-coupling.svg)

            Each cell counts the classes with a fan-in and a fan-out from its ticks up to just \
            below the next ones, such as 2 to 4, on logarithmic scales, and the darker it is, the \
            more classes it has. A class on the right is used by many classes, so it should be \
            stable, and a class at the top depends on many, as a class that puts others together \
            does. A class in the upper right, a hub, does both: a change of a class it depends on \
            can reach the many classes that depend on it. The dots are the hubs listed below, and \
            the dashed lines connect the points with the same fan-in \u00d7 fan-out. A hub without \
            room for its name has none.

            ### Most Used Classes

            | Class | Fan-in |
            | --- | ---: |
            | [`domain.Order`](src/com/enofex/taikai/exports/fixtures/domain/Order.java) | 4 |
            | [`api.OrderPort`](src/com/enofex/taikai/exports/fixtures/api/OrderPort.java) | 1 |
            | [`app.BaseService`](src/com/enofex/taikai/exports/fixtures/app/BaseService.java) | 1 |
            | [`app.OrderService`](src/com/enofex/taikai/exports/fixtures/app/OrderService.java) | \
            1 |
            | [`app.Report`](src/com/enofex/taikai/exports/fixtures/app/Report.java) | 1 |
            | [`billing.Invoice`](src/com/enofex/taikai/exports/fixtures/billing/Invoice.java) | 1 |
            | [`domain.LineItem`](src/com/enofex/taikai/exports/fixtures/domain/LineItem.java) | 1 |
            | [`payment.Payment`](src/com/enofex/taikai/exports/fixtures/payment/Payment.java) | 1 |

            ### Classes with the Most Dependencies

            | Class | Fan-out |
            | --- | ---: |
            | [`app.OrderService`](src/com/enofex/taikai/exports/fixtures/app/OrderService.java) | \
            4 |
            | [`payment.Payment`](src/com/enofex/taikai/exports/fixtures/payment/Payment.java) | 2 |
            | [`api.OrderPort`](src/com/enofex/taikai/exports/fixtures/api/OrderPort.java) | 1 |
            | [`app.BaseService`](src/com/enofex/taikai/exports/fixtures/app/BaseService.java) | 1 |
            | [`billing.Invoice`](src/com/enofex/taikai/exports/fixtures/billing/Invoice.java) | 1 |
            | [`domain.Order`](src/com/enofex/taikai/exports/fixtures/domain/Order.java) | 1 |
            | [`spi.Plugin`](src/com/enofex/taikai/exports/fixtures/spi/Plugin.java) | 1 |

            ### Hubs

            The classes that at least 2 classes depend on and that depend on at least 2 classes, \
            the highest fan-in \u00d7 fan-out first, which grows with the information that flows \
            through a class, after Sallie Henry and Dennis Kafura.

            None.

            ## Cohesion

            The LCOM4 of a class counts the groups of its methods that have nothing in common: \
            methods belong together if they use the same field or method of their object, also an \
            inherited one, or call each other. Lower is better: 1 is best, as all methods work \
            together. A higher value means that the class does as many unrelated things and can be \
            split into as many classes. Constructors, static and abstract methods, getters and \
            setters do not count, nor do methods without a connection to another member of their \
            class. Interfaces, records and anonymous classes are not measured.

            ### Least Cohesive Classes

            | Class | LCOM4 | Methods |
            | --- | ---: | ---: |
            | [`app.Report`](src/com/enofex/taikai/exports/fixtures/app/Report.java) | 2 | 3 |
            """,
        Files.readString(file, StandardCharsets.UTF_8));
  }

  @Test
  void shouldListEntryPoints() throws IOException {
    String markdown = exportEntryPoints(ENTRY_POINTS, EntryPoints.springMvc(),
        EntryPoints.annotatedWith(Scheduled.class));

    assertTrue(markdown.contains("| Maximum inheritance depth | 1 |\n| Entry points | 13 |\n"),
        markdown);
    assertTrue(markdown.contains("""
            | Package | Classes | Abstract | Ca | Ce | I | A | D | Visibility | Libraries | Entry \
            points | Zone |
            | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
            | `api` | 2 | 2 | 4 | 0 | 0.00 | 1.00 | 0.00 | 1.00 | 1 |  |  |
            | `app` | 1 | 0 | 1 | 0 | 0.00 | 0.00 | 1.00 | 1.00 | 1 | 1 | pain |
            | `web` | 11 | 5 | 0 | 4 | 1.00 | 0.45 | 0.45 | 0.00 | 1 | 12 |  |

            Visibility is the relative visibility of Herbert Dowalil, the share of public classes, \
            and Libraries the number of libraries that the package uses. Entry points counts the \
            methods of the package that a framework calls, see [Entry Points](#entry-points).
            """), markdown);
    assertTrue(markdown.contains("""
            ### Entry Points

            Entry points are the methods that a framework calls rather than the measured \
            classes, such as the handler methods of Spring MVC controllers, and a class counts \
            those it inherits as well. They do not count as couplings, as their callers depend on \
            a contract, such as the path and the parameters of a request, and not on the class, \
            which can be renamed, split or moved without changing them. So a class with entry \
            points is used even with a fan-in of 0, and the classes with the lowest fan-in come \
            first, then those with the most entry points. Kinds counts the entry points by their \
            kind, such as their HTTP method, and an entry point with several kinds, such as GET \
            and POST, counts for each of them.

            | Class | Entry points | Kinds | Fan-in |
            | --- | ---: | --- | ---: |
            | `web.ItemController` | 7 | ANY 1, DELETE 1, GET 4, POST 2 | 0 |
            | `web.TagController` | 3 | GET 1, POST 2, PUT 1 | 0 |
            | `web.NoteController` | 1 | PUT 1 | 0 |
            | `web.ReportController` | 1 | GET 1 | 0 |
            | `app.ItemService` | 1 | Scheduled 1 | 1 |

            ## Cohesion
            """), markdown);
  }

  @Test
  void shouldListAtMost20ClassesWithEntryPoints() throws IOException {
    String markdown = exportEntryPoints("com.enofex.taikai.exports",
        (javaClass, method) -> Set.of("Call"));
    String entryPoints = markdown.substring(markdown.indexOf("### Entry Points"),
        markdown.indexOf("## Cohesion"));

    assertEquals(MetricsMarkdownExport.LISTED_CLASSES,
        entryPoints.lines().filter(line -> line.startsWith("| `")).count(), entryPoints);
  }

  @Test
  void shouldListNoEntryPointsIfNoneAreFound() throws IOException {
    String markdown = exportEntryPoints(FIXTURES, EntryPoints.springMvc());

    assertTrue(markdown.contains("| Entry points | 0 |\n"), markdown);
    assertTrue(markdown.contains(
        "| `domain` | 2 | 0 | 4 | 0 | 0.00 | 0.00 | 1.00 | 0.50 | 0 |  | pain |\n"), markdown);
    assertTrue(markdown.contains("counts for each of them.\n\nNone.\n\n## Cohesion"),
        markdown);
  }

  @Test
  void shouldNotListEntryPointsUnlessConfigured() throws IOException {
    String markdown = Files.readString(exportFixtures(ENTRY_POINTS), StandardCharsets.UTF_8);

    assertFalse(markdown.contains("Entry"), markdown);
    assertTrue(markdown.contains("| Visibility | Libraries | Zone |\n"), markdown);
  }

  @Test
  void shouldEscapeKindsOfEntryPoints() throws IOException {
    String markdown = exportEntryPoints(ENTRY_POINTS, (javaClass, method) ->
        method.getName().equals("clear") ? Set.of("rate|cron\nevery minute") : Set.of());

    assertTrue(markdown.contains("| `app.ItemService` | 1 | rate\\|cron every minute 1 | 1 |\n"),
        markdown);
  }

  @Test
  void shouldNameMethodWithNullKinds() {
    TaikaiException exception = assertThrows(TaikaiException.class,
        () -> exportEntryPoints(ENTRY_POINTS, (javaClass, method) ->
            method.getName().equals("clear") ? Collections.singleton(null) : Set.of()));

    assertInstanceOf(NullPointerException.class, exception.getCause());
    assertEquals("The kinds of the entry points of "
            + "com.enofex.taikai.exports.entrypoints.app.ItemService.clear() must not be null",
        exception.getCause().getMessage());
  }

  @Test
  void shouldKeepKindsOfEntryPointsUnmodifiable() {
    Map<String, Integer> kinds = ArchitectureMetrics.of(classes(ENTRY_POINTS),
        List.of(EntryPoints.springMvc())).entryPoints().get(0).kinds();

    assertThrows(UnsupportedOperationException.class, () -> kinds.put("GET", 0));
  }

  @Test
  void shouldRejectAnnotationsThatNoClassFileHas() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> EntryPoints.annotatedWith(SourceOnly.class));

    assertTrue(exception.getMessage().contains("has the retention SOURCE"),
        exception.getMessage());
  }

  @Test
  void shouldListNoEntryPointsUnlessGivenAndKeepThem() {
    EntryPoints entryPoints = EntryPoints.springMvc();
    MetricsMarkdownExport.Settings settings = MetricsMarkdownExport.file("METRICS.md");

    assertEquals(List.of(), settings.entryPoints());
    assertEquals(List.of(), new MetricsMarkdownExport.Settings("METRICS.md", "images",
        List.of("src")).entryPoints());
    assertEquals(List.of(entryPoints), settings.withEntryPoints(entryPoints)
        .withImageDirectory("images")
        .withSourceDirectories("src")
        .entryPoints());
    assertThrows(NullPointerException.class, () -> settings.withEntryPoints(entryPoints, null));
  }

  @Test
  void shouldListHubs() throws IOException {
    String markdown = Files.readString(exportFixtures(HUBS), StandardCharsets.UTF_8);

    assertTrue(markdown.contains("""
        | Class | Fan-in | Fan-out | Fan-in \u00d7 fan-out |
        | --- | ---: | ---: | ---: |
        | `Hub` | 3 | 2 | 6 |
        | `Router` | 2 | 2 | 4 |

        ## Cohesion
        """), markdown);
  }

  @Test
  void shouldCountClassesInCellsAndDrawHubsOnTop() {
    exportFixtures(HUBS);

    Document svg = svg(this.directory.resolve("METRICS-coupling.svg"));

    assertEquals(List.of("Fan-in 0, fan-out 1: 1 class\nThird",
            "Fan-in 0, fan-out 2\u20134: 2 classes\nFirst\nSecond",
            "Fan-in 2\u20134, fan-out 0: 2 classes\nLeft\nRight",
            "Fan-in 2\u20134, fan-out 2\u20134: 2 classes\nHub\nRouter"),
        titles(svg, "g", "density-cell"));
    assertEquals(List.of("1", "2", "2", "2"), counts(svg));
    assertEquals(List.of("Hub", "Router"),
        elements(svg, "g", "hub").stream().map(MetricsMarkdownExportTest::label).toList());
    assertEquals(List.of("Hub: fan-in 3, fan-out 2", "Router: fan-in 2, fan-out 2"),
        titles(svg, "g", "hub"));
    assertEquals(1, elements(svg, "polyline", "isoline").size());
  }

  @Test
  void shouldDrawPackagesOnTheMainSequence() {
    exportFixtures();

    assertEquals(Map.of(
            "api", "balanced",
            "app", "balanced",
            "billing, payment", "balanced",
            "domain", "pain",
            "spi", "uselessness"),
        zones(this.directory.resolve("METRICS-main-sequence.svg"), "dot"));
  }

  @Test
  void shouldDrawPackageDependencies() {
    exportFixtures();

    Path chart = this.directory.resolve("METRICS-dependencies.svg");
    Document svg = svg(chart);

    assertEquals(PACKAGE_ZONES, zones(chart, "package"));
    assertEquals(List.of("payment \u2192 billing: 1 dependency, closes a cycle"),
        titles(svg, "path", "cycle"));
    assertEquals(5, elements(svg, "path", "edge").size());
    assertEquals(List.of("payment \u2192 app: 1 dependency, on a less stable package"),
        titles(svg, "path", "unstable"));
  }

  @Test
  void shouldDrawDependencyMatrix() {
    exportFixtures();

    Document svg = svg(this.directory.resolve("METRICS-dependency-matrix.svg"));

    assertEquals(List.of("api \u2192 domain: 1 dependency", "app \u2192 api: 1 dependency",
            "app \u2192 domain: 2 dependencies", "billing \u2192 payment: 1 dependency",
            "spi \u2192 domain: 1 dependency"),
        titles(svg, "g", "dependency-cell"));
    assertEquals(List.of("payment \u2192 billing: 1 dependency, closes a cycle"),
        titles(svg, "g", "closing-cell"));
    assertEquals(List.of("payment \u2192 app: 1 dependency, on a less stable package"),
        titles(svg, "g", "unstable-cell"));
    assertEquals(1, elements(svg, "rect", "group").stream()
        .filter(rect -> rect.getAttribute("rx").equals("4"))
        .count());
  }

  @Test
  void shouldDrawLibraries() {
    exportFixtures();

    assertEquals(List.of("app uses org.springframework: 1 class\napp.OrderService",
            "billing uses jakarta.validation: 1 class\nbilling.Invoice"),
        titles(svg(this.directory.resolve("METRICS-libraries.svg")), "g", "library-cell"));
  }

  @Test
  void shouldNotCountJdkAsLibraryWithoutResolvingClasses() throws IOException {
    ArchConfiguration.get().setResolveMissingDependenciesFromClassPath(false);

    try {
      exportFixtures();
    } finally {
      ArchConfiguration.get().reset();
    }

    String markdown = Files.readString(this.directory.resolve("METRICS.md"),
        StandardCharsets.UTF_8);

    assertTrue(markdown.contains("| Libraries | 2 |\n"), markdown);
    assertTrue(markdown.contains(
        "### Packages Without Libraries\n\n`api`, `domain`, `payment`, `spi`\n"), markdown);
  }

  @Test
  void shouldNameNamespace() throws IOException {
    Path file = this.directory.resolve("METRICS.md");

    ExportChecks.export(Taikai.builder().namespace("com.enofex.taikai.exports"),
        MetricsMarkdownExport.class, settings(file));

    assertTrue(Files.readString(file, StandardCharsets.UTF_8).contains(
        "The metrics of `com.enofex.taikai.exports`, measured by Taikai."));
  }

  @Test
  void shouldNotDrawPackageWithoutCouplingsOrLibraries() throws Exception {
    String markdown = Files.readString(exportFixtures(FIXTURES + ".domain"),
        StandardCharsets.UTF_8);

    assertTrue(markdown.contains("| Average distance from the main sequence | - |"), markdown);
    assertTrue(markdown.contains(
        "| `com.enofex.taikai.exports.fixtures.domain` | 2 | 0 | 0 | 0 | - | 0.00 | - | 0.50 "
            + "| 0 |  |"),
        markdown);
    assertTrue(markdown.contains("### Package Cycles\n\nNone.\n"), markdown);
    assertTrue(markdown.contains("| `Order` | 1 |"), markdown);
    assertEquals(Map.of(), zones(this.directory.resolve("METRICS-main-sequence.svg"), "dot"));
    assertTrue(Files.readString(this.directory.resolve("METRICS-libraries.svg"),
        StandardCharsets.UTF_8).contains(">No libraries</text>"));
  }

  @Test
  void shouldWriteChartsToConfiguredDirectoryAndLinkThem() throws IOException {
    Path file = this.directory.resolve("docs/METRICS.md");
    Path images = this.directory.resolve("docs/c# 100%");

    export(classes(FIXTURES), settings(file).withImageDirectory(images.toString()));

    String markdown = Files.readString(file, StandardCharsets.UTF_8);

    for (String chart : CHARTS) {
      assertTrue(Files.isRegularFile(images.resolve(chart)), chart);
      assertFalse(Files.exists(file.resolveSibling(chart)), chart);
      assertTrue(markdown.contains("](<c%23 100%25/" + chart + ">)"), markdown);
    }
  }

  @Test
  void shouldReplaceFilesAndCreateMissingDirectories() throws IOException {
    Path file = this.directory.resolve("docs/metrics/METRICS.md");
    List<Path> charts = CHARTS.stream().map(file::resolveSibling).toList();
    Files.createDirectories(file.getParent());
    Files.writeString(file, "old content that is longer than the new one".repeat(100));

    for (Path chart : charts) {
      Files.writeString(chart, "old content that is longer than the new one".repeat(100));
    }

    export(classes(FIXTURES), settings(file));

    String markdown = Files.readString(file, StandardCharsets.UTF_8);

    assertTrue(markdown.startsWith("# Architecture Metrics"));
    assertFalse(markdown.contains("old content"));

    for (Path chart : charts) {
      String svg = Files.readString(chart, StandardCharsets.UTF_8);

      assertTrue(svg.startsWith("<svg "));
      assertFalse(svg.contains("old content"));
    }
  }

  @Test
  void shouldDrawPackageMap() {
    exportFixtures();

    Path chart = this.directory.resolve("METRICS-package-map.svg");
    Map<String, Double> areaByPackage = new LinkedHashMap<>();

    for (Element tile : groups(chart, "tile")) {
      Element rect = (Element) tile.getElementsByTagName("rect").item(0);
      areaByPackage.put(label(tile), (Double.parseDouble(rect.getAttribute("width")) + 3)
          * (Double.parseDouble(rect.getAttribute("height")) + 3));
    }

    assertEquals(PACKAGE_ZONES, zones(chart, "tile"));
    assertEquals(3, areaByPackage.get("app") / areaByPackage.get("api"), 0.01);
    assertEquals(2, areaByPackage.get("domain") / areaByPackage.get("spi"), 0.01);
  }

  @Test
  void shouldNotLinkNamesWithoutSources() throws IOException {
    String markdown = Files.readString(exportFixtures(), StandardCharsets.UTF_8);

    assertTrue(markdown.contains("| `api` | 1 |"), markdown);
    assertTrue(markdown.contains("| `domain.Order` | 4 |"), markdown);
    assertFalse(markdown.contains("](src/"), markdown);
  }

  @Test
  void shouldLinkToSourcesInDefaultDirectories() throws IOException {
    Path file = this.directory.resolve("METRICS.md");
    Path sources = Path.of("src/test/java/com/enofex/taikai/exports/fixtures").toAbsolutePath();

    // on Windows, the temporary directory may be on another drive, which nothing can link to
    assumeTrue(file.getRoot().equals(sources.getRoot()));

    export(classes(FIXTURES), MetricsMarkdownExport.file(file.toString()));

    String markdown = Files.readString(file, StandardCharsets.UTF_8);

    assertTrue(markdown.contains("[`domain.Order`](%s)".formatted(
        link(file, sources.resolve("domain/Order.java")))), markdown);
    assertTrue(markdown.contains("[`api`](%s)".formatted(link(file, sources.resolve("api")))),
        markdown);
  }

  @Test
  void shouldLinkToDirectoryOfFile() throws IOException {
    copySources();

    Path file = this.directory.resolve("src").resolve(FIXTURES.replace('.', '/'))
        .resolve("api/METRICS.md");

    export(classes(FIXTURES), settings(file));

    String markdown = Files.readString(file, StandardCharsets.UTF_8);

    assertTrue(markdown.contains("| [`api`](.) |"), markdown);
    assertTrue(markdown.contains("[`api.OrderPort`](OrderPort.java)"), markdown);
  }

  @Test
  void shouldNotDependOnDefaultLocale() throws IOException {
    Locale locale = Locale.getDefault();
    Locale.setDefault(Locale.forLanguageTag("ar-EG"));

    try {
      exportFixtures();
    } finally {
      Locale.setDefault(locale);
    }

    for (String name : Stream.concat(Stream.of("METRICS.md"), CHARTS.stream()).toList()) {
      String content = Files.readString(this.directory.resolve(name), StandardCharsets.UTF_8);

      assertTrue(content.chars().filter(Character::isDigit).allMatch(digit -> digit <= '9'), name);
    }
  }

  @Test
  void shouldWriteNoFileIfExportFails() {
    Path file = this.directory.resolve("METRICS.md");
    MetricsMarkdownExport.Settings settings = settings(file);
    JavaClasses classes = classes(FIXTURES);
    // the export reads the namespace only after it has built the charts
    ExportContext context = TestExportContext.of(MetricsMarkdownExport.class, this.directory)
        .withNamespace(() -> {
          throw new IllegalStateException("No namespace");
        })
        .withClasses(classes)
        .withConfiguration(settings);

    assertThrows(IllegalStateException.class, () -> new MetricsMarkdownExport().export(context));

    assertFalse(Files.exists(file));
    CHARTS.forEach(chart -> assertFalse(Files.exists(this.directory.resolve(chart)), chart));
  }

  @Test
  void shouldPutChartsNextToFileAndLookUpSourcesInMavenAndGradleDirectories() {
    assertEquals("docs", MetricsMarkdownExport.file("docs/METRICS.md").imageDirectory());
    assertEquals(".", MetricsMarkdownExport.file("METRICS.md").imageDirectory());
    assertEquals("docs", MetricsMarkdownExport.file("docs\\METRICS.md").imageDirectory());
    assertEquals("/", MetricsMarkdownExport.file("/METRICS.md").imageDirectory());
    assertEquals("C:\\", MetricsMarkdownExport.file("C:\\METRICS.md").imageDirectory());
    assertEquals(List.of("src/main/java", "src/main/kotlin", "src/test/java", "src/test/kotlin"),
        MetricsMarkdownExport.file("METRICS.md").sourceDirectories());
  }

  @Test
  void shouldFailWithoutConfiguredFile() {
    Taikai taikai = Taikai.builder()
        .classes(classes(FIXTURES))
        .discoverExports(ExportChecks.only(MetricsMarkdownExport.class))
        .build();

    TaikaiException exception = assertThrows(TaikaiException.class, taikai::export);

    assertInstanceOf(IllegalStateException.class, exception.getCause());
    assertTrue(exception.getCause().getMessage().contains("add it with discoverExports("
            + "exports -> exports.configure(MetricsMarkdownExport.class, ...))"),
        exception.getCause().getMessage());
  }

  @Test
  void shouldRejectBlankFiles() {
    MetricsMarkdownExport.Settings settings = MetricsMarkdownExport.file("METRICS.md");

    assertThrows(IllegalArgumentException.class, () -> MetricsMarkdownExport.file(" "));
    assertThrows(IllegalArgumentException.class, () -> settings.withImageDirectory(" "));
    assertThrows(IllegalArgumentException.class, settings::withSourceDirectories);
    assertThrows(IllegalArgumentException.class, settings::withEntryPoints);
  }

  /**
   * The charts of generated packages, which test the limits of their layouts.
   */
  @Nested
  class Charts {

    private static final String TOO_WIDE = "This graph would be too wide to read";

    @Test
    void shouldLabelEveryDotThatHasRoom() {
      String svg = MainSequenceChart.svg(dots(10));

      assertEquals(10, count(svg, "<g class=\"dot\""));
      assertEquals(10, count(svg, "class=\"ink halo\""));
    }

    @Test
    void shouldLeaveOutLabelsWithoutFreePosition() {
      String svg = MainSequenceChart.svg(dots(600));
      int labels = count(svg, "class=\"ink halo\"");

      assertEquals(600, count(svg, "<g class=\"dot\""));
      assertTrue(labels > 0 && labels < 600, "labels: " + labels);
    }

    @Test
    void shouldStepTicksOfCouplingMapByOneTwoAndFiveUpToTheFirstAboveTheMaximum() {
      assertEquals(List.of(0, 1), CouplingChart.ticks(0));
      assertEquals(List.of(0, 1, 2), CouplingChart.ticks(1));
      assertEquals(List.of(0, 1, 2, 5, 10, 20, 50), CouplingChart.ticks(28));
      assertEquals(List.of(0, 1, 2, 5, 10, 20, 50), CouplingChart.ticks(49));
      assertEquals(List.of(0, 1, 2, 5, 10, 20, 50, 100), CouplingChart.ticks(50));
    }

    @Test
    void shouldDrawLineForEveryPowerOfTenOfFanInTimesFanOutBelowTheCorner() {
      assertEquals(0, count(CouplingChart.svg(couplings(2)), "<polyline"));
      assertEquals(1, count(CouplingChart.svg(couplings(9)), "<polyline"));
      assertEquals(3, count(CouplingChart.svg(couplings(600)), "<polyline"));
    }

    @Test
    void shouldCountClassesPerCellAndDrawAtMost20Hubs() {
      String svg = CouplingChart.svg(couplings(600));

      // fan-in 0, 1, 2 to 4, 5 to 9, 10 to 19 and 20 to 29 times fan-out 0 to 19 in 5 ranges
      assertEquals(30, count(svg, "<g class=\"density-cell\""));
      assertEquals(20, count(svg, "<g class=\"hub\""));
    }

    @Test
    void shouldMoveCountsAwayFromHubs() {
      // the hub at 3 and 3 covers the middle of the cell of fan-in and fan-out 2 to 4
      List<ClassMetric> classes = new ArrayList<>(IntStream.range(0, 11)
          .mapToObj(index -> new ClassMetric("c" + index, 2, 2))
          .toList());
      classes.add(new ClassMetric("hub", 3, 3));
      classes.add(new ClassMetric("wide", 99, 0));

      Document svg = document(CouplingChart.svg(couplings(classes)));
      Element count = texts(svg, "11").stream()
          .filter(text -> text.getTextContent().equals("12"))
          .findFirst()
          .orElseThrow();
      Svg.Box box = Svg.countBox(Double.parseDouble(count.getAttribute("x")),
          Double.parseDouble(count.getAttribute("y")), 12);

      assertEquals(2, hubCircles(svg).size());
      assertTrue(hubCircles(svg).stream().noneMatch(circle -> circle.box().overlaps(box)));
    }

    @Test
    void shouldDrawLinesBelowCounts() {
      String svg = CouplingChart.svg(couplings(600));

      assertTrue(svg.lastIndexOf("<polyline") < svg.indexOf("font-size=\"11\""));
    }

    @Test
    void shouldKeepHubsInsideThePlot() {
      Document svg = document(CouplingChart.svg(couplings(List.of(
          new ClassMetric("hub", 49, 49), new ClassMetric("leaf", 0, 0)))));
      Svg.Box hub = hubCircles(svg).get(0).box();

      assertTrue(hub.x2() + 1 <= Svg.SquarePlot.LEFT + Svg.SquarePlot.SIZE, hub.toString());
      assertTrue(hub.y1() - 1 >= Svg.SquarePlot.TOP, hub.toString());
    }

    @Test
    void shouldDrawNoteWithoutClasses() {
      String svg = CouplingChart.svg(couplings(0));

      assertTrue(svg.contains(">No classes</text>"), svg);
      assertFalse(svg.contains("density-cell"), svg);
    }

    @Test
    void shouldDrawGraphThatIsNotTooWide() {
      String svg = DependencyChart.svg(packages(25));

      assertEquals(25, count(svg, "<g class=\"package\""));
      assertFalse(svg.contains(TOO_WIDE), svg);
    }

    @Test
    void shouldReplaceGraphThatIsTooWideByNote() {
      String svg = DependencyChart.svg(packages(40));

      assertFalse(svg.contains("<g class=\"package\""), svg);
      assertTrue(svg.contains(TOO_WIDE + ", see the dependency matrix"), svg);
    }

    @Test
    void shouldTurnColumnNumbersOfSmallCellsSoThatTheyDoNotOverlap() {
      assertEquals(0, count(DependencyMatrix.packages(packages(40)), "rotate(-90"));
      assertEquals(41, count(DependencyMatrix.packages(packages(41)), "rotate(-90"));
    }

    private static int count(String svg, String text) {
      return svg.split(Pattern.quote(text), -1).length - 1;
    }

    /**
     * Returns packages without dependencies, all in one layer of the dependency graph.
     */
    private static ArchitectureMetrics packages(int packages) {
      List<String> names = IntStream.range(0, packages).mapToObj(index -> "p" + index).toList();

      return new ArchitectureMetrics("",
          names.stream().map(name -> new PackageMetric(name, 1, 0, 1, 0, 0)).toList(),
          List.of(), List.of(), List.of(), List.of(), List.of(), names, List.of(), List.of());
    }

    /**
     * Returns classes that differ in fan-in and fan-out: a fan-in of up to 29, then a higher
     * fan-out.
     */
    private static ArchitectureMetrics couplings(int classes) {
      return couplings(IntStream.range(0, classes)
          .mapToObj(index -> new ClassMetric("c" + index, index % 30, index / 30))
          .toList());
    }

    private static ArchitectureMetrics couplings(List<ClassMetric> classes) {
      return new ArchitectureMetrics("", List.of(), classes, List.of(), List.of(), List.of(),
          List.of(), List.of(), List.of(), List.of());
    }

    private static List<Svg.Circle> hubCircles(Document svg) {
      return elements(svg, "g", "hub").stream()
          .map(hub -> (Element) hub.getElementsByTagName("circle").item(0))
          .map(circle -> new Svg.Circle(Double.parseDouble(circle.getAttribute("cx")),
              Double.parseDouble(circle.getAttribute("cy")),
              Double.parseDouble(circle.getAttribute("r"))))
          .toList();
    }

    /**
     * Returns packages that are all dots of their own, in steps of 0.01 of instability, then of
     * abstractness.
     */
    private static ArchitectureMetrics dots(int packages) {
      List<PackageMetric> metrics = IntStream.range(0, packages)
          .mapToObj(index -> new PackageMetric("p" + index, 100, index / 30, 100,
              100 - index % 30, index % 30))
          .toList();

      return new ArchitectureMetrics("", metrics, List.of(), List.of(), List.of(), List.of(),
          List.of(), metrics.stream().map(PackageMetric::name).toList(), List.of(), List.of());
    }
  }

  @Nested
  class DependencyGraphs {

    @Test
    void shouldIgnoreDependenciesOnItself() {
      DependencyGraph graph = graph(2, 0, 0, 0, 1);

      assertEquals(Set.of(1), graph.targets(0));
    }

    @Test
    void shouldReturnComponentsAfterTheComponentsTheyDependOn() {
      DependencyGraph graph = graph(5, 0, 1, 1, 2, 2, 1, 2, 3, 4, 0);

      assertEquals(List.of(List.of(3), List.of(1, 2), List.of(0), List.of(4)),
          graph.components());
    }

    @Test
    void shouldHandleLongChainsWithoutRecursion() {
      int nodes = 100_000;
      DependencyGraph graph = new DependencyGraph(nodes);

      for (int node = 0; node < nodes - 1; node++) {
        graph.add(node, node + 1);
      }

      graph.add(nodes - 1, 0);

      assertEquals(1, graph.components().size());
      assertEquals(nodes, graph.components().get(0).size());
    }

    /**
     * Returns a graph with the dependencies given as pairs of origin and target.
     */
    private static DependencyGraph graph(int nodes, int... dependencies) {
      DependencyGraph graph = new DependencyGraph(nodes);

      for (int index = 0; index < dependencies.length; index += 2) {
        graph.add(dependencies[index], dependencies[index + 1]);
      }

      return graph;
    }
  }

  /**
   * The cohesion of the fixtures at the end of this class, each of which says what connects its
   * methods.
   */
  @Nested
  class ClassCohesion {

    @ParameterizedTest
    @MethodSource("fixtures")
    void shouldCountGroupsOfConnectedMethods(Class<?> fixture, int methods, int groups) {
      assertEquals(methods == 0
              ? Optional.empty()
              : Optional.of(new CohesionMetric(fixture.getName(), methods, groups)),
          Cohesion.of(List.of(new ClassFileImporter().importClass(fixture))).stream()
              .findFirst());
    }

    /**
     * Returns every fixture with its counted methods and groups, or none if it is not measured.
     */
    static Stream<Arguments> fixtures() {
      return Stream.of(
          Arguments.of(ViaGetter.class, 2, 1),
          Arguments.of(ViaStaticMethod.class, 2, 1),
          Arguments.of(ViaAbstractMethod.class, 2, 1),
          Arguments.of(ViaInheritedMethod.class, 2, 1),
          Arguments.of(ViaStaticField.class, 2, 2),
          Arguments.of(NoGetter.class, 2, 2),
          Arguments.of(RecordStyleGetter.class, 2, 1),
          Arguments.of(WithUnconnectedMethods.class, 2, 1),
          Arguments.of(WithOuterObject.Inner.class, 2, 2),
          Arguments.of(NoOperations.class, 0, 0));
    }
  }

  /**
   * Exports all fixtures to {@code METRICS.md} in the temporary directory.
   */
  private Path exportFixtures() {
    return exportFixtures(FIXTURES);
  }

  /**
   * Exports the classes of the package with the given entry points and returns the Markdown.
   */
  private String exportEntryPoints(String packageName, EntryPoints... entryPoints)
      throws IOException {
    Path file = this.directory.resolve("METRICS.md");
    export(classes(packageName), settings(file).withEntryPoints(entryPoints));

    return Files.readString(file, StandardCharsets.UTF_8);
  }

  private Path exportFixtures(String packageName) {
    Path file = this.directory.resolve("METRICS.md");
    export(classes(packageName), settings(file));

    return file;
  }

  /**
   * Looks up the sources in {@code src} of the temporary directory, which {@link #copySources()}
   * fills.
   */
  private MetricsMarkdownExport.Settings settings(Path file) {
    return MetricsMarkdownExport.file(file.toString())
        .withSourceDirectories(this.directory.resolve("src").toString());
  }

  /**
   * Copies the fixtures, so that the links to them do not depend on where the project is.
   */
  private void copySources() throws IOException {
    Path sources = Path.of("src/test/java");
    Path fixtures = sources.resolve(FIXTURES.replace('.', '/'));

    try (Stream<Path> paths = Files.walk(fixtures)) {
      for (Path path : paths.filter(Files::isRegularFile).toList()) {
        Path target = this.directory.resolve("src").resolve(sources.relativize(path).toString());
        Files.createDirectories(target.getParent());
        Files.copy(path, target);
      }
    }
  }

  private static String link(Path file, Path target) {
    return file.getParent().relativize(target).toString().replace(File.separatorChar, '/');
  }

  private static JavaClasses classes(String packageName) {
    return new ClassFileImporter().importPackages(packageName);
  }

  private static void export(JavaClasses classes, MetricsMarkdownExport.Settings settings) {
    ExportChecks.export(Taikai.builder().classes(classes), MetricsMarkdownExport.class, settings);
  }

  /**
   * Returns the zone of every group of the given class, by its label.
   */
  private static Map<String, String> zones(Path chart, String groupClass) {
    Map<String, String> zoneByLabel = new LinkedHashMap<>();
    List<Element> groups = groups(chart, groupClass);
    groups.forEach(group -> zoneByLabel.put(label(group), group.getAttribute("data-zone")));

    assertEquals(groups.size(), zoneByLabel.size(), "Labels are unique: " + zoneByLabel);
    return zoneByLabel;
  }

  private static List<String> titles(Document svg, String tag, String elementClass) {
    return elements(svg, tag, elementClass).stream()
        .map(element -> element.getElementsByTagName("title").item(0).getTextContent())
        .toList();
  }

  private static List<Element> groups(Path chart, String groupClass) {
    return elements(svg(chart), "g", groupClass);
  }

  private static String label(Element group) {
    return group.getElementsByTagName("text").item(0).getTextContent();
  }

  /**
   * Returns the counts of the cells, the texts in their font size of 11.
   */
  private static List<String> counts(Document svg) {
    return texts(svg, "11").stream().map(Element::getTextContent).toList();
  }

  private static List<Element> texts(Document svg, String fontSize) {
    NodeList nodes = svg.getElementsByTagName("text");

    return IntStream.range(0, nodes.getLength())
        .mapToObj(index -> (Element) nodes.item(index))
        .filter(text -> text.getAttribute("font-size").equals(fontSize))
        .toList();
  }

  private static Document document(String svg) {
    try {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder()
          .parse(new InputSource(new StringReader(svg)));
    } catch (Exception e) {
      throw new AssertionError("The chart is no valid XML", e);
    }
  }

  private static Document svg(Path chart) {
    try {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(chart.toFile());
    } catch (Exception e) {
      throw new AssertionError("The chart %s is no valid XML".formatted(chart), e);
    }
  }

  private static List<Element> elements(Document document, String tag, String elementClass) {
    NodeList nodes = document.getElementsByTagName(tag);
    List<Element> elements = new ArrayList<>();

    for (int index = 0; index < nodes.getLength(); index++) {
      Element element = (Element) nodes.item(index);

      if (element.getAttribute("class").equals(elementClass)) {
        elements.add(element);
      }
    }

    return elements;
  }

  @Retention(RetentionPolicy.SOURCE)
  @interface SourceOnly {

  }

  /**
   * Both methods use the name, one of them through its getter.
   */
  static class ViaGetter {

    private String name;

    String getName() {
      return this.name;
    }

    int length() {
      return getName().length();
    }

    boolean blank() {
      return this.name.isBlank();
    }
  }

  /**
   * The methods use different fields, but both call the same static method.
   */
  static class ViaStaticMethod {

    private int width;
    private int height;

    private static int twice(int value) {
      return 2 * value;
    }

    int doubledWidth() {
      return twice(this.width);
    }

    int doubledHeight() {
      return twice(this.height);
    }
  }

  /**
   * Both methods call the same abstract method.
   */
  abstract static class ViaAbstractMethod {

    abstract String delegate();

    int length() {
      return delegate().length();
    }

    boolean blank() {
      return delegate().isBlank();
    }
  }

  /**
   * The superclass of {@link ViaInheritedMethod}.
   */
  static class Registry {

    private final List<String> names = new ArrayList<>();

    void register(String name) {
      this.names.add(name);
    }
  }

  /**
   * Both methods call the same inherited method.
   */
  static class ViaInheritedMethod extends Registry {

    void registerFirst() {
      register("first");
    }

    void registerSecond() {
      register("second");
    }
  }

  /**
   * The methods use different fields and only share a static field, which does not connect
   * them.
   */
  static class ViaStaticField {

    private static final List<String> LOG = new ArrayList<>();

    private String title;
    private int count;

    String heading() {
      LOG.add("heading");
      return this.title.strip();
    }

    int next() {
      LOG.add("next");
      return this.count + 1;
    }
  }

  /**
   * The methods use different fields. {@link #describe()} uses only the id, but is not named like
   * a getter.
   */
  static class NoGetter {

    private String id;
    private int count;

    String describe() {
      return "Order " + this.id;
    }

    int next() {
      return this.count + 1;
    }
  }

  /**
   * Both methods use the name, one of them through {@link #name()}, a getter in the style of a
   * record, which does not count.
   */
  static class RecordStyleGetter {

    private String name;

    String name() {
      return this.name;
    }

    int length() {
      return this.name.length();
    }

    boolean blank() {
      return name().isBlank();
    }
  }

  /**
   * Two methods use the name. The other two use nothing of their object and do not count.
   */
  static class WithUnconnectedMethods {

    private String name;

    int length() {
      return this.name.length();
    }

    boolean blank() {
      return this.name.isBlank();
    }

    String constant() {
      return "constant";
    }

    void nothing() {
    }
  }

  /**
   * The methods of the inner class use different fields of their own and of the outer object,
   * which they reach through the same field that the compiler generates.
   */
  static class WithOuterObject {

    private int first;
    private int second;

    class Inner {

      private int ownFirst;
      private int ownSecond;

      void copyFirst() {
        this.ownFirst = first;
      }

      void copySecond() {
        this.ownSecond = second;
      }
    }
  }

  /**
   * No method uses anything of its object, so the cohesion of the class is not measured.
   */
  static class NoOperations {

    void first() {
    }

    void second() {
    }
  }
}
