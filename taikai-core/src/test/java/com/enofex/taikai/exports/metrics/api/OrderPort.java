package com.enofex.taikai.exports.metrics.api;

import com.enofex.taikai.exports.metrics.domain.Order;

public interface OrderPort {

  Order find(String id);
}
