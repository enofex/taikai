package com.enofex.taikai.checks;

import com.enofex.taikai.checks.spi.CheckContext;
import com.enofex.taikai.checks.spi.TaikaiCheck;
import java.util.List;

/**
 * Checks that the project has the Maven Wrapper, so that it can be built without a local Maven
 * installation: the scripts {@code mvnw} and {@code mvnw.cmd}, and the file
 * {@code .mvn/wrapper/maven-wrapper.properties}, none of them empty. The files are looked up in the
 * {@linkplain CheckContext#projectDirectory() project directory}.
 *
 * <p>Taikai does not register its predefined checks itself, so that a new Taikai version never adds
 * checks on its own. To run this check, list its class name in the file
 * {@code src/test/resources/META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck} of the
 * project and call {@code discoverChecks()}.</p>
 */
public final class MavenWrapperCheck implements TaikaiCheck {

  private static final List<String> WRAPPER_FILES = List.of(
      "mvnw", "mvnw.cmd", ".mvn/wrapper/maven-wrapper.properties");

  @Override
  public String description() {
    return "Project should have the Maven Wrapper";
  }

  @Override
  public List<String> violations(CheckContext context) {
    return ProjectFiles.missingOrEmpty(context.projectDirectory(), WRAPPER_FILES);
  }
}
