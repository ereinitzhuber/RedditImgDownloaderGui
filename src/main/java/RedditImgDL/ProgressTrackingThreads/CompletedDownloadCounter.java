package RedditImgDL.ProgressTrackingThreads;

import java.util.concurrent.atomic.AtomicInteger;

public class CompletedDownloadCounter {
    private AtomicInteger completed;
    int total;

    public CompletedDownloadCounter() {
        this.completed = new AtomicInteger(0);
        this.total = 0;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void increment() {
        this.completed.getAndIncrement();
    }

    public int getTotal() {
        return this.total;
    }

    public int getCompleted() {
        return this.completed.get();
    }

}
