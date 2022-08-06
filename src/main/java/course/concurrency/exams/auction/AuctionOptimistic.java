package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {
    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicReference<Bid> latestBid = new AtomicReference<>();

    public boolean propose(Bid bid) {
        Bid oldBid = latestBid.get();
        if (oldBid == null) {
            if (!latestBid.compareAndSet(null, bid)) {
                return propose(bid);
            }
            return true;
        }

        if (bid.price > oldBid.price) {
            if (!latestBid.compareAndSet(oldBid, bid)) {
                return propose(bid);
            }
            notifier.sendOutdatedMessage(oldBid);
            return true;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
