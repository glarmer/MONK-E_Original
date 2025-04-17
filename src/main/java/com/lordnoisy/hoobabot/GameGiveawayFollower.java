package com.lordnoisy.hoobabot;

import com.api.igdb.request.IGDBWrapper;
import com.api.igdb.request.TwitchAuthenticator;
import com.api.igdb.utils.TwitchToken;
import com.lordnoisy.hoobabot.utility.DataSource;
import com.lordnoisy.hoobabot.utility.DiscordUtilities;
import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import com.lordnoisy.hoobabot.utility.Utilities;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.Embed;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.json.JSONArray;
import org.json.JSONObject;
import reactor.core.publisher.Mono;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public class GameGiveawayFollower {
    final private static String getAllGiveawayConfig = "SELECT giveaway_channel_id, giveaway_role_id FROM servers WHERE giveaway_channel_id IS NOT NULL";
    final private static String updateGiveawayConfiguration = "UPDATE servers SET giveaway_channel_id = ?, giveaway_role_id = ? WHERE server_id = ?";
    final private static String deleteServer = "UPDATE servers SET giveaway_channel_id, giveaway_role_id = NULL WHERE server_id = ?";

    private TwitchToken token;
    TwitchAuthenticator tAuth = TwitchAuthenticator.INSTANCE;
    private final RSSReader rssReader = new RSSReader();
    private final IGDBWrapper wrapper = IGDBWrapper.INSTANCE;
    private final WebImageSearch webImageSearch;
    private final String TWITCH_CLIENT_SECRET;
    private final String TWITCH_CLIENT_ID;
    private long frequency;
    private String lastSentGiveaway;
    private Properties properties;

    /**
     * Constructor for GameGiveawayFollower
     * @param twitch_client_id the twitch client id
     * @param twitch_client_secret the twitch client secret
     */
    public GameGiveawayFollower(String twitch_client_id, String twitch_client_secret, WebImageSearch webImageSearch, String lastSentGiveaway, Properties properties) {
        this.TWITCH_CLIENT_ID = twitch_client_id;
        this.TWITCH_CLIENT_SECRET = twitch_client_secret;
        this.token = this.tAuth.requestTwitchToken(TWITCH_CLIENT_ID, TWITCH_CLIENT_SECRET);
        System.out.println("Twitch token: " + this.token);
        this.wrapper.setCredentials(twitch_client_id, token.getAccess_token());
        this.webImageSearch = webImageSearch;
        this.lastSentGiveaway = lastSentGiveaway;
        this.properties = properties;
        setFrequency(900);
    }

    private void resetToken() {
        this.token = this.tAuth.requestTwitchToken(TWITCH_CLIENT_ID, TWITCH_CLIENT_SECRET);
    }

    /**
     * Set the frequency to check the RSS feed
     * @param timeInSeconds the time in seconds
     */
    private void setFrequency(long timeInSeconds) {
        this.frequency = timeInSeconds * 1000;
    }

    /**
     * Get the frequency to check the RSS feed
     * @return the frequency in milliseconds
     */
    public long getFrequency() {
        return this.frequency;
    }

    /**
     * Check for giveaways and return a Mono to send them
     * @param messageChannels the channels that follow giveaways
     * @return a Mono with the messages to send
     */
    public Mono<Void> checkForAndSendGiveaways(HashMap<MessageChannel, String> messageChannels, boolean test) {
        resetToken();
        System.out.println("READING GIVEAWAYS FEED");
        ArrayList<GameGiveaway> giveaways = getGiveawaysFromApi();
        ArrayList<EmbedCreateSpec> giveawayEmbedsToSend = new ArrayList<>();
        for (int i = 0; i < giveaways.size(); i++) {
            GameGiveaway giveaway = giveaways.get(i);
            System.out.println(giveaway.toString());
            if (!test & giveaway.getTitle().replaceAll("\\s+","").equals(lastSentGiveaway)) {
                break;
            }
            EmbedCreateSpec giveawayEmbed = giveaway.createGameFeedEntryEmbed();
            giveawayEmbedsToSend.add(giveawayEmbed);
        }

        //Set lastSentGiveaway
        if (!giveawayEmbedsToSend.isEmpty()) {
            setLastSentGiveaway(giveawayEmbedsToSend.get(0).title().get());
        }

        Mono<Void> monoToReturn = Mono.empty();
        Mono<Void> pingRoleMessage = Mono.empty();
        for (Map.Entry<MessageChannel, String> data : messageChannels.entrySet()) {
            MessageChannel messageChannel = data.getKey();
            if (!data.getValue().equals("") & !giveawayEmbedsToSend.isEmpty()) {
                pingRoleMessage = messageChannel.createMessage(data.getValue()).then();
            }
            for (int i = giveawayEmbedsToSend.size() - 1; i >= 0; i--) {
                System.out.println("MESSAGE CHANNELS SIZE " + messageChannels.size());
                    System.out.println("SENDING GIVEAWAY MESSAGES TO " + messageChannel.getId().asString() + " TITLE: ");
                    monoToReturn = monoToReturn.and(messageChannel.createMessage(giveawayEmbedsToSend.get(i)));
            }
        }
        return pingRoleMessage.and(monoToReturn);
    }

    /**
     * Store the last sent giveaway so that we can prevent the bot sending old giveaways
     * @param lastSentGiveaway the last sent giveaway
     */
    private void setLastSentGiveaway(String lastSentGiveaway) {
        this.lastSentGiveaway = lastSentGiveaway.replaceAll("\\s+","");
        properties.setProperty(Main.LAST_SENT_GIVEAWAY, this.lastSentGiveaway);
        try {
            properties.store(new FileOutputStream("monke.properties"), null);
        } catch (IOException e) {
            System.out.println("ERROR WRITING PROPERTIES");
            e.printStackTrace();
        }
    }

    public ArrayList<GameGiveaway> getGiveawaysFromApi() {
        ArrayList<GameGiveaway> gameGiveaways = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://www.gamerpower.com/api/giveaways?platform=pc&amp;&type=game&amp;sort-by=date")).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray jsonArray = new JSONArray(response.body());
            for (int i = 0; i < jsonArray.length(); i++) {
                //Newest giveaway should go into array first
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                gameGiveaways.add(new GameGiveaway(jsonObject));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gameGiveaways;
    }

    public Mono<Void> executeSetConfigurationCommand(ChatInputInteractionEvent event, DataSource dataSource) {
        if (event.getInteraction().getGuildId().isEmpty()) {
            return Mono.empty();
        }
        Snowflake serverSnowflake = event.getInteraction().getGuildId().get();
        Snowflake giveawayChannelSnowflake = null;
        Snowflake giveawayRole = null;
        boolean deleteGiveawayConfig = false;
        for (int i = 0; i < event.getOptions().size(); i++) {
            ApplicationCommandInteractionOption option = event.getOptions().get(i);
            String optionName = option.getName();
            if (optionName.startsWith("giveaway_channel")) {
                giveawayChannelSnowflake = option.getValue().get().asSnowflake();
            } else if (optionName.equals("delete_config")) {
                deleteGiveawayConfig = option.getValue().get().asBoolean();
            } else if (optionName.equals("giveaway_role")) {
                giveawayRole = option.getValue().get().asSnowflake();
            }
        }
        EmbedCreateSpec embed;
        try {
            if (deleteGiveawayConfig) {
                embed = this.deleteServerFromDatabase(dataSource.getDatabaseConnection(), event.getInteraction().getMember().get().asFullMember(), serverSnowflake);
            } else {
                embed = this.setGiveawayConfigurationInDB(dataSource.getDatabaseConnection(), event.getInteraction().getMember().get().asFullMember(), serverSnowflake, giveawayChannelSnowflake, giveawayRole);
            }
        } catch (SQLException e) {
            embed = EmbedBuilder.constructErrorEmbed();
        }
        EmbedCreateSpec finalEmbed = embed;


        Mono<Void> giveawayConfigMono = event.getInteraction().getChannel()
                .ofType(MessageChannel.class)
                .flatMap(channel -> channel.createMessage(finalEmbed))
                .then();
        return event.deleteReply().then(giveawayConfigMono);
    }

    /**
     * Add a channel to the database
     * @param connection the database connection
     * @param author the user who ran the command
     * @param serverSnowflake the server snowflake
     * @param channelSnowflake the channel snowflake
     * @return an embed of the result
     */
    public EmbedCreateSpec setGiveawayConfigurationInDB(Connection connection, Mono<Member> author, Snowflake serverSnowflake, Snowflake channelSnowflake, Snowflake roleSnowflake) {
        if(DiscordUtilities.validatePermissions(author)) {
            String serverID = serverSnowflake.asString();
            String channelID = channelSnowflake.asString();
            String roleID = null;
            if (roleSnowflake != null) {
                roleID = roleSnowflake.asString();
            }
            try {
                PreparedStatement finalQuery = connection.prepareStatement(updateGiveawayConfiguration);
                finalQuery.setString(1, channelID);
                finalQuery.setString(2, roleID);
                finalQuery.setString(3, serverID);
                finalQuery.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this.constructGiveawayChannelSetEmbed();
        } else {
            return EmbedBuilder.constructInsufficientPermissionsEmbed();
        }
    }

    /**
     * Delete a server's giveaway channel from the database
     * @param connection the database connection
     * @param author the user who ran the command
     * @param serverSnowflake the server snowflake
     * @return an embed of the result
     */
    public EmbedCreateSpec deleteServerFromDatabase(Connection connection, Mono<Member> author, Snowflake serverSnowflake) {
        if(DiscordUtilities.validatePermissions(author)) {
            String serverID = serverSnowflake.asString();
            try {
                PreparedStatement finalQuery = connection.prepareStatement(deleteServer);
                finalQuery.setString(1, serverID);
                finalQuery.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this.constructGiveawayChannelSetEmbed();
        } else {
            return EmbedBuilder.constructInsufficientPermissionsEmbed();
        }
    }

    public EmbedCreateSpec constructGiveawayChannelSetEmbed(){
        return EmbedCreateSpec.builder()
                .color(EmbedBuilder.getStandardColor())
                .title("You have successfully set this channel as the free game giveaways channel")
                .timestamp(Instant.now())
                .footer(EmbedBuilder.getFooterText(), (EmbedBuilder.getFooterIconUrl() + Utilities.getRandomNumber(0,156) + ".png"))
                .build();
    }

    /**
     * Get all the servers from the database
     * @param connection the database connection
     * @return
     */
    public HashMap<String, String> getChannelsFromDatabase(Connection connection) {
        HashMap<String, String> channels = new HashMap<>();
        try {
            PreparedStatement finalQuery = connection.prepareStatement(getAllGiveawayConfig);
            ResultSet resultSet = finalQuery.executeQuery();

            while(resultSet.next()) {
                channels.put(resultSet.getString(1), resultSet.getString(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channels;
    }

}
