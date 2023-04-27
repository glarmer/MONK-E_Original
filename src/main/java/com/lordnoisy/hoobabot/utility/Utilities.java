package com.lordnoisy.hoobabot.utility;

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
     * Checks if all characters are present in an encoding
     * @param message the message to hide
     * @param format the message to hide the other inside
     * @return whether its valid
     */
    public static boolean checkPresent(String message, String format) {
        boolean present = true;
        boolean isPresent = false;
        for (int i = 0; i < message.length(); i++) {
            char currentMessageChar = message.charAt(i);
            isPresent = false;
            for (int j = 0; j < format.length(); j++) {
                char currentFormatChar = format.charAt(j);
                if (currentMessageChar == currentFormatChar) {
                    isPresent = true;
                }
            }
            present = present && isPresent;
        }
        return present;
    }

    /**
     * Hides a message inside another
     * @param message the message to hide
     * @param format the message to hide the other inside
     * @return the new message
     */
    public static String encodeMessage(String message, String format) {
        String newMessage = "";
        char messageLetter = message.charAt(0);
        char formatLetter = format.charAt(0);
        System.out.println(messageLetter + " test");
        String formatRemaining = "";
        String messageRemaining = "";
        boolean wasPrevious = false;
        if (messageLetter == formatLetter || messageLetter == "t".charAt(0)) {
            newMessage = newMessage + formatLetter + "||";
            formatRemaining = format.substring(1);
            messageRemaining = message.substring(1);
            wasPrevious = true;

        } else {
            newMessage = newMessage + "||";
            formatRemaining = format.substring(0);
            messageRemaining = message.substring(0);
        }

        int cyclesSinceSuccess = 0;

        while (messageRemaining.length() > 0) {
            messageLetter = messageRemaining.charAt(0);
            formatLetter = formatRemaining.charAt(0);

            //Is this letter in the message?
            if (messageLetter == formatLetter) {
                //Account for if previous was also in message and remove extra ||
                if (wasPrevious) {
                    newMessage = newMessage.substring(0, newMessage.length()-2);
                    newMessage = newMessage + formatLetter + "||";
                } else {
                    newMessage = newMessage + "||" + formatLetter + "||";
                }
                messageRemaining = messageRemaining.substring(1);
                wasPrevious = true;
                cyclesSinceSuccess = 0;
            } else {
                //Move on and increment cycles counter
                newMessage = newMessage + formatLetter;
                wasPrevious = false;
                cyclesSinceSuccess++;
            }

            formatRemaining = formatRemaining.substring(1);
            //Deal with format cycling
            if (formatRemaining.length() < 1) {
                formatRemaining = format;
                newMessage = newMessage + "\n";
            }

            //Failsafe for unknown characters
            if (cyclesSinceSuccess == format.length()) {
                newMessage = newMessage.substring(0, newMessage.length()-format.length());
                if (wasPrevious) {
                    newMessage = newMessage.substring(0, newMessage.length()-2);
                    newMessage = newMessage + messageLetter + "||";
                } else {
                    newMessage = newMessage + "||" + messageLetter + "||";
                }
                messageRemaining = messageRemaining.substring(1);
                formatRemaining = formatRemaining.substring(1);
                wasPrevious = true;
                cyclesSinceSuccess = 0;
            }
        }
        if (formatRemaining.length() != format.length()) {
            //Add remaining of text on
            newMessage = newMessage.substring(0, newMessage.length()-2);
            newMessage = newMessage + "||" + formatRemaining + "||";
        } else {
            //Remove newline and ||
            newMessage = newMessage.substring(0, newMessage.length()-3);
        }
        return newMessage;
    }

    /**
     * Convert a number to a different base
     * @param number the number to convert
     * @param outputBase the base to convert to
     * @return the converted number
     */
    public static String convertToBase(String number, int outputBase) {
        System.out.println(number);
        Long numberAsLong = Long.parseLong(number);
        return Long.toString(numberAsLong, outputBase);
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
