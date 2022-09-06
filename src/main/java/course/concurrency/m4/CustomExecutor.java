package course.concurrency.m4;

import java.util.concurrent.*;

public class CustomExecutor extends ThreadPoolExecutor {
    public CustomExecutor() {
        super(8, 8, 0L, TimeUnit.MILLISECONDS, new CustomQueue(), new DiscardPolicy());
    }

    private static class CustomQueue extends LinkedBlockingDeque<Runnable> {
        @Override
        public boolean offer(Runnable runnable) {
            return super.offerFirst(runnable);
        }
    }
}
