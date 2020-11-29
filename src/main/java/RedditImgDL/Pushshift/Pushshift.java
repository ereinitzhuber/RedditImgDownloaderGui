package RedditImgDL.Pushshift;

import RedditImgDL.Exceptions.PushshiftConnectionException;
import RedditImgDL.PushshiftResponse.Data;
import RedditImgDL.PushshiftResponse.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

public class Pushshift {
    public static List<String> getLinks(String subreddit, String path, boolean saveJSON) throws PushshiftConnectionException {

        List<Data>  Responses = new ArrayList<>();
        List<String> urls = new ArrayList<>();

        boolean flag = true;
        long Timestamp = Instant.now().getEpochSecond();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        long lastTimestamp = Timestamp;
        String baseURL = "https://api.pushshift.io/reddit/search/submission/" +
                "?sort=desc&fields=url,domain,title,created_utc&size=500&is_self=false" +
                "&subreddit=" + subreddit;
        while (flag) {
            try {
                String url = baseURL + "&before=" + lastTimestamp;
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder(new URI(url)).build();
                HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());

                if (resp.statusCode() != 200) {
                    for (int i = 0; i < 5; i++) {
                        Thread.sleep(500);
                        resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
                        if (resp.statusCode() == 200) {
                            flag = false;
                            break;
                        }
                    }
                    if (resp.statusCode() != 200) throw new
                            PushshiftConnectionException("Connection failed. Status code" + resp.statusCode());
                }
                BufferedReader read = new BufferedReader(new InputStreamReader(resp.body()));
                Response pushshiftResponse = gson.fromJson(read, Response.class);
                read.close();
                if (pushshiftResponse.getData().size() == 0) {
                    flag = false;
                    continue;
                }
                Responses.addAll(pushshiftResponse.getData());
                lastTimestamp = Responses.get(Responses.size() - 1).getCreatedUtc();
            }
            catch (IOException | URISyntaxException | InterruptedException e) {
                throw new PushshiftConnectionException(e);
            }
        }
        for (Data data : Responses) {
            urls.add(data.getUrl());
        }
        if (urls.size() == 0) {
            throw new PushshiftConnectionException("Response empty... No links found.");
        }
        else {
            if (saveJSON) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + subreddit + "Responses.json"));
                    gson.toJson(Responses, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return urls;
    }


    public static List<String> getLinks(String subreddit, String path, boolean saveJSON, String notInTitle, String inTitle) throws PushshiftConnectionException {

        List<Data>  Responses = new ArrayList<>();
        List<String> urls = new ArrayList<>();

        boolean flag = true;
        long Timestamp = Instant.now().getEpochSecond();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        long lastTimestamp = Timestamp;
        String baseURL = "https://api.pushshift.io/reddit/search/submission/" +
                "?sort=desc&fields=url,domain,title,created_utc&size=500&is_self=false" +
                "&subreddit=" + subreddit;

        if (!notInTitle.equals("")) {
            baseURL += "&title:not='" + notInTitle + "'";
        }
        if (!inTitle.equals("")) {
            baseURL += "&title='" + inTitle + "'";
        }


        while (flag) {
            try {
                String url = baseURL + "&before=" + lastTimestamp;
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder(new URI(url)).build();
                HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());

                if (resp.statusCode() != 200) {
                    for (int i = 0; i < 5; i++) {
                        Thread.sleep(500);
                        resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
                        if (resp.statusCode() == 200) {
                            flag = false;
                            break;
                        }
                    }
                    if (resp.statusCode() != 200) throw new
                            PushshiftConnectionException("Connection failed. Status code" + resp.statusCode());
                }
                BufferedReader read = new BufferedReader(new InputStreamReader(resp.body()));
                Response pushshiftResponse = gson.fromJson(read, Response.class);
                read.close();
                if (pushshiftResponse.getData().size() == 0) {
                    flag = false;
                    continue;
                }
                Responses.addAll(pushshiftResponse.getData());
                lastTimestamp = Responses.get(Responses.size() - 1).getCreatedUtc();
            }
            catch (IOException | URISyntaxException | InterruptedException e) {
                throw new PushshiftConnectionException(e);
            }
        }
        for (Data data : Responses) {
            urls.add(data.getUrl());
        }
        if (urls.size() == 0) {
            throw new PushshiftConnectionException("Response empty... No links found.");
        }
        else {
            if (saveJSON) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + subreddit + "Responses.json"));
                    gson.toJson(Responses, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return urls;
    }
}