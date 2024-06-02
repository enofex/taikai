package com.enfoex.taikai;

@FunctionalInterface
public interface Customizer<T extends Configurer> {

  void customize(T t);

}