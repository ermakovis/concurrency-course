package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {
    private final Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicReference<Bid> latestBid = new AtomicReference<>(new Bid(Long.MIN_VALUE, 0L, 0L));

    public boolean propose(Bid bid) {
        Bid oldBid;

        do {
            oldBid = latestBid.get();
            if (bid.price <= oldBid.price) {
                return false;
            }
        } while (latestBid.compareAndSet(oldBid, bid));

        notifier.sendOutdatedMessage(oldBid);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
