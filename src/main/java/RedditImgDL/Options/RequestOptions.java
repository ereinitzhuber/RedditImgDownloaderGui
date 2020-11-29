package RedditImgDL.Options;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class RequestOptions {

    private File saveDir;
    private boolean fullOutput;
    private String titleContainsRegex;
    private String titleDoesNotContainRegex;
    private String url;
    private int amount;
    private AtomicBoolean isJobRunning;


    public RequestOptions() {
        this.isJobRunning = new AtomicBoolean(false);
        this.fullOutput = false;
        this.saveDir = new File("");
        this.titleContainsRegex = "";
        this.titleDoesNotContainRegex = "";
        this.url = "";
        this.amount = 0;
    }

    public void setFullOutput(boolean fullOutput) {
        this.fullOutput = fullOutput;
    }

    public boolean getFullOutput() {
        return fullOutput;
    }

    public void setTitleDoesNotContainRegex(String titleDoesNotContainRegex) {
        this.titleDoesNotContainRegex = titleDoesNotContainRegex;
    }

    public String getTitleDoesNotContainRegex() {
        return titleDoesNotContainRegex;
    }

    public void setTitleContainsRegex(String titleContainsRegex) {
        this.titleContainsRegex = titleContainsRegex;
    }

    public String getTitleContainsRegex() {
        return titleContainsRegex;
    }

    public void setSaveDir(File saveDir) {
        this.saveDir = saveDir;
    }

    public File getSaveDir() {
        return saveDir;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isJobRunning() {

        return this.isJobRunning.get();
    }

    public void setJobRunning(boolean bool) {
        this.isJobRunning.getAndSet(bool);
    }


}
