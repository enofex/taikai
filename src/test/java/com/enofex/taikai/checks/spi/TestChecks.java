package com.enofex.taikai.checks.spi;

import static java.util.Objects.requireNonNull;

import java.util.List;

public final class TestChecks {

  private TestChecks() {
  }

  public static final class Passing implements TaikaiCheck {

    @Override
    public String description() {
      return "A passing check";
    }

    @Override
    public List<String> violations(CheckContext context) {
      return List.of();
    }
  }

  public static final class Failing implements TaikaiCheck {

    @Override
    public String description() {
      return "A failing check";
    }

    @Override
    public List<String> violations(CheckContext context) {
      return List.of("first violation", "second violation");
    }
  }

  public static final class AnotherFailing implements TaikaiCheck {

    @Override
    public String description() {
      return "Another failing check";
    }

    @Override
    public List<String> violations(CheckContext context) {
      return List.of("third violation");
    }
  }

  public static final class Throwing implements TaikaiCheck {

    @Override
    public String description() {
      return "A throwing check";
    }

    @Override
    public List<String> violations(CheckContext context) {
      throw new IllegalStateException("broken");
    }
  }

  public static final class AssertionFailing implements TaikaiCheck {

    @Override
    public String description() {
      return "A check with a failing assertion";
    }

    @Override
    public List<String> violations(CheckContext context) {
      throw new AssertionError("README.md expected");
    }
  }

  public static final class ClassMissing implements TaikaiCheck {

    @Override
    public String description() {
      return "A check with a missing class";
    }

    @Override
    public List<String> violations(CheckContext context) {
      throw new NoClassDefFoundError("com/example/Missing");
    }
  }

  public static final class NotInstantiable implements TaikaiCheck {

    public NotInstantiable() {
      throw new IllegalStateException("should not be instantiated");
    }

    @Override
    public String description() {
      return "A check that cannot be instantiated";
    }

    @Override
    public List<String> violations(CheckContext context) {
      return List.of();
    }
  }

  public static final class ClassesCounting implements TaikaiCheck {

    @Override
    public String description() {
      return "A check that needs classes";
    }

    @Override
    public List<String> violations(CheckContext context) {
      return context.classes().isEmpty() ? List.of("no classes") : List.of();
    }
  }

  public record Settings(String value) {

  }

  public static final class SettingsRequiring implements TaikaiCheck {

    @Override
    public String description() {
      return "A check that requires settings";
    }

    @Override
    public List<String> violations(CheckContext context) {
      String value = context.requireConfiguration(Settings.class).value();
      return value.equals("valid") ? List.of() : List.of("invalid setting " + value);
    }
  }

  public static final class ContextCapturing implements TaikaiCheck {

    private static CheckContext lastContext;

    public static CheckContext lastContext() {
      return requireNonNull(lastContext, "The context capturing check did not run");
    }

    public static void reset() {
      lastContext = null;
    }

    @Override
    public String description() {
      return "A context capturing check";
    }

    @Override
    public List<String> violations(CheckContext context) {
      lastContext = context;
      return List.of();
    }
  }
}
