package course.concurrency.exams.auction;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private boolean isStopped = false;
    private Bid latestBid = new Bid(Long.MIN_VALUE, 0L, 0L);

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
