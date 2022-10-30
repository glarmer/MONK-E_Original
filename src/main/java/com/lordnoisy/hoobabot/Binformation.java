package com.lordnoisy.hoobabot;

import com.lordnoisy.hoobabot.utility.DiscordUtilities;
import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class Binformation {
    final private static String updateBinChannel = "UPDATE servers SET bin_channel_id = ? WHERE server_id = ?";
    final private static String getAllBinChannels = "SELECT bin_channel_id FROM servers WHERE bin_channel_id IS NOT NULL";
    final private static String deleteServer = "UPDATE servers SET bin_channel_id = NULL WHERE server_id = ?";

    public static EmbedCreateSpec binWeekCalculator(int weekNumber, int weekDay, int dayHour, EmbedBuilder builder, boolean isReminder){
        EmbedCreateSpec result;
        boolean isGreenWeekUpUntilTue = ((weekNumber % 2) == 0 && (weekDay <= 2));
        boolean isGreenWeekUpUntilWed8AM = (((weekNumber % 2) == 0 &&  (weekDay == 3)) && (dayHour < 8));
        boolean isGreenWeekAfterPinkWeekDone = ((weekNumber % 2) != 0 && (weekDay>= 4));
        boolean isGreenWeekAfterPinkWed8AM = (((weekNumber % 2) != 0) && (weekDay == 3) && (dayHour > 8));
        if(isGreenWeekUpUntilTue || isGreenWeekAfterPinkWeekDone || isGreenWeekUpUntilWed8AM || isGreenWeekAfterPinkWed8AM) {
            result = builder.greenWeekEmbedMaker(isReminder);
        } else {
            result = builder.pinkWeekEmbedMaker(isReminder);
        }
        return result;
    }

    public static EmbedCreateSpec addChannelToDatabase(Connection connection, Mono<Member> author, Snowflake serverSnowflake, Snowflake channelSnowflake, EmbedBuilder embeds) {
        if(DiscordUtilities.validatePermissions(author)) {
            String serverID = serverSnowflake.asString();
            String channelID = channelSnowflake.asString();
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

    public static EmbedCreateSpec deleteServerFromDatabase(Connection connection, Mono<Member> author, Snowflake serverSnowflake, EmbedBuilder embeds) {
        if(DiscordUtilities.validatePermissions(author)) {
            String serverID = serverSnowflake.asString();
            try {
                PreparedStatement finalQuery = connection.prepareStatement(deleteServer);
                finalQuery.setString(1, serverID);
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
}
