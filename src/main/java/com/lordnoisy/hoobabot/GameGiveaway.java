package com.lordnoisy.hoobabot;

import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import discord4j.core.spec.EmbedCreateSpec;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

/**
 * Class to represent Game Giveaways
 */
public class GameGiveaway {
    private String title;
    private String price;
    private String image;
    private String description;
    private String giveawayUrl;
    private String finalUrl;
    private String publishedDate;
    private String endDate;
    private String platform;
    private String id;
    private String rating;
    private boolean isRatingRecommendations;


    /**
     * The constructor
     * @param obj the JSON data from gamerpower API
     */
    public GameGiveaway(JSONObject obj) {
        this.setTitle(obj.getString("title"));
        this.setPrice(obj.getString("worth"));
        this.setImage(obj.getString("image"));
        this.setDescription(obj.getString("description"));
        this.setGiveawayUrl(obj.getString("open_giveaway_url"));
        this.setPublishedDate(obj.getString("published_date"));
        this.setEndDate(obj.getString("end_date"));
        this.setPlatform(obj.getString("platforms"));

        this.finalUrl = setFinalUrl(this.giveawayUrl);
        this.setId();

        this.setPriceAndRatingFromSteam();



        System.out.println("TESTING \n" + this);
    }

    /**
     * Create the embed that represents this giveaway
     * @return the embed
     */
    public EmbedCreateSpec createGameFeedEntryEmbed() {
        String openInString = "";
        String ratingString = "";

        if (!rating.equals("") & !isRatingRecommendations) {
            ratingString = "**" + rating + "**/100 \u2605";
        } else if (!rating.equals("")) {
            ratingString = "**" + rating + "** Recommendations";
        }

        if ( (platform.equalsIgnoreCase("steam") & this.getFinalUrl().contains("https://store.steampowered.com/api/appdetails?appids=")) || (platform.equalsIgnoreCase("epic game store") & this.getFinalUrl().contains("https://store.epicgames.com/en-US/p/")) ) {
            openInString = " \uFEFF \uFEFF \uFEFF \uFEFF \uFEFF " + "[**Open on " + platform + " \u2197**](" + createOpenInLink() + ")";
        }

        EmbedCreateSpec gameFeedEntryEmbed = EmbedCreateSpec.builder()
                .color(EmbedBuilder.getStandardColor())
                .title(this.getTitle())
                .description("> " + this.getDescription() + "\n\n"
                        + "~~" + this.getPrice() + "~~" + " **Free!**" + " \uFEFF \uFEFF \uFEFF \uFEFF \uFEFF " + ratingString + "\n\n"
                        + "[**Open in browser \u2197**](" + this.getGiveawayUrl() + ")" + openInString + "\n\n"
                        + "**Claim before:** " + this.getEndDate())
                .image(this.getImage())
                .thumbnail(this.getStoreLogo())
                .timestamp(Instant.now())
                .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconURL() + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return gameFeedEntryEmbed;
    }

