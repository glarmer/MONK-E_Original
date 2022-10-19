package com.lordnoisy.hoobabot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.intent.IntentSet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.util.*;

public final class Main {

    private static final Map<String, Command> commands = new HashMap<>();
    private static MessageOfTheDay motd = new MessageOfTheDay();
    private static final String status = "for ;help";
    private static final ModuleFinder moduleFinder = new ModuleFinder();
    private static ArrayList<MessageChannel> messageChannels = new ArrayList<>();

    static {
        commands.put("test", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("howdy").withMessageReference(event.getMessage().getId()))
                .then());
    }

    public static DataSource dataSource = null;

    public static void main(final String[] args) throws SQLException {
        String token = args[0];
        String url = args[1];
        String user = args[2];
        String password = args[3];
        YoutubeHttpContextFilter.setPAPISID(args[4]);
        YoutubeHttpContextFilter.setPSID(args[5]);
        String xRapidKey = args[6];
        String shortenerURL = args[7];
        String shortenerSignature = args[8];
        String shortenerUsername = args[9];
        String shortenerPassword = args[10];
        String weatherKey = args[11];
        String googleAPIKey = args[12];
        String bingAPIKey = args[13];

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

        commands.put("poll", event -> poll.createPoll(event)
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

        commands.put("monkey", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(monkey.monkeyCommand(event.getMessage().getContent())).withMessageReference(event.getMessage().getId()))
                .then());

        commands.put("video", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage(youtubeSearch.getVideoURL(event.getMessage().getContent())).withMessageReference(event.getMessage().getId()))
                .then());

        //Start timer
        System.out.println("Timer started");

        DiscordClient client = DiscordClient.create(token);

        Mono<Void> login = client.gateway().setEnabledIntents(IntentSet.all()).withGateway((GatewayDiscordClient gateway) -> {


            ArrayList<String> binChannels = null;
            try {
                binChannels = Binformation.getChannelsFromDatabase(dataSource.getDatabaseConnection());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < binChannels.size(); i++) {
                System.out.println("Bin Channels: " + binChannels.get(i));
                var channel = gateway.getChannelById(Snowflake.of(binChannels.get(i))).block();
                messageChannels.add((MessageChannel) channel);
                Snowflake id = Snowflake.of(binChannels.get(i));

            }


            System.out.println("Bin Channels Size: " + binChannels.size() + " Message Channels size: " + messageChannels.size());
            //sets house datetime to dev channel for if im devving on a tuesday
            DateTime date;
            if (System.getProperty("os.name").startsWith("Windows")) {
                date = new DateTime(binChannels, embeds, true, gateway);
            } else {
                date = new DateTime(binChannels, embeds, false, gateway);
            }

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

            //commands.put("done", event -> event.getMessage().getChannel()
            //        .flatMap(channel -> channel.createMessage(date.setIsBinsDone(true)).withMessageReference(event.getMessage().getId()))
            //        .then());

            DateTime finalDate = date;
            commands.put("time", event -> event.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage("```The date is: " + String.valueOf(finalDate.date) + "\n\nThe time is: " + String.valueOf(finalDate.time) + "```").withMessageReference(event.getMessage().getId()))
                    .then());

            commands.put("bins", event -> event.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage(Binformation.binWeekCalculator(finalDate.dateWeek, finalDate.dateWeekDay, finalDate.dateDayHour, embeds, false)).withMessageReference(event.getMessage().getId()))
                    .then());

            /*
                    commands.put("anyone", event -> event.getMessage().getChannel()
                            .flatMap(channel -> channel.createMessage(anyone.atAnyone(event)).withMessageReference(event.getMessage().getId()))
                            .then());

                     */

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

                    gateway.getGuilds().doOnEach(guild -> {
                        try {
                            musicMap.put(guild.get().getId(), new Music(youtubeSearch));
                            if (!serversAlreadyExist.contains(guild.get().getId().asString())){
                                MySQLUtilities.addServer(dataSource.getDatabaseConnection(), guild.get().getId().asString());
                            }
                        } catch (NullPointerException | SQLException npe) {
                            System.out.println("Continuing");
                        }
                    }).then().subscribe();

                    gateway.updatePresence(ClientPresence.online(ClientActivity.watching(status))).subscribe();
                    client.gateway().setEnabledIntents(IntentSet.all());

                    Mono<Void> MessageHandler = gateway.getEventDispatcher().on(MessageCreateEvent.class)
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

                    Mono<Void> ReactionAddManager = gateway.getEventDispatcher().on(ReactionAddEvent.class)
                            .flatMap(event -> event.getMessage().flatMap(message -> poll.updatePoll(event.getMessage(), event.getUserId(), event.getEmoji())))
                            .then();

                    Mono<Void> ReactionRemoveManager = gateway.getEventDispatcher().on(ReactionRemoveEvent.class)
                            .flatMap(event -> event.getMessage().flatMap(message -> poll.updatePoll(event.getMessage(), event.getUserId(), event.getEmoji())))
                            .then();

                    Mono<Void> GuildCreateManager = gateway.getEventDispatcher().on(GuildCreateEvent.class)
                            .flatMap(event -> {
                                Snowflake guildSnowflake = event.getGuild().getId();
                                if (!musicMap.containsKey(guildSnowflake)) {
                                    musicMap.put(guildSnowflake, new Music(youtubeSearch));
                                }
                                return Mono.empty();
                            })
                            .then();

                    return MessageHandler.and(ReactionAddManager).and(ReactionRemoveManager).and(GuildCreateManager);

                });



        login.block();

        System.out.println("Message disconnected");
    }
}
