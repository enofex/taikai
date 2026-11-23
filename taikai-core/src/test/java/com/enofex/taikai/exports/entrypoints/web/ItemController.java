package com.enofex.taikai.exports.entrypoints.web;

import com.enofex.taikai.exports.entrypoints.api.ItemsApi;
import com.enofex.taikai.exports.entrypoints.app.ItemService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ItemController extends BaseController implements ItemsApi {

  private final ItemService itemService;

  ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @Override
  public List<String> items() {
    return this.itemService.items();
  }

  @Override
  public void add(String item) {
    // adds the item
  }

  @GetMapping("/items/{index}")
  String item(int index) {
    return "";
  }

  @RequestMapping(path = "/items/search", method = {RequestMethod.GET, RequestMethod.POST})
  List<String> search(String query) {
    return List.of();
  }

  @RequestMapping("/items/export")
  String export() {
    return "";
  }

  @Remove
  void remove(int index) {
    // removes the item
  }
}
