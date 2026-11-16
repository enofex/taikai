package com.enofex.taikai.exports.fixtures.app;

import com.enofex.taikai.exports.fixtures.domain.Order;

public abstract class BaseService {

  protected abstract Order load(String id);

  String name() {
    return "orders";
  }
}
