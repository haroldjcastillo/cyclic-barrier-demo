package com.github.haroldjcastillo.cyclebarrier;

import static com.github.haroldjcastillo.cyclicbarrier.ParallelTask.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.haroldjcastillo.cyclicbarrier.ParallelTask;
import com.github.haroldjcastillo.cyclicbarrier.Task;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class ParallelTaskTest {

  private static final Logger LOGGER = Logger.getLogger(ParallelTaskTest.class.getName());

  private static final ExecutorService executor = Executors.newFixedThreadPool(20);

  static {
    // Gracefully shutdown
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  executor.shutdown();
                  try {
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                      executor.shutdownNow();
                      if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        LOGGER.log(Level.SEVERE, "Executor did not terminate");
                      }
                    }
                  } catch (InterruptedException ie) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                  }
                }));
  }

  @Test
  void shouldExecuteTest() {
    final var t1 =
        new Task(
            "t1",
            (input, sharedObject) -> {
              sharedObject.put(UPPER_CASE, input.toUpperCase());
            });
    final var t2 =
        new Task(
            "t2",
            (input, sharedObject) -> {
              sharedObject.put(LOWER_CASE, input.toLowerCase());
            });
    final var t3 =
        new Task(
            "t3",
            (input, sharedObject) -> {
              sharedObject.put(SIZE, String.valueOf(input.length()));
            });
    final var parallelTask = new ParallelTask(List.of(t1, t2, t3), executor);
    final var response = parallelTask.execute("Test");
    assertThat(response).isNotNull();
    assertThat(response.upperCase()).isEqualTo("TEST");
    assertThat(response.lowerCase()).isEqualTo("test");
    assertThat(response.size()).isEqualTo(4);
  }
}
