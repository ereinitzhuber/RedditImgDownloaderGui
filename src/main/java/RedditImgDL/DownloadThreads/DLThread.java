package RedditImgDL.DownloadThreads;

import java.io.IOException;

public interface DLThread extends Runnable {
    public void run();
    public void download(String url) throws IOException ;
}