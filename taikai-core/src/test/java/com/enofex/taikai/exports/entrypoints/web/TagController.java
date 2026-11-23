package com.enofex.taikai.exports.entrypoints.web;

import com.enofex.taikai.exports.entrypoints.api.CrudApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TagController implements CrudApi<String> {

  @Override
  @PutMapping("/tags")
  public String save(String tag) {
    return tag;
  }

  @RestController
  @RequestMapping(path = "/legacy", method = RequestMethod.POST)
  static class LegacyController {

    @GetMapping("/tags")
    String tags() {
      return "";
    }

    @RequestMapping("/run")
    void run() {
      // runs a legacy command
    }
  }
}
