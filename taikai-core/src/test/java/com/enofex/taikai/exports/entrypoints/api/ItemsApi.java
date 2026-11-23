package com.enofex.taikai.exports.entrypoints.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

public interface ItemsApi {

  @GetMapping("/items")
  List<String> items();

  @PostMapping("/items")
  void add(String item);
}
