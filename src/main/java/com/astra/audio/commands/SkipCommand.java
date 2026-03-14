package com.astra.audio.commands;

import com.astra.audio.GuildMusicManager;
import com.astra.audio.GuildMusicManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SkipCommand implements MusicCommand {
    @Override
    public String getName() { return "skip"; }

    @Override
    public String getDescription() { return "Skip lagu sekarang"; }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        handleSkip(event.getGuild(), event);
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        handleSkip(event.getGuild(), null);
        event.getChannel().sendMessage("⏭️ Lagu di-skip!").queue();
    }

    private void handleSkip(net.dv8tion.jda.api.entities.Guild guild, SlashCommandInteractionEvent slashEvent) {
        GuildMusicManager manager = PlayCommand.getOrCreate(guild);
        manager.getScheduler().skip();
        if (slashEvent != null) {
            slashEvent.reply("⏭️ Lagu di-skip!").queue();
        }
    }
}
