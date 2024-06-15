package com.github.haroldjcastillo.cyclicbarrier;

import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public record Task(String name, BiConsumer<String, Map<String, String>> action) {
  private static final Logger LOGGER = Logger.getLogger(Task.class.getName());

  public Task {
    if (name == null) throw new IllegalArgumentException("name cannot be null");
    if (action == null) throw new NullPointerException("action cannot be null");
  }

  public void execute(String input, Map<String, String> data, CyclicBarrier circularBarrier) {
    try {
      LOGGER.log(Level.INFO, "Executing task {0}", name);
      action.accept(input, data);
      circularBarrier.await();
    } catch (InterruptedException | BrokenBarrierException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }
}
