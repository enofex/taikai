package com.enofex.taikai.exports.libraries.domain;

import jakarta.validation.constraints.Positive;

public class LineItem {

  @Positive
  int quantity;
}
