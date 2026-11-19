package com.enofex.taikai;

import static com.enofex.taikai.java.ImportPatterns.lombok;
import static com.enofex.taikai.java.ImportPatterns.shaded;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.enofex.taikai.checks.ClassFileVersionCheck;
import com.enofex.taikai.checks.FileContentCheck;
import com.enofex.taikai.checks.ForbiddenFilesCheck;
import com.enofex.taikai.checks.MavenWrapperCheck;
import com.enofex.taikai.checks.RequiredFilesCheck;
import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import com.enofex.taikai.configures.AbstractConfigurer;
import com.enofex.taikai.configures.Configurer;
import com.enofex.taikai.core.ClassCriteria;
import com.enofex.taikai.core.FieldCriteria;
import com.enofex.taikai.core.TaikaiRule;
import com.enofex.taikai.exports.AgentsMarkdownExport;
import com.enofex.taikai.exports.MetricsMarkdownExport;
import com.enofex.taikai.exports.SarifExport;
import com.enofex.taikai.exports.spi.ExportContext;
import com.enofex.taikai.exports.spi.TaikaiExport;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

  private static final Taikai TAIKAI = Taikai.builder()
      .namespace("com.enofex.taikai")
      .java(java -> java
          .noUsageOfDeprecatedAPIs()
          .noUsageOfSystemOutOrErr()
          .noUsageOf(Date.class)
          .noUsageOf(Calendar.class)
          .noUsageOf(SimpleDateFormat.class)
          .fieldsShouldHaveModifiers(FieldCriteria.nameMatching("^[A-Z][A-Z0-9_]*$"),
              List.of(JavaModifier.STATIC, JavaModifier.FINAL))
          .classesShouldImplementHashCodeAndEquals()
          .finalClassesShouldNotHaveProtectedMembers()
          .utilityClassesShouldBeFinalAndHavePrivateConstructor()
          .methodsShouldNotDeclareGenericExceptions()
          .fieldsShouldNotBePublic()
          .serialVersionUIDFieldsShouldBeStaticFinalLong()
          .classesShouldBeNullMarked()
          .classesShouldResideInPackage("com.enofex.taikai..")
          .imports(imports -> imports
              .shouldHaveNoCycles()
              .shouldNotImport(ClassCriteria.inPackage("org.springframework.core.annotation.."))
              .shouldNotImport(ClassCriteria.inPackage("jakarta.annotation.."))
              .shouldNotImport(ClassCriteria.inPackage("javax.annotation.."))
              .shouldNotImport(ClassCriteria.inPackage("org.jetbrains.annotations.."))
              .shouldNotImport(shaded())
              .shouldNotImport(lombok()))
          .naming(naming -> naming
              .packagesShouldMatchDefault()
              .fieldsShouldNotMatch(".*(List|Set|Map)$")
              .classesShouldNotMatch(".*Impl")
              .classesShouldMatch(ClassCriteria.assignableTo(AbstractConfigurer.class),
                  ".*Configurer")
              .classesShouldMatch(ClassCriteria.implementing(Configurer.class), ".*Configurer")
              .classesShouldMatch(ClassCriteria.implementing(TaikaiCheck.class), ".*Check")
              .classesShouldMatch(ClassCriteria.implementing(TaikaiExport.class), ".*Export")
              .classesShouldMatch(ClassCriteria.implementing(CheckContext.class),
                  ".*CheckContext")
              .classesShouldMatch(ClassCriteria.implementing(ExportContext.class),
                  ".*ExportContext")
              .interfacesShouldNotHavePrefixI()
              .enumConstantsShouldFollowConventions()
              .constantsShouldFollowConventions()))
      .metrics(metrics -> metrics
          .classesShouldNotExceedMaxInheritanceDepth(2)
          .packagesShouldNotExceedMaxClasses(20)
          .classesShouldNotExceedMaxDependencies(25))
      .addRule(TaikaiRule.of(slices()
          .matching("com.enofex.(**)")
          .should().beFreeOfCycles()
          .as("Packages of com.enofex.taikai, the root package included, should be free of "
              + "cycles")))
      .discoverChecks(checks -> checks
          .exclude(FileContentCheck.class)
          .configure(RequiredFilesCheck.class, RequiredFilesCheck.files(
              "../README.md", "../LICENSE", "../CONTRIBUTING.md", "../CODE_OF_CONDUCT.md",
              "../SECURITY.md"))
          .configure(MavenWrapperCheck.class, MavenWrapperCheck.directories(".."))
          .configure(ForbiddenFilesCheck.class, ForbiddenFilesCheck.files(
              "src/main/**/*.pem", "src/main/**/*.key", "src/main/**/*.p12", "src/main/**/*.jks",
              "src/main/**/.env", "src/main/**/*.class", "src/main/**/*.jar"))
          .configure(ClassFileVersionCheck.class, ClassFileVersionCheck.javaVersion(17)))
      .build();

  @Test
  void taikai() {
    TAIKAI.checkAll();
  }

  static final class ExportAgents {

    public static void main(String[] args) {
      export(AgentsMarkdownExport.class, AgentsMarkdownExport.file(inModule("../ARCHITECTURE.md")));
    }
  }

  static final class ExportMetrics {

    public static void main(String[] args) {
      export(MetricsMarkdownExport.class, MetricsMarkdownExport.file(inModule("../METRICS.md"))
          .withImageDirectory(inModule("../metrics"))
          .withSourceDirectories(inModule("src/main/java"), inModule("src/test/java")));
    }
  }

  static final class ExportSarif {

    public static void main(String[] args) {
      export(SarifExport.class, SarifExport.file(inModule("target/taikai.sarif"))
          .withSourceDirectories(inModule("src/main/java"), inModule("src/test/java")));
    }
  }

  /**
   * Returns the absolute path of a path relative to this module, so that the exports write the
   * same files from any working directory, such as the root of the repository in an IDE.
   */
  private static String inModule(String path) {
    try {
      Path testClasses = Path.of(ArchitectureTest.class.getProtectionDomain().getCodeSource()
          .getLocation().toURI());

      return testClasses.getParent().getParent().resolve(path).normalize().toString();
    } catch (URISyntaxException e) {
      throw new IllegalStateException("The directory of the module cannot be found", e);
    }
  }

  private static void export(Class<? extends TaikaiExport> export, Object settings) {
    TAIKAI.toBuilder()
        .discoverExports(exports -> {
          ServiceLoader.load(TaikaiExport.class).stream()
              .map(ServiceLoader.Provider::type)
              .filter(type -> !type.equals(export))
              .forEach(exports::exclude);
          exports.configure(export, settings);
        })
        .build()
        .export();
  }
}
