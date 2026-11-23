package com.enofex.taikai.exports.entrypoints.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
abstract class BaseController {

  @GetMapping("/health")
  String health() {
    return "UP";
  }
}
