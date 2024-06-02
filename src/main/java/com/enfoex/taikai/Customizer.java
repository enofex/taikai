package com.enfoex.taikai;

@FunctionalInterface
public interface Customizer<T> {

  void customize(T t);

}