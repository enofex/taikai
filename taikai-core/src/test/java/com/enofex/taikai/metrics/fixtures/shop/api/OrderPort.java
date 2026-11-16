package com.enofex.taikai.metrics.fixtures.shop.api;

import com.enofex.taikai.metrics.fixtures.shop.domain.Order;

public interface OrderPort {

  Order find(String id);
}
