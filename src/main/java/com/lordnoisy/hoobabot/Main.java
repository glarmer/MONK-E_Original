package com.lordnoisy.hoobabot;

import com.lordnoisy.hoobabot.game.Checkers;
import com.lordnoisy.hoobabot.game.TicTacToe;
import com.lordnoisy.hoobabot.music.Music;
import com.lordnoisy.hoobabot.registry.ApplicationCommandRegistry;
import com.lordnoisy.hoobabot.utility.*;
import com.lordnoisy.hoobabot.weather.WeatherReader;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.component.*;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionPresentModalSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.gateway.intent.IntentSet;
import net.sourceforge.tess4j.util.LoadLibs;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class Main {
    private static final Map<String, Command> commands = new HashMap<>();
    private static MessageOfTheDay motd = new MessageOfTheDay();
    private static final String status = "for /help";
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
    private static final String TWITCH_CLIENT_ID = "twitch_client_id";
    private static final String TWITCH_CLIENT_SECRET = "twitch_client_secret";

    public static final String LAST_SENT_GIVEAWAY = "do_not_edit_last_sent_giveaway";


    private static final String DM_ERROR = "This command can't be run in DMs!";

    public static void main(final String[] args) throws SQLException {
        File tmpFolder = LoadLibs.extractTessResources("win32-x86-64"); // replace platform
        System.setProperty("java.library.path", tmpFolder.getPath());


        System.out.println("MONK-E operating on: " + System.getProperty("os.name"));
        String path = new File(".").getAbsolutePath();
        path = path.substring(0, path.length()-1);
        String configPath = path + "monke.properties";

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
                        + BING_API_KEY_PROPERTY + PROPERTY_END + TWITCH_CLIENT_ID + PROPERTY_END + TWITCH_CLIENT_SECRET + PROPERTY_END + LAST_SENT_GIVEAWAY + PROPERTY_END);
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
        String twitchClientId = properties.getProperty(TWITCH_CLIENT_ID);
        String twitchClientSecret = properties.getProperty(TWITCH_CLIENT_SECRET);
        String lastSentGiveaway = properties.getProperty(LAST_SENT_GIVEAWAY);

        //YoutubeHttpContextFilter.setPAPISID(papsid);
        //YoutubeHttpContextFilter.setPSID(psid);

        dataSource = new DataSource(url,user,password);
        URLShortener shortener = new URLShortener(shortenerURL, shortenerSignature, shortenerUsername, shortenerPassword);
        WeatherReader weatherReader = new WeatherReader(weatherKey);
        WebImageSearch webImageSearch = new WebImageSearch(googleAPIKey, xRapidKey, bingAPIKey);
        EmbedBuilder embeds = new EmbedBuilder(webImageSearch);
        Monkey monkey = new Monkey(embeds);
        YoutubeSearch youtubeSearch = new YoutubeSearch(googleAPIKey);

        final GameGiveawayFollower gameGiveawayFollower = new GameGiveawayFollower(twitchClientId, twitchClientSecret, webImageSearch, lastSentGiveaway, properties);


        //Get all servers that already exist in the database
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

        commands.put("quote", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(motd.getMessageOfTheDay(embeds)).withMessageReference(event.getMessage().getId())
                        .flatMap(message -> message.edit(motd.getFinalMessageOfTheDay(embeds))))
                .then());

        commands.put("monkey", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(monkey.monkeyCommand(event.getMessage().getContent())).withMessageReference(event.getMessage().getId()))
                .then());

        DiscordClient client = DiscordClient.create(token);
        Mono<Void> login = client.gateway().setEnabledIntents(IntentSet.all()).withGateway((GatewayDiscordClient gateway) -> {
            //Add any servers that don't already exist in the database
            Mono<Void> addServersMono = gateway.getGuilds().flatMap(guild -> {
                if (!serversAlreadyExist.contains(guild.getId().asString())) {
                    try {
                        System.out.println("Adding server: " + guild.getId().asString());
                        MySQLUtilities.addServer(dataSource.getDatabaseConnection(), guild.getId().asString());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                return Mono.empty();
            }).then();

            ArrayList<String> binChannels;
            ArrayList<String> giveawayChannels;
            try {
                binChannels = Binformation.getChannelsFromDatabase(dataSource.getDatabaseConnection());
                giveawayChannels = gameGiveawayFollower.getChannelsFromDatabase(dataSource.getDatabaseConnection());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            //TODO: Remove the block, not that it matters too much
            for (String binChannel : binChannels) {
                System.out.println("Bin Channels: " + binChannel);
                var channel = gateway.getChannelById(Snowflake.of(binChannel)).block();
                messageChannels.add((MessageChannel) channel);
            }
            DateTime date;
            if (System.getProperty("os.name").startsWith("Windows")) {
                date = new DateTime(binChannels, embeds, true, gateway);
            } else {
                date = new DateTime(binChannels, embeds, false, gateway);
            }

            ArrayList<MessageChannel> giveawayMessageChannels = new ArrayList<>();
            for (String giveawayChannel : giveawayChannels) {
                System.out.println("Giveaway Channels: " + giveawayChannel);
                var channel = gateway.getChannelById(Snowflake.of(giveawayChannel)).block();
                giveawayMessageChannels.add((MessageChannel) channel);
            }

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Mono<Void> giveawaysMono = gameGiveawayFollower.checkForAndSendGiveaways(giveawayMessageChannels, false);
                    giveawaysMono.then().block();
                }
            }, 0, gameGiveawayFollower.getFrequency());

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

            DateTime dateTimeSource = date;
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String startDateTime = dateTimeSource.getDate().toString() + " " + dateTimeSource.getTime().now().format(timeFormatter);

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
                    .flatMap(event -> {
                        if (event.getUserId().equals(gateway.getSelfId())) {
                            return Mono.empty();
                        } else {
                            return event.getMessage().flatMap(message -> poll.updatePoll(event.getMessage(), null));
                        }
                    })
                    .then();

            Mono<Void> reactionRemoveManager = gateway.getEventDispatcher().on(ReactionRemoveEvent.class)
                    .flatMap(event -> {
                        if (event.getUserId().equals(gateway.getSelfId())) {
                            return Mono.empty();
                        } else {
                            return event.getMessage().flatMap(message -> poll.updatePoll(event.getMessage(), null));
                        }
                    })
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
                    String commandName = event.getCommandName();
                    Snowflake channelSnowflake = event.getInteraction().getChannelId();

                    Snowflake opponent = null;
                    if (event.getOption("opponent").isPresent() && event.getOption("opponent").get().getValue().isPresent()) {
                        opponent = event.getOption("opponent").get().getValue().get().asSnowflake();
                    }
                    Snowflake finalOpponent = opponent;

                    String question = null;
                    String description = null;
                    Attachment attachment = null;
                    String startDate = null;
                    int interval = 1;
                    int numberOfDays = 20;
                    List<Attachment> attachments = null;
                    Snowflake serverSnowflake = event.getInteraction().getGuildId().orElse(null);
                    switch(commandName) {
                        case "poll_dates":
                            for (int i = 0; i < event.getOptions().size(); i++) {
                                ApplicationCommandInteractionOption option = event.getOptions().get(i);
                                String optionName = option.getName();
                                switch (optionName) {
                                    case "question" -> question = option.getValue().get().asString();
                                    case "description" -> description = option.getValue().get().asString();
                                    case "image" -> {
                                        String attachmentRaw = option.getValue().get().getRaw();
                                        Snowflake attachmentSnowflake = Snowflake.of(attachmentRaw);
                                        attachment = event.getInteraction().getCommandInteraction().get().getResolved().get().getAttachment(attachmentSnowflake).get();
                                    }
                                    case "start_date" -> startDate = option.getValue().get().asString();
                                    case "interval" -> interval = (int) option.getValue().get().asLong();
                                    case "number_of_days" -> numberOfDays = (int) option.getValue().get().asLong();
                                }
                            }
                            if (attachment != null) {
                                attachments = List.of(attachment);
                            }

                            Mono<Void> createDatePollMono = poll.createDatePoll(member, question, description, attachments, gateway, channelSnowflake, numberOfDays, startDate, interval);
                            editMono =  event.editReply("Your poll has been created!").and(createDatePollMono);
                            break;
                        case "poll":
                            if (member == null) {
                                editMono = event.editReply(DM_ERROR).then();
                                return deferMono.then(editMono);
                            }
                            ArrayList<String> options = new ArrayList<>();
                            boolean isOpenPoll = false;
                            boolean hasOptions = true;
                            int numberOfOptions = -1;

                            for (int i = 0; i < event.getOptions().size(); i++) {
                                ApplicationCommandInteractionOption option = event.getOptions().get(i);
                                String optionName = option.getName();
                                if (optionName.startsWith("option")) {
                                    options.add(option.getValue().get().asString().concat(":"));
                                }
                                switch (optionName) {
                                    case "question" -> question = option.getValue().get().asString();
                                    case "description" -> description = option.getValue().get().asString();
                                    case "image" -> {
                                        String attachmentRaw = option.getValue().get().getRaw();
                                        Snowflake attachmentSnowflake = Snowflake.of(attachmentRaw);
                                        attachment = event.getInteraction().getCommandInteraction().get().getResolved().get().getAttachment(attachmentSnowflake).get();
                                    }
                                    case "open_poll" ->
                                            isOpenPoll = event.getOption("open_poll").get().getValue().get().asBoolean();
                                    case "empty_poll" ->
                                            hasOptions = !event.getOption("empty_poll").get().getValue().get().asBoolean();
                                    case "dates_poll" ->
                                            numberOfOptions = (int) event.getOption("dates_poll").get().getValue().get().asLong();
                                }
                            }

                            if (attachment != null) {
                                attachments = List.of(attachment);
                            }

                            Mono<Void> createPollMono = poll.createPoll(member, options, question, description, attachments, gateway, channelSnowflake, isOpenPoll, hasOptions);
                            editMono =  event.editReply("Your poll has been created!").and(createPollMono);
                            break;
                        case "uptime":
                            String currentDateTime = dateTimeSource.getDate().toString() + " " + dateTimeSource.getTime().now().format(timeFormatter);
                            editMono = event.editReply(
                                    "```\n" +
                                            "The current uptime is:          " + date.getUptime() +
                                            "\nBot running since:              " + startDateTime +
                                            "\nThe current time is:            " + currentDateTime +
                                            "```"
                                    ).then();
                            break;
                        case "ocr_image":
                            String url = event.getOptions().get(0).getValue().get().asString();

                            Mono<Void> ocrMono = gateway.getChannelById(event.getInteraction().getChannelId())
                                    .ofType(MessageChannel.class)
                                    .flatMap(channel -> channel.createMessage((new OCR(event, url, embeds)).doOCR()))
                                    .then();

                            return deferMono.then(event.deleteReply()).then(ocrMono);
                        case "foxify":
                            String message = event.getOption("message").get().getValue().get().asString();
                            String encoding = "The quick brown fox jumps over the lazy dog";
                            String secret = "Successfully encoded your message";
                            if (event.getOption("encoding").isPresent()) {
                                encoding = event.getOption("encoding").get().getValue().get().asString();
                            } else {
                                message = message.toLowerCase();
                            }
                            if (!Utilities.checkPresent(message, encoding)) {
                                secret = "Your encoding did not contain all the required letters so I switched it for you";
                                encoding = "The quick brown fox jumps over the lazy dog";
                            }
                            System.out.println("Message " + message);
                            String username = event.getInteraction().getMember().get().getDisplayName();
                            String newMessage = Utilities.encodeMessage(message, encoding);
                            String newNewMessage = username + " says:\n" + newMessage;
                            Mono<Void> encodingMono = gateway.getChannelById(event.getInteraction().getChannelId())
                                    .ofType(MessageChannel.class)
                                    .flatMap(channel -> channel.createMessage(newNewMessage).then());
                            editMono = event.editReply(secret).then(encodingMono);
                            break;
                        case "bin_config":
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
                                        Binformation.deleteServerFromDatabase(dataSource.getDatabaseConnection(), event.getInteraction().getMember().get().asFullMember(), serverSnowflake, embeds);
                                    } else {
                                        Binformation.addChannelToDatabase(dataSource.getDatabaseConnection(), event.getInteraction().getMember().get().asFullMember(), serverSnowflake, binChannelSnowflake, embeds);
                                    }
                                    editMono = event.editReply("Bins config editted successfully").then();
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            break;
                        case "giveaway_config":
                            Snowflake giveawayChannelSnowflake = null;
                            boolean deleteGiveawayConfig = false;
                            for (int i = 0; i < event.getOptions().size(); i++) {
                                ApplicationCommandInteractionOption option = event.getOptions().get(i);
                                String optionName = option.getName();
                                if (optionName.startsWith("giveaway_channel")) {
                                    giveawayChannelSnowflake = option.getValue().get().asSnowflake();
                                } else if (optionName.equals("delete_config")) {
                                    deleteGiveawayConfig = option.getValue().get().asBoolean();
                                }
                            }
                            if (serverSnowflake != null) {
                                EmbedCreateSpec embed;
                                try {
                                    if (deleteGiveawayConfig) {
                                        embed = gameGiveawayFollower.deleteServerFromDatabase(dataSource.getDatabaseConnection(), event.getInteraction().getMember().get().asFullMember(), serverSnowflake, embeds);
                                    } else {
                                        embed = gameGiveawayFollower.addChannelToDatabase(dataSource.getDatabaseConnection(), event.getInteraction().getMember().get().asFullMember(), serverSnowflake, giveawayChannelSnowflake, embeds);
                                    }
                                } catch (SQLException e) {
                                    embed = embeds.constructErrorEmbed();
                                }
                                EmbedCreateSpec finalEmbed = embed;
                                Mono<Void> giveawayConfigMono = gateway.getChannelById(event.getInteraction().getChannelId())
                                        .ofType(MessageChannel.class)
                                        .flatMap(channel -> channel.createMessage(finalEmbed))
                                        .then();
                                return deferMono.then(event.deleteReply()).then(giveawayConfigMono);
                            }
                            break;
                        case "video":
                            ApplicationCommandInteractionOption option = event.getOptions().get(0);


                            String userID = event.getInteraction().getMember().get().getId().asString();
                            Mono<Void> videoMono = gateway.getChannelById(event.getInteraction().getChannelId())
                                    .ofType(MessageChannel.class)
                                    .flatMap(channel -> channel.createMessage(youtubeSearch.getVideoMessage(option.getValue().get().asString(), userID)))
                                    .then();

                            return deferMono.then(event.deleteReply()).then(videoMono);
                        case "tic_tac_toe":
                            TicTacToe ticTacToe = new TicTacToe();
                            if (member != null) {
                                Mono<Void> ticTacToeMono = gateway.getChannelById(event.getInteraction().getChannelId())
                                        .ofType(MessageChannel.class)
                                        .flatMap(channel -> channel.createMessage(ticTacToe.startTicTacToe(member, finalOpponent)).then());
                                editMono = event.editReply("Prepare to battle!").then();
                                return deferMono.then(editMono).then(ticTacToeMono);
                            }
                            break;
                        case "checkers":
                            Checkers checkers = new Checkers();
                            if (member != null) {
                                Mono<Void> checkersMono = gateway.getChannelById(event.getInteraction().getChannelId())
                                        .ofType(MessageChannel.class)
                                        .flatMap(channel -> channel.createMessage(checkers.startCheckers(member, finalOpponent)).then());
                                editMono = event.editReply("Prepare to battle!").then();
                                return deferMono.then(editMono).then(checkersMono);
                            }
                            break;
                        case "help":
                            Mono<Void> helpMono = event.editReply().withEmbeds(embeds.constructHelpEmbed()).then();
                            return deferMono.then(helpMono);
                        case "bins":
                            Mono<Void> binMono = gateway.getChannelById(event.getInteraction().getChannelId())
                                    .ofType(MessageChannel.class)
                                    .flatMap(channel -> channel.createMessage(Binformation.binWeekCalculator(dateTimeSource.getDateWeek(), dateTimeSource.getDateWeekDay(), dateTimeSource.getDateDayHour(), embeds, false)))
                                    .then();
                            return deferMono.then(event.deleteReply()).then(binMono);
                        case "image":
                            editMono = event.deleteReply();
                            String search = null;
                            String engine = "google";
                            boolean gif = false;
                            if (event.getOption("search").isPresent() && event.getOption("search").get().getValue().isPresent()) {
                                search = event.getOption("search").get().getValue().get().asString();
                            }
                            if (event.getOption("engine").isPresent() && event.getOption("engine").get().getValue().isPresent()) {
                                engine = event.getOption("engine").get().getValue().get().asString();
                            }
                            if (event.getOption("gif").isPresent() && event.getOption("gif").get().getValue().isPresent()) {
                                gif = event.getOption("gif").get().getValue().get().asBoolean();
                            }

                            String searchFinal = search;
                            String engineFinal = engine;
                            boolean gifFinal = gif;


                            Mono<Object> imageMono = event.getInteraction().getChannel().flatMap(channel-> channel.createMessage(webImageSearch.doImageSearch(event, searchFinal, engineFinal, gifFinal)).withComponents(ActionRow.of(DiscordUtilities.deleteButton(event.getInteraction().getUser().getId()))));
                            return deferMono.then(editMono).and(imageMono);
                        case "test":
                            String testOption = event.getOptions().get(0).getValue().get().asString();
                            String commandInformation = event.getInteraction().getUser().getUsername().toString() + " ran a test command: \n";

                            Mono<Void> testMono;
                            RSSReader rss = new RSSReader();
                            switch (testOption) {
                                case "rss":
                                    testMono = gateway.getChannelById(event.getInteraction().getChannelId())
                                            .ofType(MessageChannel.class)
                                            .flatMap(channel -> channel.createMessage(commandInformation + rss.testRSSReader("https://lorem-rss.herokuapp.com/feed?length=1")))
                                            .then();
                                    break;
                                case "giveaways":
                                    testMono = gateway.getChannelById(event.getInteraction().getChannelId())
                                            .ofType(MessageChannel.class)
                                            .flatMap(channel -> channel.createMessage(gameGiveawayFollower.readGiveawaysFeed(2).get(0)))
                                            .then();
                                    ArrayList<MessageChannel> channels = new ArrayList<>();
                                    channels.add(event.getInteraction().getChannel().block());
                                    testMono = gameGiveawayFollower.checkForAndSendGiveaways(channels, true);
                                    break;
                                default:
                                    //Silly
                                    if (testOption.startsWith("speak#") & event.getInteraction().getMember().get().getId().asString().equals("1022286242123087952")) {
                                        String messageToSend = testOption.split("#")[1].strip();
                                        testMono = gateway.getChannelById(event.getInteraction().getChannelId())
                                                .ofType(MessageChannel.class)
                                                .flatMap(channel -> channel.createMessage(messageToSend))
                                                .then();
                                    } else {
                                        testMono = gateway.getChannelById(event.getInteraction().getChannelId())
                                                .ofType(MessageChannel.class)
                                                .flatMap(channel -> channel.createMessage(commandInformation + "Error: There is no test feature by this name."))
                                                .then();
                                        break;
                                    }
                            }

                            return deferMono.then(event.deleteReply()).then(testMono);
                    }

                    return deferMono.then(editMono);
                }
            }).then();

            Mono<Void> buttonListener = gateway.on(ButtonInteractionEvent.class, event -> {
                String buttonId = event.getCustomId();
                String[] buttonParts = buttonId.split(":");
                String authorId = null;
                if (buttonId.startsWith("delete:")) {
                    authorId = buttonParts[buttonParts.length-1];
                    buttonId = buttonParts[0];
                } else if (buttonId.startsWith("tic_tac_toe:")) {
                    authorId = buttonParts[buttonParts.length-1];
                    buttonId = buttonParts[0];
                } else if (buttonId.startsWith("c:")){
                    buttonId = buttonParts[0];
                }
                switch (buttonId) {
                    case "poll:add_option":
                        InteractionPresentModalSpec modal = InteractionPresentModalSpec.builder()
                                .title("Add an option to the poll!")
                                .customId(buttonId)
                                .addComponent(ActionRow.of(TextInput.small(buttonId, "Option", 1, 40)))
                                .build();

                        return event.presentModal(modal);
                    case "delete":
                        return DiscordUtilities.deleteMessage(event.getMessage().get(), event.getInteraction().getUser().getId(), authorId)
                                .flatMap(success -> {
                                    if (success) {
                                        return Mono.empty();
                                    } else {
                                        return event.reply("You can't delete someone else's message!").withEphemeral(true).then();
                                    }
                                });
                    case "tic_tac_toe":
                        buttonId = event.getCustomId();
                        TicTacToe ticTacToe = new TicTacToe();
                        MessageEditSpec ticTacToeEdit = ticTacToe.updateTicTacToe(event.getMessage().get(), buttonId, event.getInteraction().getUser().getId().asString());
                        if (ticTacToeEdit == null) {
                            return event.reply("You cannot do that!").withEphemeral(true);
                        }

                        Mono<Void> ticTacToeMono = event.getMessage().get().edit(ticTacToeEdit).then();
                        return ticTacToeMono.then(event.reply());
                    case "c":
                        System.out.println("BAZINGA C");
                        buttonId = event.getCustomId();
                        Checkers checkers = new Checkers();
                        if (buttonParts[1].length()==1) {
                            //First button click (picked x)
                            MessageEditSpec checkersEdit = checkers.getYVersion(buttonId, event.getMessage().get().getEmbeds().get(0));
                            Mono<Void> checkersYMono = event.getMessage().get().edit(checkersEdit).then();
                            return checkersYMono.then(event.reply());
                        } else if (buttonParts[1].length()==2) {
                            //Second button click (picked y)
                            System.out.println("BAZINGA A");
                            MessageEditSpec checkersEdit = checkers.getPossibleMovesVersion(buttonId, event.getMessage().get().getEmbeds().get(0));
                            Mono<Void> checkersMoveMono = event.getMessage().get().edit(checkersEdit).then();
                            return checkersMoveMono.then(event.reply());
                        }
                        break;
                }
                return event.reply("There has been an error responding, please try again!").withEphemeral(true);
            }).then();

            Mono<Void> modalListener = gateway.on(ModalSubmitInteractionEvent.class, event -> {
                Mono<Void> deferMono = event.deferReply().withEphemeral(true);
                Mono<Void> editMono = event.editReply("There has been an error processing your selection, please try again!").then();
                String modalId = event.getCustomId();
                String optionId = "poll:add_option";

                switch (modalId) {
                    case "poll:add_option":
                        for (TextInput textInput : event.getComponents(TextInput.class)) {
                            String inputID = textInput.getCustomId();
                            if (inputID.startsWith(optionId)) {
                                String optionToAdd = textInput.getValue().orElse(null);
                                if (optionToAdd != null) {
                                    //TODO: Logic for adding options
                                    Snowflake pollId = event.getMessage().get().getId();
                                    Snowflake channelId = event.getMessage().get().getChannelId();
                                    Mono<Message> message = gateway.getMessageById(channelId, pollId);
                                    editMono = event.editReply("Your option has been added successfully").then();
                                    return deferMono.then(editMono).and(poll.updatePoll(message, optionToAdd.concat(":")));
                                }
                            }
                        }
                        break;
                }

                return deferMono.then(editMono);
            }).then();

            return populateMusicMap.and(addServersMono).and(buttonListener).and(modalListener).and(createApplicationCommandsMono).and(updatePresenceMono).and(messageHandler).and(reactionAddManager).and(reactionRemoveManager).and(guildCreateManager).and(actOnSlashCommand);
        });
        login.block();
    }
}
