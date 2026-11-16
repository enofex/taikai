package com.enofex.taikai.exports;

import com.enofex.taikai.ExportsConfigurer;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.exports.spi.TaikaiExport;
import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * Runs one of the registered exports alone.
 */
final class ExportChecks {

  private ExportChecks() {
  }

  /**
   * Runs the given export with the given configuration, excluding all other registered exports.
   */
  static void export(Taikai.Builder builder, Class<? extends TaikaiExport> export,
      Object configuration) {
    builder
        .discoverExports(only(export).andThen(exports -> exports.configure(export, configuration)))
        .build()
        .export();
  }

  /**
   * Excludes all registered exports but the given one.
   */
  static Consumer<ExportsConfigurer> only(Class<? extends TaikaiExport> export) {
    return exports -> ServiceLoader.load(TaikaiExport.class).stream()
        .map(ServiceLoader.Provider::type)
        .filter(type -> !type.equals(export))
        .forEach(type -> exports.exclude(type));
  }
}
