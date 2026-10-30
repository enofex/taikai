package com.enofex.taikai.exports.spi;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

public final class TestExports {

  private static final List<String> EXPORTED = new ArrayList<>();

  private TestExports() {
  }

  public static List<String> exported() {
    return List.copyOf(EXPORTED);
  }

  public static void reset() {
    EXPORTED.clear();
    ContextCapturing.lastContext = null;
  }

  public static final class First implements TaikaiExport {

    @Override
    public String description() {
      return "A first export";
    }

    @Override
    public void export(ExportContext context) {
      EXPORTED.add(description());
    }
  }

  public static final class Second implements TaikaiExport {

    @Override
    public String description() {
      return "A second export";
    }

    @Override
    public void export(ExportContext context) {
      EXPORTED.add(description());
    }
  }

  public static final class Throwing implements TaikaiExport {

    @Override
    public String description() {
      return "A throwing export";
    }

    @Override
    public void export(ExportContext context) {
      throw new IllegalStateException("broken");
    }
  }

  public static final class Asserting implements TaikaiExport {

    @Override
    public String description() {
      return "An asserting export";
    }

    @Override
    public void export(ExportContext context) {
      throw new AssertionError("broken");
    }
  }

  public static final class NotInstantiable implements TaikaiExport {

    public NotInstantiable() {
      throw new IllegalStateException("should not be instantiated");
    }

    @Override
    public String description() {
      return "An export that cannot be instantiated";
    }

    @Override
    public void export(ExportContext context) {
      EXPORTED.add(description());
    }
  }

  public record Settings(String value) {

  }

  public static final class SettingsRequiring implements TaikaiExport {

    @Override
    public String description() {
      return "An export that requires settings";
    }

    @Override
    public void export(ExportContext context) {
      EXPORTED.add(context.requireConfiguration(Settings.class).value());
    }
  }

  public static final class ContextCapturing implements TaikaiExport {

    private static ExportContext lastContext;

    public static ExportContext lastContext() {
      return requireNonNull(lastContext, "The context capturing export did not run");
    }

    @Override
    public String description() {
      return "A context capturing export";
    }

    @Override
    public void export(ExportContext context) {
      lastContext = context;
    }
  }
}
