package com.enofex.taikai.exports.libraries.domain;

import jakarta.validation.constraints.NotBlank;

public class Order {

  @NotBlank
  String name;
}
