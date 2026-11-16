package com.enofex.taikai.exports.fixtures.spi;

import com.enofex.taikai.exports.fixtures.domain.Order;
import javax.xml.namespace.QName;

public interface Plugin {

  void accept(Order order, QName name);
}
