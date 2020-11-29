package RedditImgDL.Tasks;

import RedditImgDL.DownloadThreads.*;
import RedditImgDL.Exceptions.PushshiftConnectionException;
import RedditImgDL.Options.RequestOptions;
import RedditImgDL.ProgressTrackingThreads.CompletedDownloadCounter;
import RedditImgDL.Pushshift.Pushshift;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadAllTask extends Task<Boolean> {
    RequestOptions ro;

    public DownloadAllTask(RequestOptions ro) {

        this.ro = ro;
    }

    protected Boolean call()  {
        CompletedDownloadCounter counter = new CompletedDownloadCounter();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        ArrayList<String> URLS;
        List<DLThread> threadPool = new ArrayList<>();

        String subreddit = ro.getUrl();

        File out  = Paths.get(ro.getSaveDir().toString(), subreddit).toFile();
        String path = out.getPath();
        boolean isPathMade = out.mkdirs();

        Platform.runLater(() -> updateMessage("Job: Download all from " + subreddit + " started!\n"));

        if (isPathMade) {
            Platform.runLater(() -> updateMessage("Directory: " + out.toString() + " Created successfully!\n"));
        }
        else if (out.exists()) {
            Platform.runLater(() -> updateMessage("Directory " + out.toString() + " exists. Continuing.\n"));
        }
        else {
            Platform.runLater(() -> updateMessage("Cannot create Directory!\n"));
            updateProgress(0, 0);
            ro.setJobRunning(false);
           return false;
        }

        try {
            Platform.runLater(() -> updateMessage("Getting links...\n"));

            if (!ro.getTitleDoesNotContainRegex().equals("") || !ro.getTitleContainsRegex().equals("")) {
                URLS = (ArrayList<String>) Pushshift.getLinks(subreddit, path, ro.getFullOutput(), ro.getTitleDoesNotContainRegex(), ro.getTitleContainsRegex());
            }
            else {
                URLS = (ArrayList<String>) Pushshift.getLinks(subreddit, path, ro.getFullOutput());
            }
        }
        catch (PushshiftConnectionException e) {
            Platform.runLater(() -> updateMessage("Pushshift connection failed.\n"));
            updateProgress(0, 0);
            ro.setJobRunning(false);
            return false;
        }

        Platform.runLater(() -> updateMessage("Done searching for links! Starting download...\n"));
        counter.setTotal(URLS.size());
        updateProgress(counter.getCompleted(), counter.getTotal());

        for (String url : URLS) {
            if (url.contains("imgur.com")) {
                threadPool.add(new ImgurDownloadThread(url, path, counter));
            }
            else if (url.contains("i.redd.it")) {
                threadPool.add(new RedditDownloadThread(url, path, counter));
            }
            else if (url.contains("twitter.com")) {
                threadPool.add(new TwitterDownloadThread(url, path, counter));
            }
            else if (url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith(".gif")) {
                threadPool.add(new MiscDownloadThread(url, path, counter));
            }
        }

        for (DLThread thread : threadPool) {
            executorService.submit(thread);
        }
        executorService.shutdown();

        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(100);
                updateProgress(counter.getCompleted(), counter.getTotal());
            }
            catch (InterruptedException e) {
                System.out.println(e);
            }
        }

        Platform.runLater(() -> {
            updateMessage("Done!\n");
            updateProgress(1,1);
        });
        ro.setJobRunning(false);
        return true;
    }
}
