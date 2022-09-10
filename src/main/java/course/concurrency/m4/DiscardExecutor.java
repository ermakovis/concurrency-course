package course.concurrency.m4;

import java.util.concurrent.*;

public class DiscardExecutor extends ThreadPoolExecutor {

    public DiscardExecutor() {
        super(8, 8, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), new DiscardPolicy());
    }
}
