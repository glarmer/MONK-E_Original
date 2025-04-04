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

    /**
     *
     * @param URL
     * @return
     * @throws IOException
     * @throws FeedException
     */
    public SyndFeed readRssFeed(String URL) throws IOException, FeedException {
        URL feedSource = new URL(URL);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedSource));
        return feed;
    }


    /**
     *
     * @param URL
     * @return
     */
    public String testRSSReader(String URL) {
        try {
            return outputEntries(readRssFeed(URL));
        } catch (Exception e) {
            return "Error reading RSS";
        }
    }

    /**
     * 
     * @param feed
     * @return
     */
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
