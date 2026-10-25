package com.enofex.taikai.checks.spi;

import java.util.List;

/**
 * A check of the project that is not expressed as an ArchUnit rule, for example of files in the
 * project directory, of resources, or of the classpath.
 *
 * <p>Checks run together with the rules of a {@link com.enofex.taikai.Taikai} configuration: they
 * are part of the report of {@code check()} and {@code checkAll()}, and each check becomes a test
 * of its own in {@code dynamicTests()}.</p>
 *
 * <p>Checks are discovered with {@code discoverChecks()} through the
 * {@link java.util.ServiceLoader} mechanism. To register a check, list its fully qualified class
 * name in the file {@code META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck}. A check
 * must be a public class with a public constructor without parameters.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public final class LicenseCheck implements TaikaiCheck {
 *
 *   @Override
 *   public String description() {
 *     return "Project should have a LICENSE";
 *   }
 *
 *   @Override
 *   public List<String> violations(CheckContext context) {
 *     return Files.exists(context.projectDirectory().resolve("LICENSE"))
 *         ? List.of()
 *         : List.of("LICENSE is missing in " + context.projectDirectory());
 *   }
 * }
 * }</pre>
 *
 * @see com.enofex.taikai.Taikai.Builder#discoverChecks(java.util.function.Consumer)
 * @see com.enofex.taikai.ChecksConfigurer
 * @see com.enofex.taikai.checks.RequiredFilesCheck
 */
public interface TaikaiCheck {

  /**
   * Returns the description of this check. It names the check in failure reports and is the name
   * of its dynamic test, for example {@code Project should have a LICENSE}.
   *
   * @return the description
   */
  String description();

  /**
   * Runs this check and returns its violations.
   *
   * <p>If this method throws an exception, fails an assertion or misses a class, this is reported
   * as a violation of this check, like any other violation:
   * {@link com.enofex.taikai.Taikai#checkAll()} and {@link com.enofex.taikai.Taikai#dynamicTests()}
   * still run the other rules and checks, while {@link com.enofex.taikai.Taikai#check()} stops at
   * it.</p>
   *
   * @param context the project that is checked
   * @return the violations, one message each, or an empty list if the check passes
   */
  List<String> violations(CheckContext context);
}
