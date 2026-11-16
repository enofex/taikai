package com.enofex.taikai.metrics.fixtures.shop.app;

import com.enofex.taikai.metrics.fixtures.shop.domain.Order;

class OrderMapper {

  String map(Order order) {
    return order.id();
  }
}
