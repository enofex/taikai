package com.enofex.taikai;

import com.enfoex.taikai.Configurer;
import com.enfoex.taikai.Taikai;

class Client {

  public static void main(String[] args) {
    Taikai taikai = Taikai.builder()
        .namespace("com.company.project")
        .failOnEmpty(false)
        .spring(spring -> spring
            .configurations(configuration -> configuration
                .shouldHaveNameEndingConfiguration()
                .shouldHaveNameMatching("Action"))
            .controllers(controllers -> controllers
                .shouldBePackagePrivate())
            .services(Configurer::disable))
        .logging(Configurer::disable)
        .build();

    taikai.rules().forEach(System.out::println);
  }
}