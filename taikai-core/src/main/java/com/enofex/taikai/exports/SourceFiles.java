package com.enofex.taikai.exports;

import com.tngtech.archunit.core.domain.SourceCodeLocation;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Finds the source files of classes: in the directory of their package, or else the only file of
 * their name that declares the package, as Kotlin files may be anywhere.
 */
final class SourceFiles {

  /**
   * The source directories of Maven and Gradle.
   */
  static final List<String> DEFAULT_DIRECTORIES = List.of(
      "src/main/java", "src/main/kotlin", "src/test/java", "src/test/kotlin");

  private static final Pattern PACKAGE =
      Pattern.compile("^[\\s\\uFEFF]*package\\s+([\\w.]+)", Pattern.MULTILINE);

  private final List<Path> directories;
  private final Map<String, Optional<Path>> files = new HashMap<>();
  private final Map<String, Optional<Path>> packageDirectories = new HashMap<>();
  private @Nullable Map<String, List<Path>> byName;

  private SourceFiles(List<Path> directories) {
    this.directories = directories;
  }

  static SourceFiles in(Path projectDirectory, List<String> directories) {
    return new SourceFiles(directories.stream()
        .map(directory -> projectDirectory.resolve(directory).normalize())
        .toList());
  }

  Optional<Path> find(SourceCodeLocation location) {
    String packageName = location.getSourceClass().getPackageName();
    String fileName = location.getSourceFileName();

    return this.files.computeIfAbsent(packageName + "/" + fileName,
        key -> inPackageDirectory(packageName, fileName)
            .or(() -> uniqueByName(packageName, fileName)));
  }

  /**
   * Returns the directory of a package in the first source directory that has it.
   */
  Optional<Path> directory(String packageName) {
    return this.packageDirectories.computeIfAbsent(packageName, key -> this.directories.stream()
        .map(directory -> directory.resolve(packageName.replace('.', '/')))
        .filter(Files::isDirectory)
        .findFirst());
  }

  private Optional<Path> inPackageDirectory(String packageName, String fileName) {
    String packageDirectory = packageName.replace('.', '/');

    return this.directories.stream()
        .map(directory -> directory.resolve(packageDirectory).resolve(fileName))
        .filter(Files::isRegularFile)
        .findFirst();
  }

  private Optional<Path> uniqueByName(String packageName, String fileName) {
    List<Path> files = byName().getOrDefault(fileName, List.of()).stream()
        .filter(file -> declares(file, packageName))
        .toList();

    return files.size() == 1 ? Optional.of(files.get(0)) : Optional.empty();
  }

  /**
   * Reads the file leniently, so that a byte order mark or bytes that are not UTF-8 do not hide the
   * package.
   */
  private static boolean declares(Path file, String packageName) {
    try {
      Matcher matcher = PACKAGE.matcher(
          new String(Files.readAllBytes(file), StandardCharsets.UTF_8));

      return (matcher.find() ? matcher.group(1) : "").equals(packageName);
    } catch (IOException e) {
      return false;
    }
  }

  private Map<String, List<Path>> byName() {
    Map<String, List<Path>> files = this.byName;

    if (files == null) {
      files = new HashMap<>();

      for (Path directory : this.directories) {
        addFiles(directory, files);
      }

      this.byName = files;
    }

    return files;
  }

  /**
   * Skips the files that cannot be read.
   */
  private static void addFiles(Path directory, Map<String, List<Path>> files) {
    if (!Files.isDirectory(directory)) {
      return;
    }

    try {
      Files.walkFileTree(directory, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
          if (Files.isRegularFile(file)) {
            files.computeIfAbsent(file.getFileName().toString(), name -> new ArrayList<>())
                .add(file);
          }

          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path directory, @Nullable IOException e) {
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      throw new UncheckedIOException("Could not read " + directory, e);
    }
  }
}
