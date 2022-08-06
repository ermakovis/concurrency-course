package course.concurrency.exams.auction;

public class Bid {

    Long id;
    Long participantId;
    Long price;

    public Bid(Long id, Long participantId, Long price) {
        this.id = id;
        this.participantId = participantId;
        this.price = price;
    }
}
