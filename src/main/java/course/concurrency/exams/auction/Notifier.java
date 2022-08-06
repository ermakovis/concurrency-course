package course.concurrency.exams.auction;

public class Notifier {

    public void sendOutdatedMessage(Bid bid) {
        imitateSending();
        System.out.printf("Participant %d bid %d is outdated%n", bid.participantId, bid.id);
    }

    private void imitateSending() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}
    }

    public void shutdown() {}
}
