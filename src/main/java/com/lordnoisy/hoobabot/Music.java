package com.lordnoisy.hoobabot;

import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.entity.RestChannel;
import discord4j.voice.AudioProvider;
import reactor.core.publisher.Mono;

import java.util.*;

import java.util.Queue;

public class Music extends AudioEventAdapter {
    AudioProvider provider;
    TrackScheduler scheduler;
    // Creates AudioPlayer instances and translates URLs to AudioTrack instances
    final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    // Create an AudioPlayer so Discord4J can receive audio data
    final AudioPlayer player = playerManager.createPlayer();
    private RestChannel currentChannel;


    public AudioProvider getProvider() {
        return provider;
    }

    public void setProvider(AudioProvider provider) {
        this.provider = provider;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(TrackScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public AudioPlayer getPlayer() {
        return player;
    }
    private YoutubeSearch youtubeSearch;

    public Music(YoutubeSearch youtubeSearch) {
        // This is an optimization strategy that Discord4J can utilize. It is not important to understand
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        // Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager);
        // We will be creating LavaPlayerAudioProvider in the next step
        this.provider = new LavaPlayerAudioProvider(player);
        this.scheduler = new TrackScheduler(player, this);
        this.player.setVolume(50);
        this.player.addListener(scheduler);
        this.youtubeSearch = youtubeSearch;
    }

    public void playTrack(String media, MessageCreateEvent event){
        media = media.replaceFirst(":play ","");
        media = media.replaceFirst(";play ","");
        this.setCurrentChannel(event.getMessage().getRestChannel());
        String finalMedia = media;

        playerManager.loadItem(media, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                String userName = "";
                try {
                    userName = event.getMember().get().getNickname().get();
                } catch (Exception e) {
                    userName = event.getMember().get().getUsername();
                }
                if(player.getPlayingTrack()==null) {
                    getCurrentChannel().createMessage("**" + userName + "** added " + track.getInfo().title + " to the queue.").subscribe();
                    player.playTrack(track);
                } else {
                    scheduler.audioTrackQueue.add(track);
                    getCurrentChannel().createMessage("**" + userName + "** added " + track.getInfo().title + " to the queue.").subscribe();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                }
            }

            @Override
            public void noMatches() {
                playTrack(youtubeSearch.getYoutubeVideoToPlay(finalMedia), event);
                // Notify the user that we've got nothing
            }

            @Override
            public void loadFailed(FriendlyException throwable) {
                // Notify the user that everything exploded
            }
        });
    }

    public String skipTrack(){
        String title = player.getPlayingTrack().getInfo().title;
        player.getPlayingTrack().stop();
        getScheduler().playNextSong();
        return title;
    }

    public String setVolume(String input) {
        try {
            input = input.split(" ")[1];
            int volume = Integer.parseInt(input);
            player.setVolume(volume);
            return "** set the volume to " + input + "%.";
        } catch (Exception e) {
            e.printStackTrace();
            return "** failed to set the volume.";
        }
    }

    public void setCurrentChannel(RestChannel currentChannel) {
        this.currentChannel = currentChannel;
    }

    public RestChannel getCurrentChannel() {
        return currentChannel;
    }
}
