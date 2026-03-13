package com.astra.audio;

import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.LinkedList;
import java.util.Queue;

public class TrackScheduler {
    private final Queue<Track> queue = new LinkedList<>();
    private Track currentTrack;
    private final GuildMusicManager musicManager;
    private TextChannel textChannel;

    public TrackScheduler(GuildMusicManager musicManager) {
        this.musicManager = musicManager;
    }

    public void setTextChannel(TextChannel channel) {
        this.textChannel = channel;
    }

    public void queue(Track track) {
        if (currentTrack == null) {
            currentTrack = track;
            musicManager.play(track);
        } else {
            queue.add(track);
            if (textChannel != null)
                textChannel.sendMessage("📋 Ditambahkan ke queue: **" + track.getInfo().getTitle() + "**").queue();
        }
    }

    public void nextTrack() {
        currentTrack = queue.poll();
        if (currentTrack != null) {
            musicManager.play(currentTrack);
            if (textChannel != null)
                textChannel.sendMessage("▶️ Sekarang memutar: **" + currentTrack.getInfo().getTitle() + "**").queue();
        } else {
            if (textChannel != null)
                textChannel.sendMessage("✅ Queue selesai!").queue();
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
