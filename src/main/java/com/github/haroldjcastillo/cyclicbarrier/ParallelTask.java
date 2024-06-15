package com.github.haroldjcastillo.cyclicbarrier;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public record ParallelTask(List<Task> tasks, ExecutorService executor) {

  private static final Logger LOGGER = Logger.getLogger(ParallelTask.class.getName());
  public static final String UPPER_CASE = "upperCase";
  public static final String LOWER_CASE = "lowerCase";
  public static final String SIZE = "size";

  public ParallelTask {
    if (tasks == null) throw new IllegalArgumentException("tasks cannot be null");
    if (executor == null) throw new IllegalArgumentException("executor cannot be null");
  }

  public ParallelTaskResponse execute(String input) {
    final var latch = new CountDownLatch(1);
    final var sharedInstance = new ConcurrentHashMap<String, String>();
    final var response = new AtomicReference<ParallelTaskResponse>();
    final var cyclicBarrier =
        new CyclicBarrier(tasks.size(), barrierAction(response, sharedInstance, latch));
    tasks.forEach(
        task -> executor.submit(() -> task.execute(input, sharedInstance, cyclicBarrier)));
    try {
      latch.await();
      return response.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }

  private static Runnable barrierAction(
      AtomicReference<ParallelTaskResponse> response,
      ConcurrentHashMap<String, String> sharedInstance,
      CountDownLatch latch) {
    return () -> {
      LOGGER.log(Level.INFO, "Aggregating the response");
      response.set(
          new ParallelTaskResponse(
              sharedInstance.get(UPPER_CASE),
              sharedInstance.get(LOWER_CASE),
              Integer.parseInt(sharedInstance.get(SIZE))));
      latch.countDown();
    };
  }

  public record ParallelTaskResponse(String upperCase, String lowerCase, int size) {}
}
