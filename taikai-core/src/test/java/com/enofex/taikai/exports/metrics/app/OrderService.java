package com.enofex.taikai.exports.metrics.app;

import com.enofex.taikai.exports.metrics.api.OrderPort;
import com.enofex.taikai.exports.metrics.domain.Order;

public class OrderService extends BaseService {

  private final OrderPort orderPort;
  private final Report report;

  public OrderService(OrderPort orderPort) {
    this.orderPort = orderPort;
    this.report = new Report("Orders");
  }

  @Override
  protected Order load(String id) {
    return this.orderPort.find(id);
  }

  String describe(String id) {
    Order order = load(id);
    this.report.increment();
    return name() + " " + order.id();
  }
}
