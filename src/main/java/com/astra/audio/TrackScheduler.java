package com.astra.audio;

import dev.arbjerg.lavalink.client.protocol.Track;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.LinkedList;
import java.util.Queue;

public class TrackScheduler {
    private final Queue<Track> queue = new LinkedList<>();
    private Track currentTrack;
    private final GuildMusicManager musicManager;
    private MessageChannel messageChannel;

    public TrackScheduler(GuildMusicManager musicManager) {
        this.musicManager = musicManager;
    }

    public void setMessageChannel(MessageChannel channel) {
        this.messageChannel = channel;
    }

    public void queue(Track track) {
        if (currentTrack == null) {
            currentTrack = track;
            musicManager.play(track);
        } else {
            queue.add(track);
            if (messageChannel != null)
                messageChannel.sendMessage("📋 Ditambahkan ke queue: **" + track.getInfo().getTitle() + "**").queue();
        }
    }

    public void nextTrack() {
        currentTrack = queue.poll();
        if (currentTrack != null) {
            musicManager.play(currentTrack);
            if (messageChannel != null)
                messageChannel.sendMessage("▶️ Sekarang memutar: **" + currentTrack.getInfo().getTitle() + "**").queue();
        } else {
            if (messageChannel != null)
                messageChannel.sendMessage("✅ Queue selesai!").queue();
            musicManager.stop();
        }
    }

    public void skip() {
        nextTrack();
    }

    public Queue<Track> getQueue() {
        return queue;
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public void setCurrentTrack(Track track) {
        this.currentTrack = track;
    }

    public void clearQueue() {
        queue.clear();
    }
}
