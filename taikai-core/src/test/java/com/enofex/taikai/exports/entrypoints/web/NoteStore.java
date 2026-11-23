package com.enofex.taikai.exports.entrypoints.web;

import org.springframework.web.bind.annotation.PutMapping;

abstract class NoteStore {

  @PutMapping("/notes")
  public String save(String note) {
    return note;
  }
}
