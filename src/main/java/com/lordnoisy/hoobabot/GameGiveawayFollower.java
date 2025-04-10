package com.lordnoisy.hoobabot;

import com.api.igdb.request.IGDBWrapper;
import com.api.igdb.request.TwitchAuthenticator;
import com.api.igdb.utils.Endpoints;
import com.api.igdb.utils.TwitchToken;
import com.lordnoisy.hoobabot.utility.DiscordUtilities;
import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.json.JSONObject;
import proto.Game;
import proto.GameResult;
import proto.Website;
import proto.WebsiteResult;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameGiveawayFollower {
    final private static String updateGiveawayChannel = "UPDATE servers SET giveaway_channel_id = ? WHERE server_id = ?";
    final private static String getAllGiveawayChannels = "SELECT giveaway_channel_id FROM servers WHERE giveaway_channel_id IS NOT NULL";
    final private static String deleteServer = "UPDATE servers SET giveaway_channel_id = NULL WHERE server_id = ?";

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

    /**
     * Set the frequency to check the RSS feed
     * @param timeInSeconds the time in seconds
     */
    private void setFrequency(long timeInSeconds) {
        this.frequency = timeInSeconds * 1000;
    }

    /**
     * Get the frequency to check the RSS feed
     * @return the frequency in milliseconds
     */
    public long getFrequency() {
        return this.frequency;
    }

    /**
     * Check for giveaways and return a Mono to send them
     * @param messageChannels the channels that follow giveaways
     * @return a Mono with the messages to send
     */
    public Mono<Void> checkForAndSendGiveaways(ArrayList<MessageChannel> messageChannels) {
        System.out.println("READING GIVEAWAYS FEED");
        ArrayList<EmbedCreateSpec> giveawayEmbeds = readGiveawaysFeed(5);
        ArrayList<EmbedCreateSpec> giveawayEmbedsToSend = new ArrayList<>();
        for (int i = 0; i < giveawayEmbeds.size(); i++) {
            EmbedCreateSpec giveawayEmbed = giveawayEmbeds.get(i);
            if (giveawayEmbed.title().get().replaceAll("\\s+","").equals(lastSentGiveaway)) {
                break;
            }
            giveawayEmbedsToSend.add(giveawayEmbed);
        }

        //Set lastSentGiveaway
        if (!giveawayEmbedsToSend.isEmpty()) {
            setLastSentGiveaway(giveawayEmbedsToSend.get(0).title().get());
        }

        Mono<Void> monoToReturn = Mono.empty();
        for (int i = giveawayEmbedsToSend.size() - 1; i >= 0; i--) {
            System.out.println("MESSAGE CHANNELS SIZE " + messageChannels.size());
            for (MessageChannel messageChannel : messageChannels) {
                System.out.println("SENDING GIVEAWAY MESSAGES TO " + messageChannel.getId().asString() + " TITLE: " + giveawayEmbeds.get(i).title().get());
                monoToReturn = monoToReturn.and(messageChannel.createMessage(giveawayEmbedsToSend.get(i)));
            }
        }
        return Mono.empty();
        //return monoToReturn;
    }

    /**
     * Store the last sent giveaway so that we can prevent the bot sending old giveaways
     * @param lastSentGiveaway the last sent giveaway
     */
    private void setLastSentGiveaway(String lastSentGiveaway) {
        this.lastSentGiveaway = lastSentGiveaway.replaceAll("\\s+","");
        properties.setProperty(Main.LAST_SENT_GIVEAWAY, this.lastSentGiveaway);
        try {
            properties.store(new FileOutputStream("monke.properties"), null);
        } catch (IOException e) {
            System.out.println("ERROR WRITING PROPERTIES");
            e.printStackTrace();
        }
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
                Date giveawayDate = entry.getPublishedDate();
                //Since they delete old entries from the RSS feed, also make sure not to post giveaways more than a day old.
                if (giveawayDate.before(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)))) {
                    System.out.println("Skipping: " + entry.getTitle());
                    continue;
                }

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
                } else {
                    //Display nothing if the expiry is unknown, it looks cleaner
                    expiryDate = "";
                }
                entry.getDescription().setValue("START " + description + " END");
                returnValue = this.rssReader.outputEntries(feed);
                System.out.println("BEANS" + returnValue);

                String entryTitle = entry.getTitle();
                String platform = getPlatformFromRSSFTitle(entryTitle);
                Game game = getGameDataFromIGDB(entryTitle);



                if (game != null) {
                    System.out.println("GIVEAWAY GAME " + game.getName());
                    String steamAppID = getSteamAppID(game.getId());
                    JSONObject steamData = getSteamData(steamAppID);

                    embedCreateSpecs.add(createGameFeedEntryEmbed(game, platform, links, expiryDate, steamData, steamAppID));
                }
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
        //Usually the RSS feed is formatted `Game Title - FREE on platform on store`
        //Sometimes, it is formatted `Game Title on Store`
        //The goal here is to deal with those annoying edge cases while avoiding catching games that have "on" in their name
        String[] values = titleFromRss.split(" on ");
        String tempString = "";
        for (int i = 0; i < values.length - 1; i++) {
            tempString += values[i] + " ";
        }
        return tempString.split("-")[0].strip();
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
            game = getMostSimilarGame(listOfGames, title);
        } catch (Exception e) {
            System.out.println("THE FOLLOWING GAME TITLE DOES NOT WORK: " + title);
            e.printStackTrace();
        }
        return game;
    }


    /**
     * Tests the similarity of the game title from RSS against a list of results from IGDB
     * @param listOfGames the IGDB list of games
     * @param title the title from the RSS feed
     * @return the game with the most similar title
     */
    private Game getMostSimilarGame(List<Game> listOfGames, String title) {
        int score = listOfGames.get(0).getName().compareTo(title);
        System.out.println(listOfGames.get(0).getName() + " COMPARED TO " + title + " = " + score);
        int bestScoreIndex = 0;

        //If strings are not exactly equal, check other results to see if there are more equal strings
        if (score != 0) {
            for (int i = 1; i < listOfGames.size(); i++) {

                int newScore = listOfGames.get(i).getName().compareTo(title);
                System.out.println(listOfGames.get(i).getName() + " COMPARED TO " + title + " = " + newScore);
                if (Math.abs(newScore) < score) {
                    System.out.println(Math.abs(newScore) + " Setting the index to: " + i);
                    bestScoreIndex = i;
                    score = newScore;
                }
            }
        }
        return listOfGames.get(bestScoreIndex);
    }

    /**
     * Get a game's Steam app ID from IGDB if available
     * @param gameID the IGDB game ID
     * @return the Steam app ID
     */
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
            e.printStackTrace();
        }
        return steamAppID;
    }

    /**
     * From an app ID, get the steam json data
     * @param steamAppID the steam app ID
     * @return the steam json data
     */
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

    /**
     * Get the image to put into the embed. If no header image can be obtained from steam, use the less appealing cover art from IGDB
     * @param steamData the steam JSON data
     * @param steamAppID the steam app ID
     * @param game the IGDB game
     * @return the image url
     */
    public String getEmbedImage(JSONObject steamData, String steamAppID, Game game) {
        String url = getSteamHeaderImage(steamData, steamAppID);
        if (url == null) {
            url = game.getCover().getImageId();
            url = "https://images.igdb.com/igdb/image/upload/t_cover_big/" + url + ".png";
        }
        return url;
    }

    /**
     * Get the steam header image
     * @param steamData the steam json data
     * @param steamAppID the steam app ID
     * @return
     */
    public String getSteamHeaderImage(JSONObject steamData, String steamAppID) {
        try {
            return steamData.getJSONObject(steamAppID).getJSONObject("data").getString("header_image");
        } catch (Exception e) {
            //Will just result in no image in the embed, no worries.
            return null;
        }
    }

    /**
     * Get the Steam Price of a game
     * @param steamData the steam json data
     * @param steamAppID the steam app ID
     * @return the Steam price, crossed out
     */
    public String getPrice(JSONObject steamData, String steamAppID) {
        try {
            return "~~" + steamData.getJSONObject(steamAppID).getJSONObject("data").getJSONObject("price_overview").getString("initial_formatted") + "~~";
        } catch (Exception e) {
            //Just display nothing if we don't know the price
            return "**Now**";
        }
    }

    public String getStoreLogo(String platform) {
        return switch (platform.toLowerCase()) {
            case "steam" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/Steam_icon_logo.svg/512px-Steam_icon_logo.svg.png";
            case "epic game store" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/3/31/Epic_Games_logo.svg/516px-Epic_Games_logo.svg.png";
            case "indiegala store" -> "https://company.indiegala.com/wp-content/uploads/2021/09/indiegala-logo-dark-back-rgb.png";
            case "fanatical" -> "https://d4.alternativeto.net/XWWMBsuvFy_AUeoUeMn0g3RQIvVty2KbWP2ytw0IWwQ/rs:fit:280:280:0/g:ce:0:0/exar:1/YWJzOi8vZGlzdC9pY29ucy9idW5kbGUtc3RhcnNfMjI5ODg3LnBuZw.png";
            case "gog" -> "https://static.wikia.nocookie.net/this-war-of-mine/images/1/1a/Logo_GoG.png/revision/latest/scale-to-width-down/220?cb=20160711062658";
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
        String rating = "";
        if (!((int) game.getTotalRating() == 0)) {
            rating = (int) game.getTotalRating() + "/100 \u2605";
        }
        EmbedCreateSpec gameFeedEntryEmbed = EmbedCreateSpec.builder()
                .color(EmbedBuilder.getStandardColor())
                .title(game.getName() + " on " + platform)
                .description("> " + game.getSummary().split("\n")[0] + "\n\n"
                        + getPrice(steamData, steamAppID) + " **Free** " + expiryDate + " \uFEFF \uFEFF \uFEFF \uFEFF \uFEFF " + rating + "\n\n"
                        + "[**Open in browser \u2197**](" + links.get(links.size()-1) + ")")
                .image(getEmbedImage(steamData, steamAppID, game))
                .thumbnail(getStoreLogo(platform))
                .timestamp(Instant.now())
                .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconURL() + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return gameFeedEntryEmbed;
    }

    /**
     * Add a channel to the database
     * @param connection the database connection
     * @param author the user who ran the command
     * @param serverSnowflake the server snowflake
     * @param channelSnowflake the channel snowflake
     * @param embeds the embed contructor
     * @return an embed of the result
     */
    public EmbedCreateSpec addChannelToDatabase(Connection connection, Mono<Member> author, Snowflake serverSnowflake, Snowflake channelSnowflake, EmbedBuilder embeds) {
        if(DiscordUtilities.validatePermissions(author)) {
            String serverID = serverSnowflake.asString();
            String channelID = channelSnowflake.asString();
            try {
                PreparedStatement finalQuery = connection.prepareStatement(updateGiveawayChannel);
                finalQuery.setString(1, channelID);
                finalQuery.setString(2, serverID);
                finalQuery.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return embeds.constructGiveawayChannelSetEmbed();
        } else {
            return embeds.constructInsufficientPermissionsEmbed();
        }
    }

    /**
     * Delete a server's giveaway channel from the database
     * @param connection the database connection
     * @param author the user who ran the command
     * @param serverSnowflake the server snowflake
     * @param embeds the embed contructor
     * @return an embed of the result
     */
    public EmbedCreateSpec deleteServerFromDatabase(Connection connection, Mono<Member> author, Snowflake serverSnowflake, EmbedBuilder embeds) {
        if(DiscordUtilities.validatePermissions(author)) {
            String serverID = serverSnowflake.asString();
            try {
                PreparedStatement finalQuery = connection.prepareStatement(deleteServer);
                finalQuery.setString(1, serverID);
                finalQuery.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return embeds.constructGiveawayChannelSetEmbed();
        } else {
            return embeds.constructInsufficientPermissionsEmbed();
        }
    }

    /**
     * Get all the servers from the database
     * @param connection the database connection
     * @return
     */
    public ArrayList<String> getChannelsFromDatabase(Connection connection) {
        ArrayList<String> channels = new ArrayList<>();
        try {
            PreparedStatement finalQuery = connection.prepareStatement(getAllGiveawayChannels);
            ResultSet resultSet = finalQuery.executeQuery();

            while(resultSet.next()) {
                channels.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channels;
    }

}
