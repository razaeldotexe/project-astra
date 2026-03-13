package com.astra.audio;

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.client.event.*;
import java.util.function.Consumer;

public class GuildMusicManager {
    private final long guildId;
    private final TrackScheduler scheduler;
    private final Link link;

    public GuildMusicManager(long guildId) {
        this.guildId = guildId;
        this.scheduler = new TrackScheduler(this);
        this.link = com.astra.audio.LavalinkManager.getClient().getOrCreateLink(guildId);

        // Register track end event via global client
        com.astra.audio.LavalinkManager.getClient().on(TrackEndEvent.class).subscribe(event -> {
            if (event.getGuildId() == guildId && event.getEndReason().getMayStartNext()) {
                scheduler.nextTrack();
            }
        });
    }

    public void play(Track track) {
        link.updatePlayer(update -> update.setTrack(track)).subscribe();
    }

    public void stop() {
        link.updatePlayer(update -> update.setTrack(null)).subscribe();
    }

    public void pause(boolean paused) {
        link.updatePlayer(update -> update.setPaused(paused)).subscribe();
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public Link getLink() {
        return link;
    }

    public long getGuildId() {
        return guildId;
    }
}
