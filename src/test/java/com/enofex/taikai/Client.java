package com.enofex.taikai;

import static com.enfoex.taikai.Customizer.defaults;
import static com.enfoex.taikai.Customizer.disable;
import static com.enfoex.taikai.spring.SpringRules.configurations;
import static com.enfoex.taikai.spring.SpringRules.controllers;

import com.enfoex.taikai.Taikai;

class Client {

  public static void main(String[] args) {
    Taikai taikai = Taikai.builder()
        .spring(defaults())
        .spring(disable())
        .spring(spring -> spring.enable(
            controllers().shouldHaveNameEndingController(),
            controllers().shouldHaveNameMatching("regex"),
            controllers().shouldBeAnnotatedWithRestController(),
            controllers().shouldBeAnnotatedWithRestController("regex"),
            controllers().shouldBeAnnotatedWithController(),
            controllers().shouldBeAnnotatedWithController("regex"),
            controllers().shouldBePackagePrivate(),
            controllers().shouldNotDependOnOtherController(),

            configurations().shouldHaveNameEndingConfiguration(),
            configurations().shouldHaveNameMatching("regex")
        ))

        .build();

    taikai.toString();
  }
}