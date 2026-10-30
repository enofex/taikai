package com.enofex.taikai.metrics.shop.api;

import com.enofex.taikai.metrics.shop.domain.Order;

public interface OrderPort {

  Order find(String id);
}
