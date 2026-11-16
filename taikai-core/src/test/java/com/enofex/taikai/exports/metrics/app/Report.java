package com.enofex.taikai.exports.metrics.app;

import java.util.Optional;

/**
 * Two unrelated halves, one around the title and one around the count, so its LCOM4 is 2. The
 * getters {@link #getTitle()} and {@link #count()} do not count, and {@link #titleLength()} uses
 * the title only in a lambda.
 */
class Report {

  private final String title;
  private int count;

  Report(String title) {
    this.title = title;
  }

  String heading() {
    return "# " + this.title.strip();
  }

  int titleLength() {
    return Optional.of(this).map(report -> report.title.length()).orElse(0);
  }

  String getTitle() {
    return this.title;
  }

  void increment() {
    this.count++;
  }

  int count() {
    return this.count;
  }
}
