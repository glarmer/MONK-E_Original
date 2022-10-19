package com.lordnoisy.hoobabot;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Permission;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;

public class Binformation {
    final private static String updateBinChannel = "UPDATE servers SET bin_channel_id = ? WHERE server_id = ?";
    final private static String getAllBinChannels = "SELECT bin_channel_id FROM servers WHERE bin_channel_id IS NOT NULL";

    public static EmbedCreateSpec binWeekCalculator(int weekNumber, int weekDay, int dayHour, EmbedBuilder builder, boolean isReminder){
        EmbedCreateSpec result;
        boolean isGreenWeekUpUntilTue = ((weekNumber % 2) == 0 && (weekDay <= 2));
        boolean isGreenWeekUpUntilWed8AM = (((weekNumber % 2) == 0 &&  (weekDay == 3)) && (dayHour < 8));
        boolean isGreenWeekAfterPinkWeekDone = ((weekNumber % 2) != 0 && (weekDay>= 4));
        boolean isGreenWeekAfterPinkWed8AM = (((weekNumber % 2) != 0) && (weekDay == 3) && (dayHour > 8));
        System.out.println(weekNumber);
        if(isGreenWeekUpUntilTue || isGreenWeekAfterPinkWeekDone || isGreenWeekUpUntilWed8AM || isGreenWeekAfterPinkWed8AM) {
            result = builder.greenWeekEmbedMaker(isReminder);
            System.out.println("Binformation Green week");
        } else {
            result = builder.pinkWeekEmbedMaker(isReminder);
            System.out.println("Binformation pink week");
        }
        return result;
    }

    public static EmbedCreateSpec addChannelToDatabase(Connection connection, MessageCreateEvent event, EmbedBuilder embeds) {
        Mono<Member> author = event.getMember().get().asFullMember();

        if(DiscordUtilities.validatePermissions(author)) {
            String serverID = event.getGuildId().get().asString();
            String channelID = event.getMessage().getChannelId().asString();
            try {
                PreparedStatement finalQuery = connection.prepareStatement(updateBinChannel);
                finalQuery.setString(1, channelID);
                finalQuery.setString(2, serverID);
                finalQuery.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return embeds.constructReminderChannelSetEmbed();
        } else {
            return embeds.constructInsufficientPermissionsEmbed();
        }
    }

    public static ArrayList<String> getChannelsFromDatabase(Connection connection) {
        ArrayList<String> channels = new ArrayList<>();
        try {
            PreparedStatement finalQuery = connection.prepareStatement(getAllBinChannels);
            ResultSet resultSet = finalQuery.executeQuery();

            while(resultSet.next()) {
                channels.add(resultSet.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channels;
    }

    public static void checkIfUserIsInDatabase(Connection connection, MessageCreateEvent event) {
    }

    public static void addUserToDatabase(Connection connection, MessageCreateEvent event) {
    }

    public static void addPersonalBinReminder(Connection connection, MessageCreateEvent event) {
    }
}
