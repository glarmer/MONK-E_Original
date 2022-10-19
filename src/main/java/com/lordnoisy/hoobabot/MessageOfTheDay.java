package com.lordnoisy.hoobabot;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import org.json.*;

public class MessageOfTheDay {
    private static ArrayList<String> quoteData = new ArrayList<>();

    public ArrayList<String> requestMessageOfTheDay() throws IOException {
        ArrayList<String> quoteData = new ArrayList<>();
        URL url = new URL("https://zenquotes.io/api/random");  // example url which return json data
        JSONTokener tokener = new JSONTokener(url.openStream());
        JSONArray array = new JSONArray(tokener);
        JSONObject data = array.getJSONObject(0);
        String quote = "*\"" + data.getString("q") + "\"*";
        String author = data.getString("a");

        quoteData.add(quote);
        quoteData.add(author);

        return quoteData;
    }

    public EmbedCreateSpec getMessageOfTheDay(EmbedBuilder embeds) {
        try {

            quoteData = requestMessageOfTheDay();

            EmbedCreateSpec fastQuote = embeds.constructFastQuoteEmbed(quoteData.get(0), quoteData.get(1));

            return fastQuote;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public MessageEditSpec getFinalMessageOfTheDay(EmbedBuilder embeds) {
        try {
            EmbedCreateSpec finalQuote = embeds.constructQuoteEmbed(quoteData.get(0), quoteData.get(1));

            MessageEditSpec edit = MessageEditSpec.create()
                    .withEmbeds(finalQuote);

            return edit;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }


}
