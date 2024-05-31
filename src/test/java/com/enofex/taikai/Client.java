package com.enofex.taikai;

import static com.enfoex.taikai.Customizer.defaults;
import static com.enfoex.taikai.Customizer.disable;

import com.enfoex.taikai.Taikai;
import com.enfoex.taikai.spring.SpringRules;

class Client {

  public static void main(String[] args) {
    Taikai taikai = Taikai.builder()
        .spring(defaults())
        .spring(disable())
        .spring(spring -> spring.enable(
            SpringRules.controllers().shouldBeAnnotatedWithRestController(),
            SpringRules.controllers().shouldBeAnnotatedWithRestController("regex"),
            SpringRules.controllers().shouldBeAnnotatedWithController(),
            SpringRules.controllers().shouldBeAnnotatedWithController("regex"),
            SpringRules.controllers().shouldBePackagePrivate(),
            SpringRules.controllers().shouldNotDependOnOtherController()
        ))

        .build();

    taikai.toString();
  }
}