package com.lordnoisy.hoobabot;

import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Scanner;


public class WebImageSearch {
    private static String imageSearchURL = "https://www.bing.com/images/search?q=";
    private static String imageGifSearchURL = "https://www.bing.com/images/search?filetype=gif&qft=+filterui:photo-animatedgif&form=IRFLTR&first=1&tsc=ImageHoverTitle&q=";
    private EmbedBuilder embeds = new EmbedBuilder(this);
    private String googleAPIKey;
    private String xRapidKey;
    private String braveAPIKey;

    public WebImageSearch (String googleAPIKey, String xRapidKey, String braveAPIKey) {
        this.googleAPIKey = googleAPIKey;
        this.xRapidKey = xRapidKey;
        this.braveAPIKey = braveAPIKey;
    }

    public EmbedCreateSpec doImageSearch(ChatInputInteractionEvent event, String search, String engine, boolean isGifSearch) {
        String image;
        try {
            if (engine.equals("google") || isGifSearch) {
                image = getImageURLGoogle(search, isGifSearch);
                System.out.println("google: " + search + image);
            } else {
                image = getImageUrlBrave(search);
                System.out.println("BRAVE: " + search + image);
            }
        } catch (Exception e) {
            image = "https://www.refaad.com/Assets/Images/noresultsfound.png";
        }
        if (image == null) {
            image = "https://www.refaad.com/Assets/Images/noresultsfound.png";
        }

        String author;
        String authorUrl;
        if (event.getInteraction().getMember().isPresent()) {
            author = event.getInteraction().getMember().get().getDisplayName();
            authorUrl = event.getInteraction().getMember().get().getAvatarUrl();
        } else {
            author = event.getInteraction().getUser().getUsername();
            authorUrl = event.getInteraction().getUser().getAvatarUrl();
        }

        return embeds.constructImageEmbed(image, author, authorUrl, search);
    }

    public String getImageUrlBrave(String searchQuery) throws IOException, InterruptedException {
        System.out.println("BRAVE 1");
        searchQuery = Utilities.replaceSpaces(searchQuery);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.search.brave.com/res/v1/images/search?q=" + searchQuery + "&count=1&safesearch=off"))
                .header("cookie", "search_api_csrftoken=.eJwFwVsSgiAAAMC7dIISFPvMTKIMkUZr-mEKncRHhClmp293cZ63lbAk3zzfGS_8vt0J99d61Lh4qU1ScTap2b_gR1inBoH8WKrpI8w9hIpK3ZDuhZhKg13koYT3yMIDkWvdhSv7lv6JDtqpg5FTEh2CDuMvKMolB8FWzQ97G33RT02V7ZN4M6BCtzieXYfRhtwLeHsyBS6dEdm1kcNJXuHiD_aUOpU.bdUGpWzglVAwu3HAX1YUnQ_LnS8")
                .header("Accept", "application/json")
                .header("X-Subscription-Token", this.braveAPIKey)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        JSONObject searchResults = new JSONObject(response.body());
        JSONArray searchResultArray = searchResults.getJSONArray("results");
        JSONObject result = searchResultArray.getJSONObject(0);
        return result.getJSONObject("properties").getString("url");
    }

    public String getImageURLGoogle(String searchQuery, boolean isGif) {
        String image = null;
        for (int i = 0; i < 1; i++) {
            //Try getting image from Google, up to 100 a day
            try {
                image = getImageViaGoogleAPI(searchQuery, isGif);
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Try unofficial google api, up to 50 a day - cant do gif
            try {
                image = getImageViaUnofficialGoogleAPI(searchQuery);
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return image;
    }

    public String getImageViaGoogleAPI(String searchQuery, boolean isGif) throws IOException {
        String image = null;
            String apiURL;
            if (isGif) {
                apiURL = "https://customsearch.googleapis.com/customsearch/v1?cx=c0de1e69422af4569&num=1&imgType=animated&fileType=gif&searchType=image&key=" + googleAPIKey + "&q=" + Utilities.replaceSpaces(searchQuery);
            } else {
                apiURL = "https://customsearch.googleapis.com/customsearch/v1?cx=c0de1e69422af4569&num=1&searchType=image&key=" + googleAPIKey + "&q=" + Utilities.replaceSpaces(searchQuery);
            }
            URL url = new URL(apiURL);  // example url which return json data
            JSONTokener tokener = new JSONTokener(url.openStream());
            JSONObject returnedData = new JSONObject(tokener);
            JSONObject result = returnedData.getJSONArray("items").getJSONObject(0);

            image = result.getString("link");
        return image;
    }

    public String getImageViaUnofficialGoogleAPI(String searchQuery) throws Exception {
        String image = null;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://google-image-search1.p.rapidapi.com/?max=1&keyword=" + Utilities.replaceSpaces(searchQuery)))
                    .header("x-rapidapi-host", "google-image-search1.p.rapidapi.com")
                    .header("x-rapidapi-key", xRapidKey)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JSONTokener tokener = new JSONTokener(response.body());
            JSONArray returnedData = new JSONArray(tokener);
            image = returnedData.getJSONObject(0).getJSONObject("image").getString("url");
        return image;
    }
}
