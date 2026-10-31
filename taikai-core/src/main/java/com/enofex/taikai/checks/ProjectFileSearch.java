package com.enofex.taikai.checks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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

    return this.filesBySearch
        .computeIfAbsent(Search.of(this.projectDirectory, pattern), Search::files).stream()
        .map(this.projectDirectory::relativize)
        .filter(file -> matchers.stream().anyMatch(matcher -> matcher.matches(file)))
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

  private record Search(Path directory, int depth) {

    static Search of(Path projectDirectory, String pattern) {
      String[] segments = pattern.split("/");
      int fixedSegments = 0;

      while (fixedSegments < segments.length - 1 && !isWildcard(segments[fixedSegments])) {
        fixedSegments++;
      }

      return new Search(
          projectDirectory.resolve(String.join("/",
              Arrays.asList(segments).subList(0, fixedSegments))),
          pattern.contains("**") ? Integer.MAX_VALUE : segments.length - fixedSegments);
    }

    private static boolean isWildcard(String segment) {
      return segment.chars().anyMatch(character -> "*?[{".indexOf(character) >= 0);
    }

    List<Path> files() {
      if (!Files.isDirectory(this.directory)) {
        return List.of();
      }

      try (Stream<Path> files = Files.walk(this.directory, this.depth)) {
        return files.filter(Files::isRegularFile).toList();
      } catch (IOException e) {
        throw new UncheckedIOException("Could not search " + this.directory, e);
      }
    }
  }
}
