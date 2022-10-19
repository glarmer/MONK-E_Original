package com.lordnoisy.hoobabot;
import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import discord4j.core.spec.EmbedCreateSpec;

import java.sql.*;
import java.util.ArrayList;

public class Monkey {
    final private static String monkeyImageURL = "https://hoobastinki.es/img/gboard/monkeys/";
    final private static String png = ".png";
    final private static String selectAllMonkeysSQL = "SELECT * FROM monkey";
    final private EmbedBuilder embedBuilder;
    final private static String selectMonkeyNameAndDescriptionFromID = "SELECT name, description FROM monkey WHERE id = ?";
    final private static String updateNameQuery = "UPDATE monkey SET name = ? WHERE id = ?";
    final private static String updateDescriptionQuery = "UPDATE monkey SET description = ? WHERE id = ?";

    public Monkey (EmbedBuilder embedBuilder) {
        this.embedBuilder = embedBuilder;
    }

    /**
     * Gets embed of random monkey
     * @param connection the sql connection
     * @return Random monkey embed
     * @throws SQLException if some SQL goes wrong
     */
    public EmbedCreateSpec getRandomMonkey(Connection connection) throws SQLException {

        //Make the SQL query
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(selectAllMonkeysSQL);

        //Count the results
        int totalResults = 0;
        while (resultSet.next()) {
            totalResults ++;
        }

        //Pick a random result
        int chosenRandomMonkeyInt = Utilities.getRandomNumber(1,totalResults);
        String chosenRandomMonkey = String.valueOf(chosenRandomMonkeyInt);

        //Get the URL
        String chosenRandomMonkeyURL = monkeyImageURL + chosenRandomMonkey + png;

        //Get information from DB on the chosen random monkey
        ArrayList<String> details = getMonkeyNameAndDescription(connection, chosenRandomMonkey);
        String randomMonkeyName = details.get(0);
        String randomMonkeyDescription = details.get(1);

        //Create the embed
        return embedBuilder.createMonkeyEmbed(randomMonkeyName + " ID: " + chosenRandomMonkey, chosenRandomMonkeyURL, randomMonkeyDescription);
    }

    public int getNumberOfMonkeys(Connection connection) throws SQLException {

        //Make the SQL query
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(selectAllMonkeysSQL);

        //Count the results
        int totalResults = 0;
        while (resultSet.next()) {
            totalResults ++;
        }

        return totalResults;
    }

    /**
     * Get a specific monkey
     * @param connection the sql connection
     * @param id the monkey ID
     * @return the embed
     * @throws SQLException in case SQL goes wrong
     */
    public EmbedCreateSpec getSpecificMonkey(Connection connection, String id) throws SQLException {

        //Get monkey URL
        String monkeyURL = monkeyImageURL + id + png;

        //Get information on monkey from DB
        ArrayList<String> details = getMonkeyNameAndDescription(connection, id);
        String randomMonkeyName = details.get(0);
        String randomMonkeyDescription = details.get(1);

        //Create the embed
        return embedBuilder.createMonkeyEmbed(randomMonkeyName + " ID: " + id, monkeyURL, randomMonkeyDescription);
    }

    /**
     * Name a monkey in the db
     * @param connection the sql connection
     * @param name the monkey's to be set name
     * @param id the monkey's id
     * @return the embed
     * @throws SQLException in case SQL goes wrong
     */
    public EmbedCreateSpec nameMonkey(Connection connection, String name, String id) throws SQLException {
        //Remove unneeded characters
        /*name = name.replace("[","");
        name = name.replace("]","");
        name = name.replace(",","");
        name = name.replace("'", "\\'");*/

        //Execute query
        if (getNumberOfMonkeys(connection) >= Integer.valueOf(id)) {
            PreparedStatement finalQuery = connection.prepareStatement(updateNameQuery);
            finalQuery.setString(1, name);
            finalQuery.setString(2, id);

            finalQuery.execute();

            //Get monkey details from DB
            ArrayList<String> details = getMonkeyNameAndDescription(connection, id);
            String randomMonkeyName = details.get(0);
            String randomMonkeyDescription = details.get(1);

            //Create embed
            return embedBuilder.createMonkeyEmbed(randomMonkeyName + " ID: " + id, getMonkeyURLFromID(id), randomMonkeyDescription);
        } else {
            return embedBuilder.createMonkeyEmbed("Invalid Monkey", "https://hoobastinki.es/img/gboard/monkeys/158.png", "Please try again, most likely your ID was wrong.");
        }
    }

    /**
     * Describe a monkey in the db
     * @param connection the sql connection
     * @param description the monkey's to be description
     * @param id the monkey's id
     * @return the embed
     * @throws SQLException in case SQL goes wrong
     */
    public EmbedCreateSpec describeMonkey(Connection connection, String description, String id) throws SQLException {

        /*description = description.replace("[","");
        description = description.replace("]","");
        description = description.replace(",","");
        description = description.replace("'", "\\'");*/

        if (getNumberOfMonkeys(connection) >= Integer.valueOf(id)) {

            PreparedStatement finalQuery = connection.prepareStatement(updateDescriptionQuery);
            finalQuery.setString(1, description);
            finalQuery.setString(2, id);

            finalQuery.execute();

            ArrayList<String> details = getMonkeyNameAndDescription(connection, id);
            String monkeyName = details.get(0);
            String monkeyDescription = details.get(1);

            return embedBuilder.createMonkeyEmbed(monkeyName + " ID: " + id, getMonkeyURLFromID(id), monkeyDescription);
        } else {
            return embedBuilder.createMonkeyEmbed("Invalid Monkey", "https://hoobastinki.es/img/gboard/monkeys/158.png", "Please try again, most likely your ID was wrong.");
        }
    }