    /**
     * Return the string contents of the giveaway
     * @return the giveaway as a string
     */
    @Override
    public String toString() {
        return "GameGiveaway{" +
                "title='" + title + '\'' +
                ", price='" + price + '\'' +
                ", thumbnail='" + image + '\'' +
                ", description='" + description + '\'' +
                ", giveawayUrl='" + giveawayUrl + '\'' +
                ", finalUrl='" + finalUrl + '\'' +
                ", publishedDate='" + publishedDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", platform='" + platform + '\'' +
                ", id='" + id + '\'' +
                ", rating=" + rating +
                '}';
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRating() {
        return rating;
    }

    /**
     * Set the price and Rating using data from the Steam Internal API
     */
    private void setPriceAndRatingFromSteam() {
        this.rating = "";
        this.isRatingRecommendations = false;
        if (platform.equalsIgnoreCase("steam") & getId() != null) {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://store.steampowered.com/api/appdetails?appids=" + this.getId() + "&cc=gb&l=en")).build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                JSONObject steamData = new JSONObject(response.body()).getJSONObject(this.getId()).getJSONObject("data");

                this.setPrice(steamData.getJSONObject("price_overview").getString("initial_formatted"));
                if (!steamData.isNull("metacritic")) {
                    this.setRating(steamData.getJSONObject("metacritic").getString("score"));
                } else if (!steamData.isNull("recommendations")) {
                    System.out.println("Rating Recommendations");
                    this.setRating(String.valueOf(steamData.getJSONObject("recommendations").getInt("total")));
                    this.isRatingRecommendations = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create an 'open in' link
     * @return the link
     */
    private String createOpenInLink() {
        if (this.getPlatform().equalsIgnoreCase("steam")) {
            return "https://glarmer.xyz/monke/giveaways/redirect.php?platform=" + this.getPlatform().toLowerCase() + "&id=" + this.getId();
        } else if (this.getPlatform().equalsIgnoreCase("epic game store")) {
            //e.g. https://store.epicgames.com/en-US/p/river-city-girls-e6f608
            return "https://glarmer.xyz/monke/giveaways/redirect.php?platform=" + this.getPlatform().toLowerCase().replaceAll(" ", "") + "&id=" + this.getId();
        }
        return null;
    }

    /**
     * Return the store logo
     * @return the store logo image
     */
    private String getStoreLogo() {
        return switch (this.getPlatform().toLowerCase()) {
            case "steam" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/Steam_icon_logo.svg/512px-Steam_icon_logo.svg.png";
            case "epic game store" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/3/31/Epic_Games_logo.svg/516px-Epic_Games_logo.svg.png";
            case "epic games store" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/3/31/Epic_Games_logo.svg/516px-Epic_Games_logo.svg.png";
            case "indiegala store" -> "https://company.indiegala.com/wp-content/uploads/2021/09/indiegala-logo-dark-back-rgb.png";
            case "fanatical" -> "https://d4.alternativeto.net/XWWMBsuvFy_AUeoUeMn0g3RQIvVty2KbWP2ytw0IWwQ/rs:fit:280:280:0/g:ce:0:0/exar:1/YWJzOi8vZGlzdC9pY29ucy9idW5kbGUtc3RhcnNfMjI5ODg3LnBuZw.png";
            case "gog" -> "https://static.wikia.nocookie.net/this-war-of-mine/images/1/1a/Logo_GoG.png/revision/latest/scale-to-width-down/220?cb=20160711062658";
            case "prime gaming" -> "https://m.media-amazon.com/images/G/01/sm/shared/166979982420469/social_image._CB409110150_.jpg";
            case "itch.io" -> "https://cdn2.steamgriddb.com/icon_thumb/8b33ab221257b074d1d967042ad1d9d0.png";
            default -> "";
        };
    }

    public String getId() {
        return id;
    }

    public void setId() {
        if (this.getPlatform().equalsIgnoreCase("steam")) {
            //E.g. https://store.steampowered.com/app/753660/AtmaSphere/ -> 753660
            this.id = this.getFinalUrl().replace("https://store.steampowered.com/app/", "").split("/")[0];
        } else if (this.getPlatform().equalsIgnoreCase("epic games store") || this.getPlatform().equalsIgnoreCase("epic game store")) {
            //E.g. https://store.epicgames.com/en-US/p/river-city-girls-e6f608 -> river-city-girls-e6f608
            String[] parts = this.getFinalUrl().split("/");
            this.id = parts[parts.length - 1];
        } else {
            this.id = null;
        }
    }

    public String getFinalUrl() {
        return finalUrl;
    }

    public String setFinalUrl(String originalUrl)
    {
        try {
            Connection.Response response = Jsoup.connect(originalUrl).execute();
            System.out.println(response.statusCode() + " : " + response.url());
            return response.url().toString();
        } catch (HttpStatusException e) {
            //If the site forbids us, we still want the URL
            if (e.getStatusCode() == HttpStatus.SC_FORBIDDEN) {
                return e.getUrl();
            }
            return this.getGiveawayUrl();
        } catch (IOException e) {
            return this.getGiveawayUrl();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGiveawayUrl() {
        return giveawayUrl;
    }

    public void setGiveawayUrl(String giveawayUrl) {
        this.giveawayUrl = giveawayUrl;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        //E.g. PC, Itch.io, DRM-Free
        if (platform.startsWith("PC, ")) {
            //-> Itch.io, DRM-Free
            this.platform = platform.replace("PC, ", "");
            if (this.platform.contains(", DRM-Free") & !this.platform.startsWith("DRM-Free")) {
                //-> Itch.io
                this.platform = platform.replace(", DRM-Free", "");
            }
        } else {
            this.platform = platform;
        }
    }


}
