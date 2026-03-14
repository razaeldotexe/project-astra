package com.astra.audio.commands;

import com.astra.audio.GuildMusicManager;
import com.astra.audio.GuildMusicManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StopCommand implements MusicCommand {
    @Override
    public String getName() { return "stop"; }

    @Override
    public String getDescription() { return "Stop musik dan hapus queue"; }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        handleStop(event.getGuild(), event);
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        handleStop(event.getGuild(), null);
        event.getChannel().sendMessage("⏹️ Musik dihentikan.").queue();
    }

    private void handleStop(net.dv8tion.jda.api.entities.Guild guild, SlashCommandInteractionEvent slashEvent) {
        GuildMusicManager manager = PlayCommand.getOrCreate(guild);
        manager.getScheduler().clearQueue();
        manager.stop();
        guild.getJDA().getDirectAudioController().disconnect(guild);
        if (slashEvent != null) {
            slashEvent.reply("⏹️ Musik dihentikan dan bot keluar dari voice channel.").queue();
        }
    }
}
