package com.enofex.taikai.checks;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import com.tngtech.archunit.core.domain.JavaClass;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Checks that the {@linkplain CheckContext#classes() classes} of the project are not compiled for
 * a newer Java version than configured, for example to keep a library usable on older Java
 * versions. The Java version is read from the header of each class file. The check needs a
 * configuration:
 *
 * <pre>{@code
 * .discoverChecks(checks -> checks
 *     .configure(ClassFileVersionCheck.class, ClassFileVersionCheck.javaVersion(17)))
 * }</pre>
 *
 * <p>Taikai does not register its predefined checks itself, so that a new Taikai version never adds
 * checks on its own. To run this check, list its class name in the file
 * {@code src/test/resources/META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck} of the
 * project and call {@code discoverChecks()}.</p>
 */
public final class ClassFileVersionCheck implements TaikaiCheck {

  private static final int CLASS_FILE_MAGIC = 0xCAFEBABE;
  private static final int CLASS_FILE_VERSION_OF_JAVA_ZERO = 44;

  /**
   * Returns the settings that allow classes compiled for at most the given Java version.
   *
   * @param javaVersion the newest allowed Java version, for example {@code 17}
   * @return the settings
   */
  public static Settings javaVersion(int javaVersion) {
    return new Settings(javaVersion);
  }

  @Override
  public String description() {
    return "Classes should not be compiled for a newer Java version than configured";
  }

  @Override
  public List<String> violations(CheckContext context) {
    int allowedJavaVersion = context.requireConfiguration(Settings.class).javaVersion();

    return context.classes().stream()
        .sorted(Comparator.comparing(JavaClass::getName))
        .flatMap(javaClass -> violation(javaClass, allowedJavaVersion).stream())
        .toList();
  }

  private static Optional<String> violation(JavaClass javaClass, int allowedJavaVersion) {
    int compiledJavaVersion = compiledJavaVersion(javaClass);

    return compiledJavaVersion > allowedJavaVersion
        ? Optional.of("Class %s is compiled for Java %d, but at most Java %d is allowed"
            .formatted(javaClass.getName(), compiledJavaVersion, allowedJavaVersion))
        : Optional.empty();
  }

  private static int compiledJavaVersion(JavaClass javaClass) {
    URI classFile = javaClass.getSource()
        .orElseThrow(() -> new IllegalStateException(
            "The class file of %s is unknown".formatted(javaClass.getName())))
        .getUri();

    try (InputStream stream = classFile.toURL().openStream();
        DataInputStream input = new DataInputStream(stream)) {
      if (input.readInt() != CLASS_FILE_MAGIC) {
        throw new IllegalStateException("%s is not a class file".formatted(classFile));
      }

      input.readUnsignedShort();
      return input.readUnsignedShort() - CLASS_FILE_VERSION_OF_JAVA_ZERO;
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read " + classFile, e);
    }
  }

  /**
   * The settings of the {@link ClassFileVersionCheck}.
   *
   * @param javaVersion the newest allowed Java version, for example {@code 17}
   */
  public record Settings(int javaVersion) {

    /**
     * Creates the settings.
     *
     * @param javaVersion the newest allowed Java version, for example {@code 17}
     * @throws IllegalArgumentException if the Java version is not positive
     */
    public Settings {
      if (javaVersion < 1) {
        throw new IllegalArgumentException(
            "The Java version must be positive, but is %d".formatted(javaVersion));
      }
    }
  }
}
