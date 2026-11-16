package com.enofex.taikai.exports.fixtures.api;

import com.enofex.taikai.exports.fixtures.domain.Order;

public interface OrderPort {

  Order find(String id);
}
