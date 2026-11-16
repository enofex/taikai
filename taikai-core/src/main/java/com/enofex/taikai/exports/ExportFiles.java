package com.enofex.taikai.exports;

import static java.util.Objects.requireNonNull;

import com.enofex.taikai.exports.spi.ExportContext;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the files of the exports.
 */
final class ExportFiles {

  private ExportFiles() {
  }

  /**
   * Returns the given file of the settings of an export, which must not be blank.
   */
  static String requireFile(String file) {
    return requireNotBlank(file, "The file to export to must be given");
  }

  static String requireNotBlank(String path, String message) {
    requireNonNull(path);

    if (path.isBlank()) {
      throw new IllegalArgumentException(message);
    }

    return path;
  }

  /**
   * Resolves a path of the settings of an export against the project directory.
   */
  static Path resolve(ExportContext context, String path) {
    return context.projectDirectory().resolve(path).normalize();
  }

  /**
   * Writes the content in UTF-8 and creates the missing parent directories.
   */
  static void write(Path file, CharSequence content) {
    try {
      Path directory = file.getParent();

      if (directory != null) {
        Files.createDirectories(directory);
      }

      Files.writeString(file, content, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not write " + file, e);
    }
  }
}
