package com.astra.audio.commands;

import com.astra.audio.GuildMusicManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PauseCommand implements MusicCommand {
    private boolean paused = false;

    @Override
    public String getName() { return "pause"; }

    @Override
    public String getDescription() { return "Pause atau resume lagu"; }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        handlePause(event.getGuild(), paused -> {
            event.reply(paused ? "⏸️ Lagu di-pause." : "▶️ Lagu dilanjutkan.").queue();
        });
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        handlePause(event.getGuild(), paused -> {
            event.getChannel().sendMessage(paused ? "⏸️ Lagu di-pause." : "▶️ Lagu dilanjutkan.").queue();
        });
    }

    private void handlePause(net.dv8tion.jda.api.entities.Guild guild, java.util.function.Consumer<Boolean> callback) {
        GuildMusicManager manager = PlayCommand.getOrCreate(guild);
        manager.getLink().getPlayer().subscribe(player -> {
            boolean nextState = !player.getPaused();
            manager.pause(nextState);
            callback.accept(nextState);
        });
    }
}
