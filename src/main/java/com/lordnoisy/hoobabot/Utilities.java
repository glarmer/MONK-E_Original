package com.lordnoisy.hoobabot;

import org.json.JSONArray;
import org.json.JSONTokener;

import java.net.URL;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    /**
     * Replace hex in a string with its actual characters
     * @param stringToEdit the string to fix
     * @return fixed string
     */
    public static String replaceHexInString (String stringToEdit) {
        String regex = "%([1-9a-fA-F]{2})";
        Matcher matcher = Pattern.compile(regex).matcher(stringToEdit);
        while (matcher.find()) {
            StringBuilder output = new StringBuilder();
            String match = matcher.group(1);
            output.append((char) Integer.parseInt(match, 16));
            String finalChar = output.toString();
            String toReplace = "%" + match;
            stringToEdit = stringToEdit.replace(toReplace, finalChar);
        }
        return stringToEdit;
    }

    /**
     * Get random word(s)
     * @param number number of words
     * @return random words
     */
    public static String getRandomWord(int number){
        try {
            URL url = new URL("https://random-word-api.herokuapp.com/word?number="+number);  // example url which return json data
            JSONTokener tokener = new JSONTokener(url.openStream());

            JSONArray array = new JSONArray(tokener);

            String finalWords = "";
            for(int i = 0; i < array.length(); i++){
                finalWords = finalWords.concat(array.getString(i)).concat("+");
            }

            return finalWords;
        } catch (Exception e) {
            return "Word Search Error";
        }
    }

    /**
     * Generate a random alphanumeric string of a desired length
     * @param length the desired length of string
     * @return a random generated string
     */
    public static String getRandomString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static String replaceSpaces(String searchQuery) {
        searchQuery = searchQuery.replace(" ","+");
        return searchQuery;
    }

    public static String[] getArray(String string){
        return string.split("\\s+");
    }

    public static int getRandomNumber(int min, int max){
        return (int)(Math.random() * (max - min + 1) + min);
    }
}
