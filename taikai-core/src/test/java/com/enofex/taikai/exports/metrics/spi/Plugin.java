package com.enofex.taikai.exports.metrics.spi;

import com.enofex.taikai.exports.metrics.domain.Order;

public interface Plugin {

  void accept(Order order);
}
