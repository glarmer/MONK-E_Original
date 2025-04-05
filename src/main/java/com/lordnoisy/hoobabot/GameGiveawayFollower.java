package com.lordnoisy.hoobabot;

import com.api.igdb.request.IGDBWrapper;
import com.api.igdb.request.TwitchAuthenticator;
import com.api.igdb.utils.Endpoints;
import com.api.igdb.utils.TwitchToken;
import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import discord4j.core.spec.EmbedCreateSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import proto.Game;
import proto.GameResult;
import proto.Website;
import proto.WebsiteResult;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameGiveawayFollower {
    TwitchToken token;
    private final RSSReader rssReader = new RSSReader();
    private final IGDBWrapper wrapper = IGDBWrapper.INSTANCE;
    private final WebImageSearch webImageSearch;
    private long frequency;
    private String lastSentGiveaway;
    private Properties properties;

    /**
     * Constructor for GameGiveawayFollower
     * @param twitch_client_id the twitch client id
     * @param twitch_client_secret the twitch client secret
     */
    public GameGiveawayFollower(String twitch_client_id, String twitch_client_secret, WebImageSearch webImageSearch, String lastSentGiveaway, Properties properties) {

        TwitchAuthenticator tAuth = TwitchAuthenticator.INSTANCE;

        this.token = tAuth.requestTwitchToken(twitch_client_id, twitch_client_secret);
        System.out.println("Twitch token: " + this.token);
        this.wrapper.setCredentials(twitch_client_id, token.getAccess_token());
        this.webImageSearch = webImageSearch;
        this.lastSentGiveaway = lastSentGiveaway;
        this.properties = properties;
        setFrequency(3600);
    }

    private void setFrequency(long timeInSeconds) {
        this.frequency = timeInSeconds * 1000;
    }

    public long getFrequency() {
        return this.frequency;
    }

    public void checkForAndSendGiveaways() {
        ArrayList<EmbedCreateSpec> giveawayEmbeds = readGiveawaysFeed(5);
        ArrayList<EmbedCreateSpec> giveawayEmbedsToSend = new ArrayList<>();
        for (int i = 0; i < giveawayEmbeds.size(); i++) {
            EmbedCreateSpec giveawayEmbed = giveawayEmbeds.get(i);

            if (giveawayEmbed.title().equals(lastSentGiveaway)) {
                break;
            }
            giveawayEmbedsToSend.add(giveawayEmbed);
        }

        //Set lastSentGiveaway
        if (giveawayEmbedsToSend.size() > 0) {
            setLastSentGiveaway(giveawayEmbedsToSend.get(0).title().get());
        }

        //TODO: SEND EMBEDS FROM giveawayEmbedsToSend
    }

    private void setLastSentGiveaway(String lastSentGiveaway) {
        //TODO: Make this save to file
        this.lastSentGiveaway = lastSentGiveaway.replaceAll("\\s+","");
        properties.setProperty(Main.LAST_SENT_GIVEAWAY, this.lastSentGiveaway);
    }

    /**
     * Read the giveaway RSS feed and process the data
     * @return
     */
    public ArrayList<EmbedCreateSpec> readGiveawaysFeed(int numberOfResults) {
        String returnValue = "";
        SyndFeed feed = null;
        ArrayList<EmbedCreateSpec> embedCreateSpecs = new ArrayList<>();
        try {
            feed = this.rssReader.readRssFeed("https://isthereanydeal.com/feeds/GB/giveaways.rss");
            for (int i = 0; i < numberOfResults; i++) {
                SyndEntry entry = feed.getEntries().get(i);
                String originalDescription = entry.getDescription().getValue();
                System.out.println(originalDescription);
                Pattern pattern = Pattern.compile("(?:https\"?)(.*)(?=/(\")*>)", Pattern.CASE_INSENSITIVE);
                Pattern expirePattern = Pattern.compile("(?:expires?)(.*)(?=\\s\\d)|unknown expiry", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(originalDescription);
                String description = "";
                ArrayList<String> links = new ArrayList<String>();
                while (matcher.find()) {
                    links.add(matcher.group());
                }
                matcher = expirePattern.matcher(originalDescription);
                String expiryDate = "";
                while (matcher.find()) {
                    expiryDate = matcher.group();
                }
                if (!Objects.equals(expiryDate, "unknown expiry")) {
                    String[] expiryDateComponents = expiryDate.split(" ");
                    expiryDate = "until " + expiryDateComponents[2] + " " + expiryDateComponents[3] + " " + expiryDateComponents[4];
                }
                entry.getDescription().setValue("START " + description + " END");
                returnValue = this.rssReader.outputEntries(feed);
                System.out.println("BEANS" + returnValue);

                String entryTitle = entry.getTitle();
                String platform = getPlatformFromRSSFTitle(entryTitle);
                Game game = getGameDataFromIGDB(entryTitle);

                String steamAppID = getSteamAppID(game.getId());
                JSONObject steamData = getSteamData(steamAppID);

                embedCreateSpecs.add(createGameFeedEntryEmbed(game, platform, links, expiryDate, steamData, steamAppID));
            }
        } catch (Exception e) {
            embedCreateSpecs.add(EmbedCreateSpec.builder()
                    .color(EmbedBuilder.getStandardColor())
                    .title("Error")
                    .description("There was an error posting this giveaway!")
                    .timestamp(Instant.now())
                    .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconURL() + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                    .build());
        }
        return embedCreateSpecs;
    }

    /**
     * Get the game's title from the RSS title
     * @param titleFromRss RSS title
     * @return game's title
     */
    public String getGameTitleFromRSSTitle(String titleFromRss) {
        return titleFromRss.split("-")[0].strip();
    }

    /**
     * Get the platform's name from the RSS title
     * @param titleFromRss the RSS title
     * @return the platform title
     */
    public String getPlatformFromRSSFTitle(String titleFromRss) {
        String[] values = titleFromRss.split("on");
        return values[values.length-1].strip();
    }

    /**
     * Get the data of a game from IGDB API
     * @param titleFromRss the title received from the RSS feed entry
     * @return the game data
     */
    public Game getGameDataFromIGDB(String titleFromRss) {
        Game game = null;
        String title = getGameTitleFromRSSTitle(titleFromRss);
        try {
            String search = "search \"" + title + "\"; fields name, summary, artworks, cover.image_id, websites, url, total_rating;\n";
            byte[] bytes = wrapper.apiProtoRequest(Endpoints.GAMES, search);
            List<Game> listOfGames = GameResult.parseFrom(bytes).getGamesList();
            game = listOfGames.get(0);
        } catch (Exception e) {
            System.out.println(e);
        }
        return game;
    }

    public String getSteamAppID(long gameID) {
        //For some reason game.getWebsitesList().getUrl() returns nothing, so do the search directly instead.
        String steamAppID = null;
        try {
            //Category 13 = Steam
            String search = "fields *; where game='" + gameID + "' & category='13';";
            byte[] bytes = wrapper.apiProtoRequest(Endpoints.WEBSITES, search);
            List<Website> listOfWebsites = WebsiteResult.parseFrom(bytes).getWebsitesList();
            String website = listOfWebsites.get(0).getUrl();
            String[] values = website.split("/");
            steamAppID = values[values.length-1];
        } catch (Exception e) {
            System.out.println(e);
        }
        return steamAppID;
    }

    public JSONObject getSteamData(String steamAppID) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://store.steampowered.com/api/appdetails?appids=" + steamAppID)).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new JSONObject(response.body());
        } catch (Exception e) {
            return null;
        }
    }

    public String getSteamHeaderImage(JSONObject steamData, String steamAppID) {
        try {
            return steamData.getJSONObject(steamAppID).getJSONObject("data").getString("header_image");
        } catch (Exception e) {
            //Will just result in no image in the embed, no worries.
            return "";
        }
    }

    public String getPrice(JSONObject steamData, String steamAppID) {
        try {
            return steamData.getJSONObject(steamAppID).getJSONObject("data").getJSONObject("price_overview").getString("initial_formatted");
        } catch (Exception e) {
            return "Unknown Price";
        }
    }

    public String getStoreLogo(String platform) {
        return switch (platform.toLowerCase()) {
            case "steam" -> "https://store.fastly.steamstatic.com/public/shared/images/responsive/header_logo.png";
            case "epic game store" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/5/57/Epic_games_store_logo.svg/500px-Epic_games_store_logo.svg.png";
            case "indiegala store" -> "https://company.indiegala.com/wp-content/uploads/2021/09/indiegala-logo-dark-back-rgb.png";
            case "fanatical" -> "https://d4.alternativeto.net/XWWMBsuvFy_AUeoUeMn0g3RQIvVty2KbWP2ytw0IWwQ/rs:fit:280:280:0/g:ce:0:0/exar:1/YWJzOi8vZGlzdC9pY29ucy9idW5kbGUtc3RhcnNfMjI5ODg3LnBuZw.png";
            case "gog" -> "https://prowly-prod.s3.eu-west-1.amazonaws.com/uploads/23079/assets/466741/original-dc010ef024e818e082ca8e2ff6f22b98.png";
            case "prime gaming" -> "https://m.media-amazon.com/images/G/01/sm/shared/166979982420469/social_image._CB409110150_.jpg";
            case "itch.io" -> "https://cdn2.steamgriddb.com/icon_thumb/8b33ab221257b074d1d967042ad1d9d0.png";
            default -> webImageSearch.getImageURLGoogle(platform + "+store+logo", false);
        };
    }

    /**
     * Create an embed displaying the free game, it's price, description and link
     * @param game the game data from IGDB
     * @param links the links from the RSS feed
     * @param expiryDate the expiry date of the offer
     * @param platform the platform of the offer (e.g. Steam, Epic, etc.)
     * @return a finished embed displaying the deal
     */
    public EmbedCreateSpec createGameFeedEntryEmbed(Game game, String platform, ArrayList<String> links, String expiryDate, JSONObject steamData, String steamAppID) {
        EmbedCreateSpec gameFeedEntryEmbed = EmbedCreateSpec.builder()
                .color(EmbedBuilder.getStandardColor())
                .title(game.getName() + " on " + platform)
                .description("> " + game.getSummary() + "\n\n"
                        + "~~" + getPrice(steamData, steamAppID) + "~~ **Free** " + expiryDate + " \uFEFF \uFEFF \uFEFF \uFEFF \uFEFF " + (int) game.getTotalRating() + "/100 \u2605\n\n"
                        + "[**Open in browser \u2197**](" + links.get(links.size()-1) + ")")
                .image(getSteamHeaderImage(steamData, steamAppID))
                .thumbnail(getStoreLogo(platform))
                .timestamp(Instant.now())
                .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconURL() + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return gameFeedEntryEmbed;
    }

}
