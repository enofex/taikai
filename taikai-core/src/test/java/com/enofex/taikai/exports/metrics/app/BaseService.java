package com.enofex.taikai.exports.metrics.app;

import com.enofex.taikai.exports.metrics.domain.Order;

public abstract class BaseService {

  protected abstract Order load(String id);

  String name() {
    return "orders";
  }
}
