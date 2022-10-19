package com.lordnoisy.hoobabot;

import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.MessageEditSpec;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class WebImageSearch {
    private static String imageSearchURL = "https://www.bing.com/images/search?q=";
    private static String imageGifSearchURL = "https://www.bing.com/images/search?filetype=gif&qft=+filterui:photo-animatedgif&form=IRFLTR&first=1&tsc=ImageHoverTitle&q=";
    private EmbedBuilder embeds = new EmbedBuilder(this);
    private String googleAPIKey;
    private String xRapidKey;
    private String bingAPIKey;

    public WebImageSearch (String googleAPIKey, String xRapidKey, String bingAPIKey) {
        this.googleAPIKey = googleAPIKey;
        this.xRapidKey = xRapidKey;
        this.bingAPIKey = bingAPIKey;
    }

    public String getImageURL(String searchQuery) {
        String image = null;
        for (int i = 0; i < 1; i++) {
            //Try getting image from Google, up to 100 a day
            try {
                image = getImageViaGoogleAPI(searchQuery);
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Try unofficial google api, up to 50 a day
            try {
                image = getImageViaUnofficialGoogleAPI(searchQuery);
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            //If google runs out try bing 1000 a month
            try {
                image = getImageViaBingAPI(searchQuery);
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            //If google + bing runs out, then do web scraper
            try {
                image = getImageViaWebScraper(searchQuery);
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (image == null) {
            image = "https://cdn.discordapp.com/attachments/945092365989867560/977220466089529375/unknown.png";
        }

        return image;
    }

    public String getImageViaGoogleAPI(String searchQuery) throws IOException {
        String image = null;
            String apiURL;
            if (searchQuery.contains("-g")) {
                searchQuery = searchQuery.replace("-g","");
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

    public MessageEditSpec getGoogleEditSpec(Message searchMessage) {
        String queryText = getSearchQuery(Utilities.getArray(searchMessage.getContent()));
        String image = null;
        try {
            image = getImageViaGoogleAPI(searchMessage.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MessageEditSpec edit;
        if (!queryText.equals("")) {
            if (image != null) {
                edit = MessageEditSpec.create()
                        .withEmbeds()
                        .withContentOrNull(image);
            } else {
                edit = MessageEditSpec.create()
                        .withEmbeds(embeds.constructNoImageFoundEmbed());
            }
        } else {
            edit = MessageEditSpec.create()
                    .withEmbeds(embeds.constructImageHelpEmbed());
        }
        return edit;
    }

    public MessageEditSpec getBingEditSpec(Message message) {
        String queryText = getSearchQuery(Utilities.getArray(message.getContent()));
        String image;
        MessageEditSpec edit;
        if (!queryText.equals("")) {
            try {
                image = getImageViaBingAPI(queryText);
            } catch (Exception e) {
                image = getImageViaWebScraper(queryText);
            }
            if (image != null) {
                edit = MessageEditSpec.create()
                        .withEmbeds()
                        .withContentOrNull(image);
            } else {
                edit = MessageEditSpec.create()
                        .withEmbeds(embeds.constructNoImageFoundEmbed());
            }
        } else {
            edit = MessageEditSpec.create()
                    .withEmbeds(embeds.constructImageHelpEmbed());
        }
        return edit;
    }

    public MessageEditSpec getImageEditSpec(Message message){
        String queryText = getSearchQuery(Utilities.getArray(message.getContent()));
        MessageEditSpec edit;
        if (!queryText.equals("")) {
            String author;
            String avatar;
            try {
                author = message.getAuthorAsMember().block().getDisplayName();
                avatar = message.getAuthorAsMember().block().getAvatarUrl();
            } catch (Exception e) {
                author = message.getAuthor().get().getUsername();
                avatar = message.getAuthor().get().getAvatarUrl();
            }
            String image = getImageURL(queryText);

            if (image != null) {
                edit = MessageEditSpec.create()
                        .withEmbeds(embeds.constructImageEmbed(image, author, avatar, queryText));
            } else {
                edit = MessageEditSpec.create()
                        .withEmbeds(embeds.constructNoImageFoundEmbed());
            }
        } else {
            edit = MessageEditSpec.create()
                    .withEmbeds(embeds.constructImageHelpEmbed());
        }
        return edit;
    }

    public String getSearchQuery(String[] messageWords) {
        List<String> messageText = Arrays.asList(messageWords);
        String queryText = messageText.subList(1, messageText.size()).toString();
        queryText = queryText.replace("[", "");
        queryText = queryText.replace("]", "");
        queryText = queryText.replace(",", "");
        return queryText;
    }

    public String getImageViaUnofficialGoogleAPI(String searchQuery) throws Exception {
        String image = null;
            if (searchQuery.contains("-g")) {
                throw new Exception("Invalid parameter");
            }
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

    public String getImageViaBingAPI(String searchQuery) throws IOException {
            URL url;
            if (searchQuery.contains("-g")) {
                searchQuery = searchQuery.replace("-g","");
                url = new URL("https://api.bing.microsoft.com/v7.0/images/search?safeSearch=Moderate&count=1&imageType=AnimatedGif&q="+ Utilities.replaceSpaces(searchQuery));
            } else {
                url = new URL("https://api.bing.microsoft.com/v7.0/images/search?safeSearch=Moderate&count=1&q="+ Utilities.replaceSpaces(searchQuery));
            }
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestProperty("Ocp-Apim-Subscription-Key", bingAPIKey);
            http.setRequestProperty("Content-Type", "application/json");

            // Receive JSON body
            InputStream stream = http.getInputStream();
            Scanner scanner = new Scanner(stream);
            String response = scanner.useDelimiter("\\A").next();


            http.disconnect();

            JSONTokener tokener = new JSONTokener(response);
            JSONObject returnedData = new JSONObject(tokener);
            JSONObject resultInfo = returnedData.getJSONArray("value").getJSONObject(0);
            String result = resultInfo.getString("contentUrl");

            return result;
    }

    public String getImageViaWebScraper(String searchQuery) {
        try {
            if (System.getProperty("os.name").startsWith("Windows")) {
                System.setProperty("webdriver.chrome.driver", "C:\\Users\\LegIt\\Desktop\\chromedriver.exe");
            } else {
                System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
            }
            WebDriver driver = new ChromeDriver();
            driver.manage().window().setSize(new Dimension(1280, 720));
            try {
                String imageURL;
                searchQuery = Utilities.replaceSpaces(searchQuery);
                if (searchQuery.contains("-g")) {
                    searchQuery = searchQuery.replace("-g","");
                    driver.get(imageGifSearchURL + Utilities.replaceSpaces(searchQuery));
                } else {
                    driver.get(imageSearchURL + Utilities.replaceSpaces(searchQuery));
                }
                //Find and click the small image
                List images = driver.findElements(new By.ByClassName("mimg"));
                WebElement firstImage = (WebElement) images.get(0);
                firstImage.click();

                //Get and prepare URL
                String currentURL = driver.getCurrentUrl();
                currentURL = currentURL.split("mediaurl=")[1];
                currentURL = currentURL.split("&")[0];

                //convert hex to ascii
                imageURL = Utilities.replaceHexInString(currentURL);

                //If not a source link then find original source
                if (imageURL.contains("th.bing.com")) {
                    imageURL = imageURL.split("riu=")[1].split("&")[0];
                    imageURL = Utilities.replaceHexInString(imageURL);
                }

                driver.close();
                return imageURL;
            } catch (Exception e){
                driver.close();
                return null;
            }
        } catch (Exception e){
            return null;
        }
    }
}
