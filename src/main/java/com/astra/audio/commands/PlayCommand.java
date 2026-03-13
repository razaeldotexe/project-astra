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

        String searchQuery = query.startsWith("http") ? query : "ytsearch:" + query;

        link.loadItem(searchQuery).subscribe(result -> {
            try {
                if (result instanceof TrackLoaded) {
                    Track track = ((TrackLoaded) result).getTrack();
                    musicManager.getScheduler().queue(track);
                    reply(channel, slashEvent, "▶️ Memutar: **" + track.getInfo().getTitle() + "**");
                } else if (result instanceof PlaylistLoaded) {
                    PlaylistLoaded playlistLoaded = (PlaylistLoaded) result;
                    List<Track> tracks = playlistLoaded.getTracks();
                    tracks.forEach(t -> musicManager.getScheduler().queue(t));
                    reply(channel, slashEvent, "📋 Playlist dimuat: **" + playlistLoaded.getInfo().getName() + "** (" + tracks.size() + " lagu)");
                } else if (result instanceof SearchResult) {
                    List<Track> tracks = ((SearchResult) result).getTracks();
                    if (tracks.isEmpty()) {
                        reply(channel, slashEvent, "❌ Lagu tidak ditemukan!");
                    } else {
                        Track track = tracks.get(0);
                        musicManager.getScheduler().queue(track);
                        reply(channel, slashEvent, "▶️ Memutar: **" + track.getInfo().getTitle() + "**");
                    }
                } else if (result instanceof NoMatches) {
                    reply(channel, slashEvent, "❌ Tidak ada hasil ditemukan.");
                } else if (result instanceof LoadFailed) {
                    reply(channel, slashEvent, "❌ Error saat memuat lagu: " + ((LoadFailed) result).getException().getMessage());
                }
            } catch (Exception e) {
                reply(channel, slashEvent, "❌ Terjadi kesalahan internal: " + e.getMessage());
            }
        }, throwable -> {
            reply(channel, slashEvent, "❌ Gagal menghubungi server musik: " + throwable.getMessage());
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
