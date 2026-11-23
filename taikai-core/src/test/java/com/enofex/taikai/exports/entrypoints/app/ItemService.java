package com.enofex.taikai.exports.entrypoints.app;

import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

  public List<String> items() {
    return List.of();
  }

  @Scheduled(fixedRate = 60_000)
  void clear() {
    // clears the cached items
  }
}
