package com.enofex.taikai.metrics.shop.app;

import com.enofex.taikai.metrics.shop.domain.Order;

class OrderMapper {

  String map(Order order) {
    return order.id();
  }
}
