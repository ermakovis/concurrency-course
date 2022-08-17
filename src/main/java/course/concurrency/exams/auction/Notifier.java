package course.concurrency.exams.auction;

import java.util.concurrent.*;

public class Notifier {
    private final static ExecutorService EXECUTOR_SERVICE =
//            Executors.newCachedThreadPool();
//            Executors.newSingleThreadExecutor();
//            Executors.newFixedThreadPool(10);
            Executors.newFixedThreadPool(100);
//              ForkJoinPool.commonPool();
//            new ThreadPoolExecutor(
//                    Runtime.getRuntime().availableProcessors() / 2,
//                    Runtime.getRuntime().availableProcessors() * 10,
//                    10, TimeUnit.SECONDS,
//                    new LinkedBlockingQueue<>());

    public void sendOutdatedMessage(Bid bid) {
        EXECUTOR_SERVICE.submit(this::imitateSending);
    }

    private void imitateSending() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}
    }

    public void shutdown() {

    }
}
