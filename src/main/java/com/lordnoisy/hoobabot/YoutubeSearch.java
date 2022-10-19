package com.lordnoisy.hoobabot;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class YoutubeSearch {
    private String key;
    public YoutubeSearch (String key) {
        this.key = key;
    }

    public String getVideoURL(String query) {
        try {
            String youtubeSearchURL;
            boolean relevant = true;
            if (query.contains("-nr")) {
                query = query.replace("-nr","");
                youtubeSearchURL = "https://youtube.googleapis.com/youtube/v3/search?maxResults=50&order=relevance&type=video&key=" + key + "&q=";
                relevant = false;
            } else{
                youtubeSearchURL = "https://youtube.googleapis.com/youtube/v3/search?maxResults=1&order=relevance&type=video&key=" + key + "&q=";
            }
            query = getSearchQuery(Utilities.getArray(query));
            URL url = new URL(youtubeSearchURL + query);
            JSONTokener tokener = new JSONTokener(url.openStream());
            JSONObject returnedData = new JSONObject(tokener);
            String result = null;
            if (relevant) {
                result = returnedData.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");
            } else {
                JSONArray array = returnedData.getJSONArray("items");
                result = array.getJSONObject(array.length()-1).getJSONObject("id").getString("videoId");
            }

            return "https://www.youtube.com/watch?v="+ result;
        } catch (Exception exception) {
            exception.printStackTrace();
            return "https://youtu.be/mKkLjJHwRec";
        }
    }

    public String getSearchQuery(String[] messageWords) {
        List<String> messageText = Arrays.asList(messageWords);
        String queryText = messageText.subList(1, messageText.size()).toString();
        queryText = queryText.replace("[", "");
        queryText = queryText.replace("]", "");
        queryText = queryText.replace(",", "");
        queryText = queryText.replace(" ", "+");
        return queryText;
    }

    public String getYoutubeVideoToPlay(String messageContent) {
        String url = getVideoURL(messageContent.replaceAll(":play ", ""));
        return url;
    }
}
