package com.enofex.taikai.configures;

@FunctionalInterface
public interface Customizer<T extends Configurer> {

  void customize(T t);

}