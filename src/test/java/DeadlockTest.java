import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class DeadlockTest {
    @Test
    void shouldLock() {
        var map = new ConcurrentHashMap<Integer, Integer>();

        var executorService = Executors.newCachedThreadPool();

        IntStream.range(0, 100_000)
                .forEach(i ->
                        executorService.submit(() ->
                                map.computeIfAbsent(i, v -> {
                                    sleep(1000);
                                    return map.put(i - 1, i + 1);
                                })
                        )
                );


    }

    void sleep(int millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
