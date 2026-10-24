package com.enofex.taikai.checks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class ProjectFiles {

  private ProjectFiles() {
  }

  static List<String> missingOrEmpty(Path projectDirectory, Collection<String> files) {
    List<String> violations = new ArrayList<>();

    for (String file : files) {
      Path path = projectDirectory.resolve(file);

      if (!Files.isRegularFile(path)) {
        violations.add("%s is missing in %s".formatted(file, projectDirectory));
      } else if (isBlank(path)) {
        violations.add("%s is empty".formatted(path));
      }
    }

    return violations;
  }

  private static boolean isBlank(Path file) {
    try {
      return Files.readString(file, StandardCharsets.UTF_8).isBlank();
    } catch (CharacterCodingException e) {
      // not UTF-8 text, such as the Gradle Wrapper JAR, so the file has content
      return false;
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read " + file, e);
    }
  }
}
