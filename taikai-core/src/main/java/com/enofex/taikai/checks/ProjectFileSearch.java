package com.enofex.taikai.checks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Finds the files of a project directory that match glob patterns, sharing the walk of a directory
 * between patterns.
 */
final class ProjectFileSearch {

  private final Path projectDirectory;
  private final Map<Search, List<Path>> filesBySearch = new HashMap<>();

  ProjectFileSearch(Path projectDirectory) {
    this.projectDirectory = projectDirectory;
  }

  List<Path> files(String pattern) {
    List<PathMatcher> matchers = matchers(pattern);
    Search search = Search.of(this.projectDirectory, pattern);

    return this.filesBySearch.computeIfAbsent(search, Search::files).stream()
        .filter(file -> matchers.stream()
            .anyMatch(matcher -> matcher.matches(search.asInPattern(file))))
        .map(this.projectDirectory::relativize)
        .toList();
  }

  private static List<PathMatcher> matchers(String pattern) {
    Set<String> variants = new LinkedHashSet<>(List.of(pattern));
    boolean added = true;

    while (added) {
      added = false;

      for (String variant : List.copyOf(variants)) {
        for (int index = variant.indexOf("**/"); index >= 0;
            index = variant.indexOf("**/", index + 1)) {
          added |= variants.add(variant.substring(0, index) + variant.substring(index + 3));
        }
      }
    }

    return variants.stream()
        .map(variant -> FileSystems.getDefault().getPathMatcher("glob:" + variant))
        .toList();
  }

  /**
   * The directory before the first wildcard of a pattern, as the pattern names it in
   * {@code prefix}, for example {@code ..}, and as the path that is searched.
   */
  private record Search(Path prefix, Path directory, int depth) {

    static Search of(Path projectDirectory, String pattern) {
      String[] segments = pattern.split("/");
      int fixedSegments = 0;

      while (fixedSegments < segments.length - 1 && !isWildcard(segments[fixedSegments])) {
        fixedSegments++;
      }

      String prefix = String.join("/", Arrays.asList(segments).subList(0, fixedSegments));

      return new Search(Path.of(prefix), projectDirectory.resolve(prefix),
          pattern.contains("**") ? Integer.MAX_VALUE : segments.length - fixedSegments);
    }

    /**
     * Returns a found file as the pattern names it, for example {@code ../module/.env} for the
     * {@code .env} of the project directory {@code module} and the pattern
     * <code>../**&#47;.env</code>.
     * A path relative to the project directory would normalize it to {@code .env}, which the
     * pattern does not match.
     */
    Path asInPattern(Path file) {
      return this.prefix.resolve(this.directory.relativize(file));
    }

    private static boolean isWildcard(String segment) {
      return segment.chars().anyMatch(character -> "*?[{".indexOf(character) >= 0);
    }

    /**
     * Returns the regular files of the directory, as deep as the pattern reaches. Entries below the
     * directory that cannot be read, such as the data directory of a database container or a file
     * deleted during the search, are skipped, but the directory itself must be readable.
     */
    List<Path> files() {
      if (!Files.isDirectory(this.directory)) {
        return List.of();
      }

      List<Path> files = new ArrayList<>();

      try {
        Files.walkFileTree(this.directory, EnumSet.noneOf(FileVisitOption.class), this.depth,
            new SimpleFileVisitor<>() {
              @Override
              public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                if (Files.isRegularFile(file)) {
                  files.add(file);
                }

                return FileVisitResult.CONTINUE;
              }

              @Override
              public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
                if (file.equals(Search.this.directory)) {
                  throw e;
                }

                return FileVisitResult.CONTINUE;
              }
            });
      } catch (IOException e) {
        throw new UncheckedIOException("Could not search " + this.directory, e);
      }

      return List.copyOf(files);
    }
  }
}
