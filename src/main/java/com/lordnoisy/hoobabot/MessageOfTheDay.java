package com.lordnoisy.hoobabot;

import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.ArrayList;

import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import org.json.*;

public class MessageOfTheDay {

    public ArrayList<String> requestMessageOfTheDay() throws IOException {
        ArrayList<String> quoteData = new ArrayList<>();
        URL url = new URL("https://zenquotes.io/api/random");
        JSONTokener tokener = new JSONTokener(url.openStream());
        JSONArray array = new JSONArray(tokener);
        JSONObject data = array.getJSONObject(0);
        String quote = "*\"" + data.getString("q") + "\"*";
        String author = data.getString("a");

        quoteData.add(quote);
        quoteData.add(author);

        return quoteData;
    }

    public EmbedCreateSpec getMessageOfTheDay() {
        try {
            ArrayList<String> quoteData = requestMessageOfTheDay();
            String quote = quoteData.get(0);
            String author = quoteData.get(1);

            //TODO: Make it post an imageless version then edit with a web search image of the author.
            String imageURL = "";

            return EmbedCreateSpec.builder()
                    .color(EmbedBuilder.getStandardColor())
                    .title(author)
                    .description(quote)
                    .thumbnail(imageURL)
                    .timestamp(Instant.now())
                    .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconUrl() + Utilities.getRandomNumber(0,156) + ".png"))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EmbedBuilder.constructErrorEmbed();
    }


}
