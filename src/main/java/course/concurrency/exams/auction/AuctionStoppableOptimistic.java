package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicMarkableReference<Bid> latestBid =
            new AtomicMarkableReference<>(new Bid(Long.MIN_VALUE, 0L, 0L), false);

    public boolean propose(Bid bid) {
        Bid oldBid;

        do {
            oldBid = latestBid.getReference();
            if (latestBid.isMarked() || bid.price <= oldBid.price) {
                return false;
            }
        } while (!latestBid.compareAndSet(oldBid, bid, false, false));

        notifier.sendOutdatedMessage(oldBid);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.getReference();
    }

    public Bid stopAuction() {
        Bid oldBid;

        do {
            oldBid = latestBid.getReference();
            if (latestBid.isMarked()) {
                return oldBid;
            }
        } while (!latestBid.compareAndSet(oldBid, oldBid, false, true));

        return oldBid;
    }
}
