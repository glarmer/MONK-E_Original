package com.lordnoisy.hoobabot;

import com.lordnoisy.hoobabot.utility.DataSource;
import com.lordnoisy.hoobabot.utility.DiscordUtilities;
import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;

public class Binformation {
    final private static String updateBinChannel = "UPDATE servers SET bin_channel_id = ? WHERE server_id = ?";
    final private static String getAllBinChannels = "SELECT bin_channel_id FROM servers WHERE bin_channel_id IS NOT NULL";
    final private static String deleteServer = "UPDATE servers SET bin_channel_id = NULL WHERE server_id = ?";

    private static final String PINK_RECYCLING_BIN_URL = "https://glarmer.xyz/monke/img/bins/pinkRecyclingBin.png";
    private static final String PINK_RECYCLE_LOGO = "https://glarmer.xyz/monke/img/bins/pinkRecycleLogo.png";
    private static final String GREEN_RECYCLE_LOGO = "https://glarmer.xyz/monke/img/bins/greenRecycleLogo.png";
    private static final String GREEN_RECYCLING_BIN_URL = "https://glarmer.xyz/monke/img/bins/greenRecyclingBin.png";
    private static final String SWANSEA_COUNCIL_URL = "https://www.swansea.gov.uk/kerbsidecollections";

    private static final Color PINK_WEEK_COLOR = Color.of(0xdd49a7);
    private static final Color GREEN_WEEK_COLOR = Color.of(0x79E357);

    public static EmbedCreateSpec binWeekCalculator(int weekNumber, int weekDay, int dayHour, EmbedBuilder builder, boolean isReminder){
        EmbedCreateSpec result;
        boolean isGreenWeekUpUntilTue = ((weekNumber % 2) == 0 && (weekDay <= 2));
        boolean isGreenWeekUpUntilWed8AM = (((weekNumber % 2) == 0 &&  (weekDay == 3)) && (dayHour < 8));
        boolean isGreenWeekAfterPinkWeekDone = ((weekNumber % 2) != 0 && (weekDay>= 4));
        boolean isGreenWeekAfterPinkWed8AM = (((weekNumber % 2) != 0) && (weekDay == 3) && (dayHour > 8));
        if(isGreenWeekUpUntilTue || isGreenWeekAfterPinkWeekDone || isGreenWeekUpUntilWed8AM || isGreenWeekAfterPinkWed8AM) {
            result = greenWeekEmbedMaker(isReminder);
        } else {
            result = pinkWeekEmbedMaker(isReminder);
        }
        return result;
    }

    public static Mono<Void> processBinConfigurationCommand(ChatInputInteractionEvent event, DataSource dataSource) {
        Snowflake serverSnowflake = null;
        if (event.getInteraction().getGuildId().isPresent()) {
            serverSnowflake = event.getInteraction().getGuildId().get();
        }
        Snowflake binChannelSnowflake = null;
        boolean deleteConfig = false;
        for (int i = 0; i < event.getOptions().size(); i++) {
            ApplicationCommandInteractionOption option = event.getOptions().get(i);
            String optionName = option.getName();
            if (optionName.startsWith("bin_channel")) {
                binChannelSnowflake = option.getValue().get().asSnowflake();
            } else if (optionName.equals("delete_config")) {
                deleteConfig = option.getValue().get().asBoolean();
            }
        }
        if (serverSnowflake != null) {
            try {
                if (deleteConfig) {
                    Binformation.deleteServerFromDatabase(dataSource.getDatabaseConnection(), event.getInteraction().getMember().get().asFullMember(), serverSnowflake);
                } else {
                    Binformation.addChannelToDatabase(dataSource.getDatabaseConnection(), event.getInteraction().getMember().get().asFullMember(), serverSnowflake, binChannelSnowflake);
                }
                return event.editReply("Bins config set successfully").then();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return event.editReply("Bins config failed to update").then();
    }

    public static EmbedCreateSpec pinkWeekEmbedMaker(boolean isReminder){
        return EmbedCreateSpec.builder()
                .color(PINK_WEEK_COLOR)
                .author(getTitleString(isReminder, "pink"), SWANSEA_COUNCIL_URL, PINK_RECYCLE_LOGO)
                .description("It's pink week, that includes the pink bins and food bins.")
                .thumbnail(PINK_RECYCLING_BIN_URL)
                .timestamp(Instant.now())
                .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconUrl() + Utilities.getRandomNumber(0,156) + ".png"))
                .build();
    }

    public static EmbedCreateSpec greenWeekEmbedMaker(boolean isReminder){
        return EmbedCreateSpec.builder()
                .color(GREEN_WEEK_COLOR)
                .author(getTitleString(isReminder, "green"), SWANSEA_COUNCIL_URL, GREEN_RECYCLE_LOGO)
                .description("It's green week, that includes black bags, green bins and the food bins.")
                .thumbnail(GREEN_RECYCLING_BIN_URL)
                .timestamp(Instant.now())
                .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconUrl() + Utilities.getRandomNumber(0,156) + ".png"))
                .build();
    }

    private static String getTitleString(boolean isReminder, String colour){
        if (colour.equals("green")){
            if (isReminder){
                return "Have you done the bins? (Green Week)";
            } else {
                return "Green Week";
            }
        } else {
            if (isReminder){
                return "Have you done the bins? (Pink Week)";
            } else {
                return "Pink Week";
            }
        }
    }

    public static EmbedCreateSpec addChannelToDatabase(Connection connection, Mono<Member> author, Snowflake serverSnowflake, Snowflake channelSnowflake) {
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
            return constructReminderChannelSetEmbed();
        } else {
            return EmbedBuilder.constructInsufficientPermissionsEmbed();
        }
    }

    public static EmbedCreateSpec deleteServerFromDatabase(Connection connection, Mono<Member> author, Snowflake serverSnowflake) {
        if(DiscordUtilities.validatePermissions(author)) {
            String serverID = serverSnowflake.asString();
            try {
                PreparedStatement finalQuery = connection.prepareStatement(deleteServer);
                finalQuery.setString(1, serverID);
                finalQuery.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return constructReminderChannelSetEmbed();
        } else {
            return EmbedBuilder.constructInsufficientPermissionsEmbed();
        }
    }

    public static EmbedCreateSpec constructReminderChannelSetEmbed(){
        return EmbedCreateSpec.builder()
                .color(EmbedBuilder.getStandardColor())
                .title("You have successfully set this channel as the bin reminders channel")
                .timestamp(Instant.now())
                .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconUrl() + Utilities.getRandomNumber(0,156) + ".png"))
                .build();
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

    public static String getPinkRecyclingBinUrl() {
        return PINK_RECYCLING_BIN_URL;
    }

    public static String getPinkRecycleLogo() {
        return PINK_RECYCLE_LOGO;
    }

    public static String getSwanseaCouncilUrl() {
        return SWANSEA_COUNCIL_URL;
    }

    public static Color getPinkWeekColor() {
        return PINK_WEEK_COLOR;
    }

    public static Color getGreenWeekColor() {
        return GREEN_WEEK_COLOR;
    }
}
