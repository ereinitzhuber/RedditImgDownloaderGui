package RedditImgDL.DownloadThreads;

import RedditImgDL.Exceptions.URLParsingException;
import RedditImgDL.ProgressTrackingThreads.CompletedDownloadCounter;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImgurDownloadThread implements DLThread {
    String baseUrl;
    String basePath;
    Thread me;
    File outputFile;
    CompletedDownloadCounter c;


    public ImgurDownloadThread(String url, String path) {
        me = new Thread(this);
        baseUrl = url;
        this.basePath = path;
        this.c = new CompletedDownloadCounter();
    }

    public ImgurDownloadThread(String url, String path, CompletedDownloadCounter c) {
        me = new Thread(this);
        baseUrl = url;
        this.basePath = path;
        this.c = c;
    }


    public void run() {
        int parseResp = parseURL();
        if (parseResp == 1) {
            int startIndex = baseUrl.lastIndexOf("/");
            String name = baseUrl.substring(startIndex);
            String filename = basePath + name;
            outputFile = new File(filename);
            boolean fileDownloadedFlag = false;
            if (outputFile.exists()) {
                fileDownloadedFlag = true;
            }
            int retries = 0;
            while (!fileDownloadedFlag) {
                try {
                    download(baseUrl);
                    fileDownloadedFlag = true;
                } catch (IOException e) {
                    if (retries == 2) {
                        outputFile.delete();
                        break;
                    }
                    retries++;
                }
            }
        }
        if (parseResp == 4) {
            baseUrl = baseUrl + ".jpg";
            int startIndex = baseUrl.lastIndexOf("/");
            String name = baseUrl.substring(startIndex);
            String filename = basePath + name;
            outputFile = new File(filename);
            boolean fileDownloadedFlag = false;
            if (outputFile.exists()) {
                fileDownloadedFlag = true;
            }
            int retries = 0;
            while (!fileDownloadedFlag) {
                try {
                    download(baseUrl);
                    fileDownloadedFlag = true;
                } catch (IOException e) {
                    if (retries == 2) {
                        outputFile.delete();
                        break;
                    }
                    retries++;
                }
            }
        }
        if (parseResp == 2 || parseResp == 3 || parseResp == 5) {
            ArrayList<String> URLS;
            try {
                URLS = getLinks();
            } catch (URLParsingException e) {
                URLS = new ArrayList<>();
            }
            String title = baseUrl.substring(baseUrl.lastIndexOf("/"));
            Path filePath = Paths.get(basePath, title);
            String pathWithTitle = filePath.toString();
            if (URLS.size() == 1) {
                pathWithTitle = basePath;
            } else {
                try {
                    Files.createDirectories(filePath);
                } catch (IOException ignored) {
                }
            }
            for (String url : URLS) {
                int startIndex = url.lastIndexOf("/");
                String name = url.substring(startIndex);
                Path temppath = Paths.get(pathWithTitle, name);
                String filename = temppath.toString();
                outputFile = new File(filename);
                boolean fileDownloadedFlag = false;
                if (outputFile.exists()) {
                    fileDownloadedFlag = true;
                }
                int retries = 0;
                while (!fileDownloadedFlag) {
                    try {
                        download(url);
                        fileDownloadedFlag = true;
                    } catch (IOException e) {
                        if (retries == 2) {
                            outputFile.delete();
                            break;
                        }
                        retries++;
                    }
                }
            }
        }
        c.increment();
    }
    @Override
    public void download(String url) throws IOException {
        FileChannel fileChannel = null;
        ReadableByteChannel readableByteChannel = null;
        FileOutputStream fileOutputStream = null;

        try {
            URL urlObj = new URL(url);
            readableByteChannel =
                    Channels.newChannel(urlObj.openStream());
            fileOutputStream = new FileOutputStream(outputFile);
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

    public ArrayList<String> getLinks() throws URLParsingException {
        InputStream stream ;
        ArrayList<String> URLS = new ArrayList<>();
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
                if ((imageRawString = read.readLine()).contains("image               :")) {
                    stopflag = false;
                }
            }
            catch (IOException e) {
                throw new URLParsingException(e);
            }
        }
        int startImagesIndex = imageRawString.indexOf("\"album_images\":") + 15;
        int stopImagesIndex = imageRawString.indexOf("},\"adConfig\"", startImagesIndex) + 1;
        String imagesStr = imageRawString.substring(startImagesIndex, stopImagesIndex);

        Gson gson = new Gson();
        Images images = gson.fromJson(imagesStr, Images.class);
        if (images.getImages().isEmpty()) {
            throw new URLParsingException();
        }
        for (Image image : images.getImages()) {
            String hash = image.getHash();
            String ext = image.getExt();
            URLS.add("https://imgur.com/" + hash + ext);
        }
        return URLS;
    }

    public int parseURL() {
        Pattern pat;
        Matcher mat;
        if (baseUrl.contains("i.imgur.com")) {
            return 1;
        }
        pat = Pattern.compile("https*://imgur\\.com/[a-zA-Z1-9]+\\.[a-z]+");
        mat = pat.matcher(baseUrl);
        if (mat.matches()) {
            return 1;
        }
        pat = Pattern.compile("https*://imgur\\.com/a/[a-zA-Z1-9]+");
        mat = pat.matcher(baseUrl);
        if (mat.matches()) {
            return 2;
        }
        pat = Pattern.compile("https*://imgur\\.com/gallery/[a-zA-Z1-9]+");
        mat = pat.matcher(baseUrl);
        if (mat.matches()) {
            return 3;
        }
        pat = Pattern.compile("https*://imgur\\.com/[a-zA-Z1-9]+");
        mat = pat.matcher(baseUrl);
        if (mat.matches()) {
            return 4;
        }
        pat = Pattern.compile("https*://imgur\\.com/r/[a-zA-Z1-9]+/[a-zA-Z1-9]+");
        mat = pat.matcher(baseUrl);
        if (mat.matches()) {
            return 5;
        }
        return 99;
    }

    public class Images {
        @SerializedName("images")
        @Expose
        private List<Image> images = null;

        public List<Image> getImages() {
            return images;
        }
        public void setImages(List<Image> images) {
            this.images = images;
        }

    }
    public class Image {
        @SerializedName("hash")
        @Expose
        private String hash;
        @SerializedName("ext")
        @Expose
        private String ext;

        public String getHash() {
            return hash;
        }
        public String getExt() {
            return ext;
        }
    }
}