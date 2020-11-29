package RedditImgDL.DownloadThreads;

import RedditImgDL.Exceptions.URLParsingException;
import RedditImgDL.ProgressTrackingThreads.CompletedDownloadCounter;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Map;

public class TwitterDownloadThread  implements DLThread {
    String baseUrl;
    String path;
    File outputfile;
    Thread me;
    CompletedDownloadCounter c;

    public TwitterDownloadThread(String url, String path) {
        me = new Thread(this);
        baseUrl = url;
        this.path = path;
        this.c = new CompletedDownloadCounter();
    }

    public TwitterDownloadThread(String url, String path, CompletedDownloadCounter c) {
        me = new Thread(this);
        baseUrl = url;
        this.path = path;
        this.c = c;
    }


    @Override
    public void run() {
        String URL = "";
        try {
            URL = getLink();
        }
        catch  (URLParsingException ignored) {}
        String filename = URL.substring(URL.lastIndexOf("/") + 1, URL.lastIndexOf(":"));
        outputfile = new File(path, filename);
        boolean fileDownloadedFlag = false;
        if (outputfile.exists()) { fileDownloadedFlag = true; }
        int retries = 0;
        while (!fileDownloadedFlag){
            try {
                download(URL);
                fileDownloadedFlag = true;
            }
            catch (IOException e) {
                if (retries == 2) {
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

    public String getLink() throws URLParsingException {
        InputStream stream;
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder(new URI(baseUrl)).build();
            HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (resp.statusCode() == 301) {
                HttpHeaders headers = resp.headers();
                Map<String, List<String>> HeadersMap = headers.map();
                List<String> location = HeadersMap.get("Location");
                HttpRequest reqWithNewLoc = HttpRequest.newBuilder(new URI(location.get(0))).build();
                resp = client.send(reqWithNewLoc, HttpResponse.BodyHandlers.ofInputStream());
                if (resp.statusCode() != 200) {
                    throw new URLParsingException();
                }
            }
            else if (resp.statusCode() != 200) {
                resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
                if (resp.statusCode() != 200) {
                    throw new URLParsingException();
                }
            }
            stream = resp.body();
        }
        catch (IOException | URISyntaxException | InterruptedException e) {
            throw new URLParsingException(e);
        }
        BufferedReader read = new BufferedReader(new InputStreamReader(stream));
        String imageRawString = "";
        boolean stopflag = true;
        while (stopflag) {
            try {
                if ((imageRawString = read.readLine()).contains("og:image")) {
                    stopflag = false;
                }
            }
            catch (IOException e) {
                throw new URLParsingException(e);
            }
        }
        int startImagesIndex = imageRawString.indexOf("\"og:image\" content=\"") + 20;
        int stopImagesIndex = imageRawString.indexOf("\">", startImagesIndex);
        return imageRawString.substring(startImagesIndex, stopImagesIndex);
    }
}
