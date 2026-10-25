package com.enofex.taikai.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServiceRegistrationsCheckTest {

  private static final String MAIN_SERVICES = "src/main/resources/META-INF/services";
  private static final String TEST_SERVICES = "src/test/resources/META-INF/services";
  private static final String MODULE_SERVICES = "module/src/test/resources/META-INF/services";
  private static final String SERVICE = TaikaiCheck.class.getName();

  private final ServiceRegistrationsCheck check = new ServiceRegistrationsCheck();

  @TempDir
  private Path directory;

  @Test
  void shouldDescribeCheck() {
    assertEquals("Service registrations should refer to valid providers",
        this.check.description());
  }

  @Test
  void shouldPassWithoutRegistrations() {
    assertEquals(List.of(), this.check.violations(context()));
  }

  @Test
  void shouldPassForValidRegistrationsIgnoringCommentsAndBlankLines() throws IOException {
    register(MAIN_SERVICES, SERVICE, """
        # predefined checks
        com.enofex.taikai.checks.MavenWrapperCheck

        com.enofex.taikai.checks.RequiredFilesCheck  # with comment
        """);
    register(TEST_SERVICES, SERVICE, "com.enofex.taikai.checks.ServiceRegistrationsCheck");

    assertEquals(List.of(), this.check.violations(context()));
  }

  @Test
  void shouldReportInvalidProviders() throws IOException {
    register(TEST_SERVICES, SERVICE, String.join(System.lineSeparator(),
        "com.example.MissingCheck",
        String.class.getName(),
        PackagePrivateCheck.class.getName(),
        AbstractCheck.class.getName(),
        ParameterizedCheck.class.getName()));
    String registration = Path.of(TEST_SERVICES, SERVICE).toString();

    assertEquals(List.of(
            "Provider com.example.MissingCheck registered in %s does not exist"
                .formatted(registration),
            "Provider java.lang.String registered in %s does not implement %s"
                .formatted(registration, SERVICE),
            notInstantiable(PackagePrivateCheck.class, registration),
            notInstantiable(AbstractCheck.class, registration),
            notInstantiable(ParameterizedCheck.class, registration)),
        this.check.violations(context()));
  }

  @Test
  void shouldReportMissingService() throws IOException {
    register(MAIN_SERVICES, "com.example.MissingService", "com.example.Provider");

    assertEquals(List.of("Service com.example.MissingService registered in %s does not exist"
            .formatted(Path.of(MAIN_SERVICES, "com.example.MissingService"))),
        this.check.violations(context()));
  }

  @Test
  void shouldCheckConfiguredDirectoriesInsteadOfDefaultOnes() throws IOException {
    register(MODULE_SERVICES, SERVICE, "com.example.MissingCheck");
    register(TEST_SERVICES, SERVICE, "com.example.IgnoredCheck");

    assertEquals(List.of("Provider com.example.MissingCheck registered in %s does not exist"
            .formatted(Path.of(MODULE_SERVICES, SERVICE))),
        this.check.violations(context()
            .withConfiguration(ServiceRegistrationsCheck.directories(MODULE_SERVICES))));
  }

  @Test
  void shouldReportMissingConfiguredDirectory() {
    assertEquals(List.of("%s is missing in %s".formatted(MODULE_SERVICES, this.directory)),
        this.check.violations(context()
            .withConfiguration(ServiceRegistrationsCheck.directories(MODULE_SERVICES))));
  }

  @Test
  void shouldRejectSettingsWithoutDirectories() {
    assertThrows(IllegalArgumentException.class, ServiceRegistrationsCheck::directories);
  }

  private TestCheckContext context() {
    return TestCheckContext.of(ServiceRegistrationsCheck.class, this.directory);
  }

  private void register(String servicesDirectory, String service, String providers)
      throws IOException {
    Path directory = Files.createDirectories(this.directory.resolve(servicesDirectory));
    Files.writeString(directory.resolve(service), providers);
  }

  private static String notInstantiable(Class<?> provider, String registration) {
    return ("Provider %s registered in %s is not a public class with a public constructor "
        + "without parameters").formatted(provider.getName(), registration);
  }

  static final class PackagePrivateCheck implements TaikaiCheck {

    @Override
    public String description() {
      return "A package-private check";
    }

    @Override
    public List<String> violations(CheckContext context) {
      return List.of();
    }
  }

  public abstract static class AbstractCheck implements TaikaiCheck {
  }

  public static final class ParameterizedCheck implements TaikaiCheck {

    private final String name;

    public ParameterizedCheck(String name) {
      this.name = name;
    }

    @Override
    public String description() {
      return this.name;
    }

    @Override
    public List<String> violations(CheckContext context) {
      return List.of();
    }
  }
}
