package com.lordnoisy.hoobabot;

import com.lordnoisy.hoobabot.music.Music;
import com.lordnoisy.hoobabot.registry.ApplicationCommandRegistry;
import com.lordnoisy.hoobabot.utility.*;
import com.lordnoisy.hoobabot.weather.WeatherReader;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.intent.IntentSet;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public final class Main {
    private static final Map<String, Command> commands = new HashMap<>();
    private static MessageOfTheDay motd = new MessageOfTheDay();
    private static final String status = "for ;help";
    private static final ModuleFinder moduleFinder = new ModuleFinder();
    private static ArrayList<MessageChannel> messageChannels = new ArrayList<>();
    public static DataSource dataSource = null;

    private static final String PROPERTY_END = " = \n";
    private static final String BOT_PROPERTY = "bot_token";
    private static final String JDBC_PROPERTY = "jdbc_url";
    private static final String DB_USER_PROPERTY = "db_username";
    private static final String DB_PASSWORD_PROPERTY = "db_password";
    private static final String YOUTUBE_PAPISID_PROPERTY = "youtube_papisid";
    private static final String YOUTUBE_PSID_PROPERTY = "youtube_psid";
    private static final String X_RAPID_KEY_PROPERTY = "xRapidKey";
    private static final String SHORTENER_URL_PROPERTY = "shortener_url";
    private static final String SHORTENER_SIGNATURE_PROPERTY = "shortener_signature";
    private static final String SHORTENER_USER_PROPERTY = "shortener_username";
    private static final String SHORTENER_PASSWORD_PROPERTY = "shortener_password";
    private static final String MET_API_KEY_PROPERTY = "met_api_key";
    private static final String GOOGLE_API_KEY_PROPERTY = "google_api_key";
    private static final String BING_API_KEY_PROPERTY = "bing_api_key";

    private static final String DM_ERROR = "This command can't be run in DMs!";

    public static void main(final String[] args) throws SQLException {
        String path = new File(".").getAbsolutePath();
        path = path.substring(0, path.length()-1);
        String configPath = path + "hoobabot.properties";

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(configPath));
        } catch (IOException e) {
            File file = new File(configPath);

            try {
                FileWriter writer = new FileWriter(file);
                file.createNewFile();

                writer.write(BOT_PROPERTY + PROPERTY_END + JDBC_PROPERTY + PROPERTY_END + DB_USER_PROPERTY + PROPERTY_END + DB_PASSWORD_PROPERTY + PROPERTY_END
                        + YOUTUBE_PAPISID_PROPERTY + PROPERTY_END + YOUTUBE_PSID_PROPERTY + PROPERTY_END + X_RAPID_KEY_PROPERTY + PROPERTY_END
                        + SHORTENER_URL_PROPERTY + PROPERTY_END + SHORTENER_SIGNATURE_PROPERTY + PROPERTY_END + SHORTENER_USER_PROPERTY + PROPERTY_END
                        + SHORTENER_PASSWORD_PROPERTY + PROPERTY_END + MET_API_KEY_PROPERTY + PROPERTY_END + GOOGLE_API_KEY_PROPERTY + PROPERTY_END
                        + BING_API_KEY_PROPERTY + PROPERTY_END);
                writer.close();
                System.out.println("Config file has been created, please configure the bot correctly!");
            } catch (IOException ioException) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        String token = properties.getProperty(BOT_PROPERTY);
        String url = properties.getProperty(JDBC_PROPERTY);
        String user = properties.getProperty(DB_USER_PROPERTY);
        String password = properties.getProperty(DB_PASSWORD_PROPERTY);
        String papsid = properties.getProperty(YOUTUBE_PAPISID_PROPERTY);
        String psid = properties.getProperty(YOUTUBE_PSID_PROPERTY);
        String xRapidKey = properties.getProperty(X_RAPID_KEY_PROPERTY);
        String shortenerURL = properties.getProperty(SHORTENER_URL_PROPERTY);
        String shortenerSignature = properties.getProperty(SHORTENER_SIGNATURE_PROPERTY);
        String shortenerUsername = properties.getProperty(SHORTENER_USER_PROPERTY);
        String shortenerPassword = properties.getProperty(SHORTENER_PASSWORD_PROPERTY);
        String weatherKey = properties.getProperty(MET_API_KEY_PROPERTY);
        String googleAPIKey = properties.getProperty(GOOGLE_API_KEY_PROPERTY);
        String bingAPIKey = properties.getProperty(BING_API_KEY_PROPERTY);

        if(properties.contains("")) {
            System.out.println("Please ensure that your configuration is set up correctly!");
            System.out.println("Configuration File Location: " + configPath);
            System.exit(1);
        }

        YoutubeHttpContextFilter.setPAPISID(papsid);
        YoutubeHttpContextFilter.setPSID(psid);

        dataSource = new DataSource(url,user,password);
        URLShortener shortener = new URLShortener(shortenerURL, shortenerSignature, shortenerUsername, shortenerPassword);
        WeatherReader weatherReader = new WeatherReader(weatherKey);
        WebImageSearch webImageSearch = new WebImageSearch(googleAPIKey, xRapidKey, bingAPIKey);
        EmbedBuilder embeds = new EmbedBuilder(webImageSearch);
        Monkey monkey = new Monkey(embeds);
        YoutubeSearch youtubeSearch = new YoutubeSearch(googleAPIKey);

        ArrayList<String> serversAlreadyExist = MySQLUtilities.getAllServers(dataSource.getDatabaseConnection());
        final Map<Snowflake, Music> musicMap = new HashMap<>();
        System.out.println("Hoobabot started");

        // Creates AudioPlayer instances and translates URLs to AudioTrack instances
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        // Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager);
        // Create an AudioPlayer so Discord4J can receive audio data
        final AudioPlayer player = playerManager.createPlayer();

        final Poll poll = new Poll(embeds);

        commands.put("ocr", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage((new OCR(event, embeds)).doOCR()))
                .then());

        commands.put("lucky", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(embeds.constructSearchingEmbed()).withMessageReference(event.getMessage().getId())
                        .flatMap(message -> message.edit(new Lucky(embeds, xRapidKey, shortener, webImageSearch).getLuckyEdit(event))))
                .then());

        commands.put("image", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(embeds.constructSearchingEmbed()).withMessageReference(event.getMessage().getId())
                        .flatMap(message -> message.edit(webImageSearch.getImageEditSpec(event.getMessage()))))
                .then());

        commands.put("google", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(embeds.constructSearchingEmbed()).withMessageReference(event.getMessage().getId()).flatMap(message -> message.edit(webImageSearch.getGoogleEditSpec(event.getMessage()))))
                .then());

        commands.put("bing", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(embeds.constructSearchingEmbed()).withMessageReference(event.getMessage().getId()).flatMap(message -> message.edit(webImageSearch.getBingEditSpec(event.getMessage()))))
                .then());

        commands.put("help", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(embeds.constructHelpEmbed()).withMessageReference(event.getMessage().getId()))
                .then());

        commands.put("pink", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(embeds.pinkWeekEmbedMaker(false)).withMessageReference(event.getMessage().getId()))
                .then());

        commands.put("green", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(embeds.greenWeekEmbedMaker(false)).withMessageReference(event.getMessage().getId()))
                .then());

        commands.put("quote", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(motd.getMessageOfTheDay(embeds)).withMessageReference(event.getMessage().getId())
                        .flatMap(message -> message.edit(motd.getFinalMessageOfTheDay(embeds))))
                .then());

        commands.put("monkey", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(monkey.monkeyCommand(event.getMessage().getContent())).withMessageReference(event.getMessage().getId()))
                .then());

        commands.put("video", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(youtubeSearch.getVideoURL(event.getMessage().getContent())).withMessageReference(event.getMessage().getId()))
                .then());

        commands.put("test", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("howdy").withMessageReference(event.getMessage().getId()))
                .then());

        //Start timer
        System.out.println("Timer started");
        DiscordClient client = DiscordClient.create(token);
        Mono<Void> login = client.gateway().setEnabledIntents(IntentSet.all()).withGateway((GatewayDiscordClient gateway) -> {

            ArrayList<String> binChannels;
            try {
                binChannels = Binformation.getChannelsFromDatabase(dataSource.getDatabaseConnection());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            //TODO: Remove this block, not that it matters too much
            for (String binChannel : binChannels) {
                System.out.println("Bin Channels: " + binChannel);
                var channel = gateway.getChannelById(Snowflake.of(binChannel)).block();
                messageChannels.add((MessageChannel) channel);
                Snowflake id = Snowflake.of(binChannel);
            }

            for(MessageChannel channel : messageChannels) {
                System.out.println("CHANNEL ID: " + channel.getId());
            }

            System.out.println("Bin Channels Size: " + binChannels.size() + " Message Channels size: " + messageChannels.size());
            //sets house datetime to dev channel for if im devving on a tuesday
            DateTime date;
            if (System.getProperty("os.name").startsWith("Windows")) {
                date = new DateTime(binChannels, embeds, true, gateway);
            } else {
                date = new DateTime(binChannels, embeds, false, gateway);
            }

            commands.put("poll", event -> poll.createPoll(event.getMember().orElse(null), event.getMessage().getContent(), "", event.getMessage().getAttachments(), gateway, event.getMessage().getChannelId()).and(event.getMessage().delete().onErrorResume(throwable -> Mono.empty()))
                    .then());

            commands.put("join", event -> Mono.justOrEmpty(event.getMember())
                    .flatMap(Member::getVoiceState)
                    .flatMap(VoiceState::getChannel)
                    // join returns a VoiceConnection which would be required if we were
                    // adding disconnection features, but for now we are just ignoring it.
                    .flatMap(channel -> channel.join(spec -> spec.setProvider(musicMap.get(event.getGuildId().get()).getProvider())))
                    .then());

            commands.put("play", event -> Mono.justOrEmpty(event.getMessage().getContent())
                    .doOnNext(command -> musicMap.get(event.getGuildId().get()).playTrack(event.getMessage().getContent(), event))
                    .then());

            commands.put("skip", event -> event.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage("**" + event.getMember().get().getDisplayName() + "** has skipped " + musicMap.get(event.getGuildId().get()).skipTrack()))
                    .then());

            commands.put("volume", event -> event.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage("**" + event.getMember().get().getDisplayName() + musicMap.get(event.getGuildId().get()).setVolume(event.getMessage().getContent())))
                    .then());

            DateTime finalDate = date;
            commands.put("time", event -> event.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage("```The date is: " + String.valueOf(finalDate.getDate()) + "\n\nThe time is: " + String.valueOf(finalDate.getTime()) + "```").withMessageReference(event.getMessage().getId()))
                    .then());

            commands.put("bins", event -> event.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage(Binformation.binWeekCalculator(finalDate.getDateWeek(), finalDate.getDateWeekDay(), finalDate.getDateDayHour(), embeds, false)).withMessageReference(event.getMessage().getId()))
                    .then());

            commands.put("setreminders", event -> event.getMessage().getChannel()
                    .flatMap(channel -> {
                        try {
                            return channel.createMessage(Binformation.addChannelToDatabase(dataSource.getDatabaseConnection(), event, embeds));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .then());

            commands.put("delete", event -> event.getMessage().getChannel()
                    .flatMap(channel -> {
                        DiscordUtilities.deleteMessage(event, gateway);
                        return Mono.empty();
                    })
                    .then());

                    Mono<Void> populateMusicMap = gateway.getGuilds()
                            .map(guild -> musicMap.put(guild.getId(), new Music(youtubeSearch)))
                            .onErrorResume(throwable -> {
                                throwable.printStackTrace();
                                return Mono.empty();
                            })
                            .then();

                    Mono<Void> updatePresenceMono = gateway.updatePresence(ClientPresence.online(ClientActivity.watching(status))).then();

                    Mono<Void> createApplicationCommandsMono = ApplicationCommandRegistry.registerApplicationCommands(gateway);

                    client.gateway().setEnabledIntents(IntentSet.all());
                    Mono<Void> messageHandler = gateway.getEventDispatcher().on(MessageCreateEvent.class)
                            .flatMap(event -> Mono.just(event.getMessage().getContent())
                                    .flatMap(content -> Flux.fromIterable(commands.entrySet())
                                            // We will be using ; as our "prefix" to any command in the system.
                                            .filter(entry -> {
                                                if (System.getProperty("os.name").startsWith("Windows")) {
                                                    return content.startsWith(':' + entry.getKey());
                                                } else {
                                                    return content.startsWith(';' + entry.getKey());
                                                }

                                            })
                                            .flatMap(entry -> entry.getValue().execute(event))
                                            .next()))
                            .then();

                    Mono<Void> reactionAddManager = gateway.getEventDispatcher().on(ReactionAddEvent.class)
                            .flatMap(event -> event.getMessage().flatMap(message -> poll.updatePoll(event.getMessage(), event.getUserId(), event.getEmoji())))
                            .then();

                    Mono<Void> reactionRemoveManager = gateway.getEventDispatcher().on(ReactionRemoveEvent.class)
                            .flatMap(event -> event.getMessage().flatMap(message -> poll.updatePoll(event.getMessage(), event.getUserId(), event.getEmoji())))
                            .then();

                    //Happens when the bot is added to a guild
                    Mono<Void> guildCreateManager = gateway.getEventDispatcher().on(GuildCreateEvent.class)
                            .flatMap(event -> {
                                Snowflake guildSnowflake = event.getGuild().getId();
                                if (!musicMap.containsKey(guildSnowflake)) {
                                    musicMap.put(guildSnowflake, new Music(youtubeSearch));
                                }
                                return Mono.empty();
                            })
                            .then();

            Mono<Void> actOnSlashCommand = gateway.on(new ReactiveEventAdapter() {
                @Override
                public Publisher<?> onChatInputInteraction(ChatInputInteractionEvent event) {
                    Mono<Void> deferMono = event.deferReply().withEphemeral(true);
                    Mono<Void> editMono = event.editReply("There has been an issue processing your command, please try again!").then();

                    Member member = event.getInteraction().getMember().orElse(null);
                    if (member == null) {
                        editMono = event.editReply(DM_ERROR).then();
                        return deferMono.then(editMono);
                    }
                    String memberID = event.getInteraction().getMember().get().getId().asString();

                    String commandName = event.getCommandName();
                    Snowflake channelSnowflake = event.getInteraction().getChannelId();
                    Snowflake guildSnowflake = event.getInteraction().getGuildId().get();
                    String result = null;

                    //Member member, String messageContent, List<Attachment> attachments, GatewayDiscordClient gateway, Snowflake channelSnowflake
                    if (commandName.equals("poll")) {
                        //TODO: Turn questions and options into suitable message content alternative
                        //TODO: put attachment into list

                        String question = "";
                        String options = "";
                        Attachment attachment = null;
                        String description = null;
                        for (int i = 0; i < event.getOptions().size(); i++) {
                            ApplicationCommandInteractionOption option = event.getOptions().get(i);
                            String optionName = option.getName();
                            if (optionName.startsWith("option")) {
                              options = options.concat(("\"").concat(option.getValue().get().asString()).concat("\""));
                            } else if (optionName.equals("question")) {
                                question = ("\"").concat(option.getValue().get().asString()).concat("\"");
                            } else if (optionName.equals("description")) {
                                description = option.getValue().get().asString();
                            } else if (optionName.equals("image")) {
                                String attachmentRaw = option.getValue().get().getRaw();
                                Snowflake attachmentSnowflake = Snowflake.of(attachmentRaw);
                                attachment = event.getInteraction().getCommandInteraction().get().getResolved().get().getAttachment(attachmentSnowflake).get();
                            }
                        }
                        List<Attachment> attachments = null;
                        if (attachment != null) {
                            attachments = List.of(attachment);
                        }

                        String messageContent = question.concat(options);

                        Mono<Void> createPollMono = poll.createPoll(member, messageContent, description, attachments, gateway, channelSnowflake);
                        editMono =  event.editReply("Your poll has been created!").and(createPollMono);
                    }

                    return deferMono.then(editMono);
                }
            }).then();

                    return populateMusicMap.and(createApplicationCommandsMono).and(updatePresenceMono).and(messageHandler).and(reactionAddManager).and(reactionRemoveManager).and(guildCreateManager).and(actOnSlashCommand);
                });
        login.block();
    }
}
