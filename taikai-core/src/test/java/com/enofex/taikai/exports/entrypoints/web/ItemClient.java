package com.enofex.taikai.exports.entrypoints.web;

import com.enofex.taikai.exports.entrypoints.api.ItemsApi;
import java.util.List;

class ItemClient implements ItemsApi {

  @Override
  public List<String> items() {
    return List.of();
  }

  @Override
  public void add(String item) {
    // sends the item
  }
}
