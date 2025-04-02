package com.lordnoisy.hoobabot;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class RSS {
    public String readRssFeed(){
        try {
            URL feedSource = new URL("https://lorem-rss.herokuapp.com/feed?length=1");
            String returnValue = "";
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedSource));
            for (SyndEntry entry : feed.getEntries()) {
                returnValue = returnValue + entry.getTitle() + "\n";
            }
            return returnValue;
        } catch (Exception e) {
            return "Error reading RSS feed";
        }
    }
}
