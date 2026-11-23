package com.enofex.taikai.exports.entrypoints.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ReportController extends Commands implements Queries {

  @RequestMapping("/reports")
  String reports() {
    return "";
  }
}
