package com.lordnoisy.hoobabot;

import discord4j.core.spec.EmbedCreateSpec;

import java.sql.*;
import java.util.ArrayList;

public class MySQLUtilities {
    //TODO: WHY HAVE I GOT THIS CLASS?
    final private static String addServerQuery = "INSERT INTO servers (server_id) VALUES (?)";
    final private static String getAllServers = "SELECT server_id FROM servers";


    public static void addServer(Connection connection, String id) {
        try {
            PreparedStatement finalQuery = connection.prepareStatement(addServerQuery);
            finalQuery.setString(1, id);
            finalQuery.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getAllServers(Connection connection) {
        ArrayList<String> resultsToReturn = new ArrayList<>();

        try {
            PreparedStatement finalQuery = connection.prepareStatement(getAllServers);
            ResultSet results = finalQuery.executeQuery();
            while (results.next()) {
                String currentResult = results.getString(1);
                resultsToReturn.add(currentResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultsToReturn;
    }
}