    public ArrayList<String> getMonkeyNameAndDescription(Connection connection, String id) throws SQLException {
        String randomMonkeyDescription = null;
        String randomMonkeyName = null;

        PreparedStatement finalQuery = connection.prepareStatement(selectMonkeyNameAndDescriptionFromID);
        finalQuery.setString(1, id);

        ResultSet resultSet = finalQuery.executeQuery();
        while (resultSet.next()) {
            try {
                randomMonkeyName = resultSet.getString(1);
            } catch (Exception e) {
                System.out.println("Failed to get name, possibly null");
            }
            try {
                randomMonkeyDescription = resultSet.getString(2);
            } catch (Exception e) {
                System.out.println("Failed to get description, possibly null");
            }
        }

        if (randomMonkeyName == null) {
            randomMonkeyName = "This monkey is unnamed :( Consider naming them!";
        }
        if (randomMonkeyDescription == null) {
            randomMonkeyDescription = "There's a distinct lack of description to be found... maybe you could write one?";
        }

        ArrayList<String> results = new ArrayList<>();
        results.add(randomMonkeyName);
        results.add(randomMonkeyDescription);

        return results;
    }

    public String getMonkeyURLFromID(String id){
        return monkeyImageURL + id + png;
    }

    public void checkIdInRange(Connection connection, String id) throws Exception {
        try {
            if (getNumberOfMonkeys(connection) >= Integer.valueOf(id)) {

            } else {
                throw new Exception("This monkey is not in range");
            }
        } catch (Exception e) {
            throw new Exception("This monkey is invalid");
        }
    }

    public EmbedCreateSpec monkeyCommand(String monkeyString) {

        String[] monkeyStrings = Utilities.getArray(monkeyString);

        //Set the variables
        String space = " ";
        String name = "";
        String id = "";
        String description = "";
        ArrayList<String> tags = new ArrayList<>();
        String currentType = "";
        EmbedCreateSpec monkeyToReturn = null;
        try {
        Connection connection = Main.dataSource.getDatabaseConnection();


            //Loop through each word
            for (int i = 0; i < monkeyStrings.length; i++) {
                String currentWord = monkeyStrings[i];
                currentWord = currentWord.replace("'", "\\'");

                //Get the ID if formatted like id:xyz
                if (currentWord.contains("id:")) {
                    try {
                        String currentID = currentWord.split(":")[1];
                        id = id.concat(currentID);
                    } catch (Exception e) {
                        System.out.println("No ID without space");
                        currentType = "id";
                    }
                    //Stop the switch running since we already have our ID
                    continue;
                }

                //Add each word to appropriate value
                switch (currentType) {
                    case ("id"):
                        if (!currentWord.contains("name:")) {
                            id = id.concat(currentWord);
                        } else {
                            currentType = "name";
                            try {
                                currentWord = currentWord.split(":")[1];
                                name = name.concat(currentWord);
                            } catch (Exception e) {
                                System.out.println("No name without space");
                            }
                        }
                        break;
                    case ("name"):
                        if (!currentWord.contains("description:")) {

                            name = name.concat(space).concat(currentWord);
                        } else {
                            currentType = "description";
                            try {
                                currentWord = currentWord.split(":")[1];
                                description = description + currentWord;
                            } catch (Exception e) {
                                System.out.println("No description without space");
                            }
                        }
                        break;
                    case ("description"):
                        if (!currentWord.contains("tags:")) {
                            description = description.concat(space).concat(currentWord);
                        } else {
                            currentType = "tags";
                            try {
                                currentWord = currentWord.split(":")[1];
                                tags.add(currentWord);
                            } catch (Exception e) {
                                System.out.println("No description without space");
                            }
                        }
                        break;
                    case ("tags"):
                        tags.add(currentWord);
                        break;
                }
            }

            if (!id.equals("")) {
                try {
                    checkIdInRange(connection, id);
                    if (tags.size() > 0) {
                        //Code for entire command here
                    } else if (!description.equals("")) {
                        describeMonkey(connection, description, id);
                        monkeyToReturn = nameMonkey(connection, name, id);
                    } else if (!name.equals("")) {
                        monkeyToReturn = nameMonkey(connection, name, id);
                    } else if (!id.equals("")) {
                        //Code for specific monkey here
                        monkeyToReturn = getSpecificMonkey(connection, id);
                    }
                } catch (Exception e) {
                    monkeyToReturn = embedBuilder.createMonkeyEmbed("Invalid Monkey", "https://hoobastinki.es/img/gboard/monkeys/158.png", "Please try again, most likely your ID was wrong.");
                }
            } else {
                //Random monkey here
                monkeyToReturn = getRandomMonkey(connection);
            }
        } catch (Exception e) {
            monkeyToReturn = embedBuilder.constructErrorEmbed();
        }
        return monkeyToReturn;
    }
}
