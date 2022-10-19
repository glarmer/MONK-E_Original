package com.lordnoisy.hoobabot;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageEditSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lucky {
    private EmbedBuilder embeds;
    private String host = "contextualwebsearch-websearch-v1.p.rapidapi.com";
    private String key;
    private URLShortener shortener;
    private WebImageSearch webImageSearch;

    public Lucky(EmbedBuilder embeds, String key, URLShortener shortener, WebImageSearch webImageSearch){
        this.embeds = embeds;
        this.key = key;
        this.shortener = shortener;
        this.webImageSearch = webImageSearch;
    }

    public EmbedCreateSpec luckySearch(MessageCreateEvent event){
        String message = event.getMessage().getContent();
        String query = "";
        try {
            if (!message.contains("\"")) {
                message = message.replaceAll("[^\\d]", " ");
                message = message.trim();
                message = message.replaceAll(" +", " ");

                if (message.equals("")) {
                    message = String.valueOf(RandomNumberGen.getRandomNumber(1,10));
                }
                Scanner scanner = new Scanner(message);

                String numberOfWordsStr = scanner.nextLine();
                if(numberOfWordsStr.length() > 2){
                    numberOfWordsStr = "" + numberOfWordsStr.charAt(0) + numberOfWordsStr.charAt(1);
                }
                int numberOfWords = Integer.parseInt(numberOfWordsStr);
                if (numberOfWords > 20) {
                    numberOfWords = 20;
                }
                query = StringUtilities.getRandomWord(numberOfWords);
            } else {
                query = getMatchesFromString(message);
                query = StringUtilities.replaceHexInString(query);
                query = query.replaceAll(" ","+");

            }
            int numberOfResults = 10;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://contextualwebsearch-websearch-v1.p.rapidapi.com/api/Search/WebSearchAPI?q="+query+"&pageNumber=1&pageSize="+String.valueOf(numberOfResults)+"&autoCorrect=false&safeSearch=false"))
                    .header("X-RapidAPI-Host", host)
                    .header("X-RapidAPI-Key", key)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject results = new JSONObject(response.body());

            String website;
            try {
                JSONArray arrayResults = results.getJSONArray("value");
                website = arrayResults.getJSONObject(RandomNumberGen.getRandomNumber(0,numberOfResults-1)).getString("url");
                website = shortener.shortenURL(website);
            } catch (JSONException noWeb) {
                website = "No results were found.";
            }



            String image = "";
            if (event.getMessage().getContent().contains("-i")){
                try {
                    if (event.getMessage().getContent().contains("-g")) {
                        image = webImageSearch.getImageURL(query + " -g");
                    } else {
                        image = webImageSearch.getImageURL(query);
                    }
                } catch (JSONException json){
                    json.printStackTrace();
                }
            }
            return embeds.constructWebSearchEmbed(website, event.getMember().get().getDisplayName(), event.getMember().get().getAvatarUrl(), query, image);
        } catch (Exception e) {
            e.printStackTrace();
            return embeds.constructErrorEmbed();
        }
    }

    public MessageEditSpec getLuckyEdit(MessageCreateEvent event){
        return MessageEditSpec.create()
                .withEmbeds(luckySearch(event));
    }

    public static String getMatchesFromString(String string){
        //Pattern match for questions + responses
        String strPattern = "\"[^\"]*\"";
        Pattern pattern = Pattern.compile(strPattern);
        Matcher matcher = pattern.matcher(string);
        String allMatches = "";
        while (matcher.find()) {
            allMatches = allMatches.concat(matcher.group().replaceAll("\"", "")).concat(" ");
        }
        return allMatches;
    }
}
