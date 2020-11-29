package RedditImgDL.DownloadThreads;

import RedditImgDL.ProgressTrackingThreads.CompletedDownloadCounter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class MiscDownloadThread implements DLThread {
    String baseUrl;
    String path;
    String filename;
    File outputfile;
    Thread me;
    CompletedDownloadCounter c;

    public MiscDownloadThread(String url, String path) {
        me = new Thread(this);
        baseUrl = url;
        this.path = path;
        int startIndex = baseUrl.lastIndexOf("/");
        String name = baseUrl.substring(startIndex);
        filename = path + name;
        outputfile = new File(filename);
        this.c = new CompletedDownloadCounter();
    }

    public MiscDownloadThread(String url, String path, CompletedDownloadCounter c) {
        me = new Thread(this);
        baseUrl = url;
        this.path = path;
        int startIndex = baseUrl.lastIndexOf("/");
        String name = baseUrl.substring(startIndex);
        filename = path + name;
        outputfile = new File(filename);
        this.c = c;
    }

    @Override
    public void run() {
        boolean fileDownloadedFlag = false;
        if (outputfile.exists()) { fileDownloadedFlag = true; }
        int retries = 0;

        while (!fileDownloadedFlag){
            try {
                download(baseUrl);
                fileDownloadedFlag = true;
            }
            catch (IOException e) {
                if (retries == 5) {
                    outputfile.delete();
                    break;
                }
                retries++;
            }
        }
        c.increment();
    }

    @Override
    public void download(String myUrl) throws IOException {
        FileChannel fileChannel = null;
        ReadableByteChannel readableByteChannel = null;
        FileOutputStream fileOutputStream = null;

        try {
            URL urlObj = new URL(myUrl);
            readableByteChannel =
                    Channels.newChannel(urlObj.openStream());
            fileOutputStream = new FileOutputStream(outputfile);
            fileChannel = fileOutputStream.getChannel();
            fileOutputStream.getChannel().transferFrom(
                    readableByteChannel, 0, Long.MAX_VALUE);
        }
        finally {
            if (fileChannel != null) {
                fileChannel.close();
            }
            if (readableByteChannel != null) {
                readableByteChannel.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }
}
