package com.lordnoisy.hoobabot;

import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import discord4j.core.object.Embed;
import discord4j.core.spec.EmbedCreateSpec;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RSSReader {
    public SyndFeed readRssFeed(String URL) throws IOException, FeedException {
        URL feedSource = new URL(URL);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedSource));
        return feed;
    }

    public EmbedCreateSpec readGiveawaysFeed() {
        String returnValue = "";
        SyndFeed feed = null;
        try {
            feed = readRssFeed("https://isthereanydeal.com/feeds/GB/giveaways.rss");
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
            returnValue = outputEntries(feed);
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

    public EmbedCreateSpec createGameFeedEntryEmbed(String title, ArrayList<String> links, String expiryDate) {
        EmbedCreateSpec gameFeedEntryEmbed = EmbedCreateSpec.builder()
                .color(EmbedBuilder.getStandardColor())
                .title(title)
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


    public String testRSSReader(String URL) {
        try {
            return outputEntries(readRssFeed(URL));
        } catch (Exception e) {
            return "Error reading RSS";
        }
    }

    public String outputEntries(SyndFeed feed) {
        String returnValue = "";
        for (SyndEntry entry : feed.getEntries()) {
            returnValue = returnValue + entry.getTitle() + "\n";

            returnValue = returnValue + entry.getDescription().getValue() + "\n";

            returnValue = returnValue + entry.getLink() + "\n";

            returnValue = returnValue + "\n";
        }
        return returnValue;
    }
}
