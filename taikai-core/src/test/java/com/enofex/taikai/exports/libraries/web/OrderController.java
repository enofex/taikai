package com.enofex.taikai.exports.libraries.web;

import com.enofex.taikai.exports.libraries.domain.Order;
import jakarta.validation.Validator;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

  Validator validator;
  Order order;
}
