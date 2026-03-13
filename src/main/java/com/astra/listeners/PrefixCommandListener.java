package com.astra.listeners;

import com.astra.config.BotConfig;
import com.astra.economy.commands.CommandHandler;
import com.astra.economy.commands.EconomyCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PrefixCommandListener extends ListenerAdapter {
    private final CommandHandler economyHandler;
    private final com.astra.audio.commands.MusicCommandHandler musicHandler;

    public PrefixCommandListener(CommandHandler economyHandler, com.astra.audio.commands.MusicCommandHandler musicHandler) {
        this.economyHandler = economyHandler;
        this.musicHandler = musicHandler;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.isFromGuild()) return;

        String content = event.getMessage().getContentRaw();
        if (!content.startsWith(BotConfig.PREFIX)) return;

        String[] parts = content.substring(BotConfig.PREFIX.length()).split("\\s+");
        String commandName = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        if (musicHandler.handlePrefix(event, commandName, args)) {
            return;
        }

        EconomyCommand command = economyHandler.getCommand(commandName);
        if (command != null) {
            command.executePrefix(event, args);
        }
    }
}
