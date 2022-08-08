package course.concurrency.exams.auction;

public class AuctionPessimistic implements Auction {
    private final Notifier notifier;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private Bid latestBid = new Bid(Long.MIN_VALUE, 0L, 0L);

    public boolean propose(Bid bid) {
        Bid oldBid;

        synchronized (this) {
            oldBid = latestBid;
            if (bid.price <= oldBid.price) {
                return false;
            }
            latestBid = bid;
        }

        notifier.sendOutdatedMessage(oldBid);
        return true;
    }

    public Bid getLatestBid() {
        return latestBid;
    }
}
