package com.astra.audio.commands;

import com.astra.audio.GuildMusicManager;
import com.astra.audio.LavalinkManager;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayCommand implements MusicCommand {
    private static final Map<Long, GuildMusicManager> managers = new HashMap<>();

    public static GuildMusicManager getOrCreate(Guild guild) {
        return managers.computeIfAbsent(guild.getIdLong(), id -> new GuildMusicManager(id));
    }

    @Override
    public String getName() { return "play"; }

    @Override
    public String getDescription() { return "Putar musik dari URL atau nama lagu"; }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String query = event.getOption("query").getAsString();
        handlePlay(event.getGuild(), event.getMember(), event.getChannel(), query, event);
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            event.getChannel().sendMessage("❌ Masukkan URL atau nama lagu!").queue();
            return;
        }
        String query = String.join(" ", args);
        handlePlay(event.getGuild(), event.getMember(), event.getChannel(), query, null);
    }

    private void handlePlay(Guild guild, Member member, MessageChannel channel, String query, SlashCommandInteractionEvent slashEvent) {
        if (member == null) return;

        // Pre-check Lavalink node readiness
        if (LavalinkManager.getClient().getNodes().isEmpty()) {
            reply(channel, slashEvent, "❌ Server musik belum terkonfigurasi.");
            return;
        }

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || !voiceState.inAudioChannel()) {
            reply(channel, slashEvent, "❌ Kamu harus masuk ke voice channel dulu!");
            return;
        }

        guild.getJDA().getDirectAudioController().connect(voiceState.getChannel());

        GuildMusicManager musicManager = getOrCreate(guild);
        musicManager.getScheduler().setMessageChannel(channel);
        Link link = musicManager.getLink();
        
        // 1. Connect to voice channel first (JDA native)
        guild.getJDA().getDirectAudioController().connect(voiceState.getChannel());

        String searchQuery = query.startsWith("http") ? query : "ytsearch:" + query;

        // 2. Wait for player ready then load item
        link.getPlayer()
            .flatMap(player -> link.loadItem(searchQuery))
            .subscribe(result -> {
                try {
                    if (result instanceof TrackLoaded t) {
                        Track track = t.getTrack();
                        link.updatePlayer(update -> update.setTrack(track)).subscribe();
                        reply(channel, slashEvent, "🎵 Now playing: **" + track.getInfo().getTitle() + "**");
                    } else if (result instanceof PlaylistLoaded p) {
                        List<Track> tracks = p.getTracks();
                        if (!tracks.isEmpty()) {
                            Track track = tracks.get(0);
                            link.updatePlayer(update -> update.setTrack(track)).subscribe();
                            reply(channel, slashEvent, "🎵 Now playing: **" + track.getInfo().getTitle() + "** (from playlist: " + p.getInfo().getName() + ")");
                        }
                    } else if (result instanceof SearchResult s) {
                        List<Track> tracks = s.getTracks();
                        if (tracks.isEmpty()) {
                            reply(channel, slashEvent, "❌ No results found.");
                        } else {
                            Track track = tracks.get(0);
                            link.updatePlayer(update -> update.setTrack(track)).subscribe();
                            reply(channel, slashEvent, "🎵 Now playing: **" + track.getInfo().getTitle() + "**");
                        }
                    } else if (result instanceof NoMatches) {
                        reply(channel, slashEvent, "❌ No results found.");
                    } else if (result instanceof LoadFailed f) {
                        reply(channel, slashEvent, "❌ Error: " + f.getException().getMessage());
                    }
                } catch (Exception e) {
                    reply(channel, slashEvent, "❌ Terjadi kesalahan internal: " + e.getMessage());
                }
            }, error -> {
                reply(channel, slashEvent, "❌ Error: " + error.getMessage());
            });
    }

    private void reply(MessageChannel channel, SlashCommandInteractionEvent slashEvent, String content) {
        if (slashEvent != null) {
            slashEvent.getHook().sendMessage(content).queue();
        } else {
            channel.sendMessage(content).queue();
        }
    }
}
