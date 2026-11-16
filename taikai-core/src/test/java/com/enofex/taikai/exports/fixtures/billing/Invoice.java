package com.enofex.taikai.exports.fixtures.billing;

import com.enofex.taikai.exports.fixtures.payment.Payment;
import jakarta.validation.constraints.NotNull;

public class Invoice {

  @NotNull
  Payment payment;
}
