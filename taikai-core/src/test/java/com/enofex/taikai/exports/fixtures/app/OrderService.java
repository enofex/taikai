package com.enofex.taikai.exports.fixtures.app;

import com.enofex.taikai.exports.fixtures.api.OrderPort;
import com.enofex.taikai.exports.fixtures.domain.Order;
import org.springframework.stereotype.Service;

@Service
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
