package com.lordnoisy.hoobabot;

import com.api.igdb.request.IGDBWrapper;
import com.api.igdb.request.TwitchAuthenticator;
import com.api.igdb.utils.TwitchToken;
import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import discord4j.core.spec.EmbedCreateSpec;

import java.time.Instant;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameGiveawayFollower {
    private final String TWITCH_CLIENT_ID;
    private final String TWITCH_CLIENT_SECRET;
    TwitchToken token;
    private final RSSReader rssReader = new RSSReader();
    private IGDBWrapper wrapper = IGDBWrapper.INSTANCE;

    /**
     * Constructor for GameGiveawayFollower
     * @param twitch_client_id the twitch client id
     * @param twitch_client_secret the twitch client secret
     */
    public GameGiveawayFollower(String twitch_client_id, String twitch_client_secret) {
        this.TWITCH_CLIENT_ID = twitch_client_id;
        this.TWITCH_CLIENT_SECRET = twitch_client_secret;

        TwitchAuthenticator tAuth = TwitchAuthenticator.INSTANCE;

        this.token = tAuth.requestTwitchToken(TWITCH_CLIENT_ID, TWITCH_CLIENT_SECRET);
        this.wrapper.setCredentials(TWITCH_CLIENT_ID, token.getAccess_token());
    }

    /**
     * Read the giveaway RSS feed and process the data
     * @return
     */
    public EmbedCreateSpec readGiveawaysFeed() {
        String returnValue = "";
        SyndFeed feed = null;
        try {
            feed = this.rssReader.readRssFeed("https://isthereanydeal.com/feeds/GB/giveaways.rss");
            SyndEntry entry = feed.getEntries().get(0);
            String originalDescription = entry.getDescription().getValue();
            System.out.println(originalDescription);
            Pattern pattern = Pattern.compile("(?:https\"?)(.*)(?=/(\")*>)", Pattern.CASE_INSENSITIVE);
            Pattern expirePattern = Pattern.compile("(?:expires?)(.*)(?=)|unknown expiry", Pattern.CASE_INSENSITIVE);
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
            entry.getDescription().setValue("START " + description + " END");
            returnValue = this.rssReader.outputEntries(feed);
            System.out.println("BEANS" + returnValue);
            return createGameFeedEntryEmbed(entry.getTitle(), links, expiryDate);
        } catch (Exception e) {
            return EmbedCreateSpec.builder()
                    .color(EmbedBuilder.getStandardColor())
                    .title("Error")
                    .description("There was an error posting this giveaway!")
                    .timestamp(Instant.now())
                    .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconURL() + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                    .build();
        }
    }

    /**
     * Get the game's title from the RSS title
     * @param title RSS title
     * @return game's title
     */
    public String getGameTitleFromRSSTitle(String title) {
        return title.split("-")[0].strip();
    }

    /**
     * Get the platform's name from the RSS title
     * @param title the RSS title
     * @return the platform title
     */
    public String getPlatformFromRSSFTitle(String title) {
        String[] values = title.split("on");
        return values[values.length-1].strip();
    }

    /**
     * Create an embed displaying the free game, it's price, description and link
     * @param title the RSS feed title
     * @param links the links from the RSS feed
     * @param expiryDate the expiry date of the offer
     * @return a finished embed displaying the deal
     */
    public EmbedCreateSpec createGameFeedEntryEmbed(String title, ArrayList<String> links, String expiryDate) {
        String gameTitle = getGameTitleFromRSSTitle(title);
        String platform = getPlatformFromRSSFTitle(title);
        EmbedCreateSpec gameFeedEntryEmbed = EmbedCreateSpec.builder()
                .color(EmbedBuilder.getStandardColor())
                .title(gameTitle + " on " + platform)
                .description("> (insert description here)" + "\n"
                        + "~~(price)~~ **Free**" + "\n"
                        + expiryDate.substring(0, 1).toUpperCase() + expiryDate.substring(1) + " \n"
                        + links.get(links.size()-1))
                .image("https://opengameart.org/sites/default/files/medievalblacksmithinteriors.jpg")
                .thumbnail("https://opengameart.org/sites/default/files/medievalblacksmithinteriors.jpg")
                .timestamp(Instant.now())
                .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconURL() + String.valueOf(Utilities.getRandomNumber(0,156)) + ".png"))
                .build();
        return gameFeedEntryEmbed;
    }

}
