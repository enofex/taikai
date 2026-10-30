package com.enofex.taikai.metrics.shop.app;

import com.enofex.taikai.metrics.shop.api.OrderPort;
import com.enofex.taikai.metrics.shop.domain.Order;

public class OrderService {

  private final OrderPort orderPort;
  private final OrderMapper orderMapper;
  private final Cache cache;

  public OrderService(OrderPort orderPort, OrderMapper orderMapper) {
    this.orderPort = orderPort;
    this.orderMapper = orderMapper;
    this.cache = new Cache();
  }

  String describe(String id) {
    Order order = this.orderPort.find(id);
    this.cache.remember(order.id());
    return this.orderMapper.map(order);
  }

  static class Cache {

    void remember(String id) {
    }
  }
}
