package com.enofex.taikai.synthetic.web;

import com.enofex.taikai.synthetic.model.Signal;
import java.util.logging.Logger;
import org.springframework.web.bind.annotation.RestController;

/**
 * Compiles to the synthetic class {@code TrafficController$1}, which javac generates for the
 * {@code switch} over an enum, next to the synthetic {@code package-info} class of its package.
 */
@RestController
final class TrafficController implements Runnable {

  private static final Logger LOGGER = Logger.getLogger(TrafficController.class.getName());

  int code(Signal signal) {
    switch (signal) {
      case RED:
        return 1;
      default:
        return 0;
    }
  }

  @Override
  public void run() {
    LOGGER.info("run");
  }
}
