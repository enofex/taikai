package com.enofex.taikai;

import com.enfoex.taikai.Configurer;
import com.enfoex.taikai.Taikai;

class Client {

  public static void main(String[] args) {
    Taikai taikai = Taikai.builder()
        .failOnEmpty(true)
        .namespace("namespace")
        .spring(spring -> spring
            .configurations(configuration -> configuration
                .shouldHaveNameEndingConfiguration()
                .shouldHaveNameEndingConfiguration()
                .shouldHaveNameEndingConfiguration()
                .shouldHaveNameMatching("d"))
            .controllers(controllers -> controllers
                .shouldBePackagePrivate())
            .controllers(Configurer::disable))
   //     .spring(Configurer::disable)
        .build();

    System.out.printf(taikai.toString());
    taikai.rules().forEach(System.out::println);
  }
}