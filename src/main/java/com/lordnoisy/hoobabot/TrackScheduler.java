package com.lordnoisy.hoobabot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;

public final class TrackScheduler extends AudioEventAdapter {
    //TODO: JUST FIX ALL OF THIs

    private final AudioPlayer player;
    final Queue<AudioTrack> audioTrackQueue = new LinkedList<AudioTrack>();
    final Music music;

    public TrackScheduler(final AudioPlayer player, Music music) {
        this.player = player;
        this.music = music;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            try {
                this.player.playTrack(audioTrackQueue.remove());
            } catch (Exception e){
                System.out.println("Likely no video next in queue");
                e.printStackTrace();
            }
        }

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
        //                       clone of this back to your queue
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        music.getCurrentChannel().createMessage(track.getInfo().title + " has started playing.").subscribe();
    }

    public void playNextSong() {
        try {
            this.player.playTrack(audioTrackQueue.remove());
        } catch (Exception e){
            System.out.println("Likely no video next in queue");
            e.printStackTrace();
        }
    }


}
