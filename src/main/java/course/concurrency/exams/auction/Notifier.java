package course.concurrency.exams.auction;

import java.util.concurrent.CompletableFuture;

public class Notifier {

    public CompletableFuture<Void> sendOutdatedMessage(Bid bid) {
        return CompletableFuture.supplyAsync(() -> {
            imitateSending();
            return null;
        });
    }

    private void imitateSending() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}
    }

    public void shutdown() {}
}
