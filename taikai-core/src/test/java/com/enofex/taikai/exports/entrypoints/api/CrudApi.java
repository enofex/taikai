package com.enofex.taikai.exports.entrypoints.api;

import org.springframework.web.bind.annotation.PutMapping;

public interface CrudApi<T> {

  @PutMapping
  T save(T entity);
}
