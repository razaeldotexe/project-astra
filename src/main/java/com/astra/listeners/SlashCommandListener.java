package com.astra.listeners;

import com.astra.economy.commands.CommandHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SlashCommandListener extends ListenerAdapter {
    private final CommandHandler economyHandler;
    private final com.astra.audio.commands.MusicCommandHandler musicHandler;

    public SlashCommandListener(CommandHandler economyHandler, com.astra.audio.commands.MusicCommandHandler musicHandler) {
        this.economyHandler = economyHandler;
        this.musicHandler = musicHandler;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (musicHandler.handleSlash(event)) {
            return;
        }
        economyHandler.handle(event);
    }
}
