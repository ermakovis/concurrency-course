package course.concurrency.exams.auction;

import java.util.concurrent.Executors;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile boolean isStopped = false;
    private volatile Bid latestBid = new Bid(Long.MIN_VALUE, 0L, 0L);

    public boolean propose(Bid bid) {
        Bid oldBid = latestBid;

        if (isStopped || bid.price <= oldBid.price) {
            return false;
        }

        synchronized (this) {
            oldBid = latestBid;
            if (isStopped || bid.price <= oldBid.price) {
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

    public Bid stopAuction() {
        //Хочется эту строчку в синхронайзд поместить, но вроде нет нужды
        isStopped = true;
        return latestBid;
    }
}
