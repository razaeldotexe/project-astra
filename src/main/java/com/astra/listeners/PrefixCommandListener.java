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
    private final CommandHandler commandHandler;
    private final Map<String, EconomyCommand> commands = new HashMap<>();

    public PrefixCommandListener(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
        // The commandHandler already has initialized commands, but they are private.
        // For simplicity in this implementation, we will use the commandHandler's 
        // internal map logic if possible, or re-register them here.
        // Looking at CommandHandler.java, it doesn't expose the map.
        // I will add a getter to CommandHandler instead.
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.isFromGuild()) return;

        String content = event.getMessage().getContentRaw();
        if (!content.startsWith(BotConfig.PREFIX)) return;

        String[] parts = content.substring(BotConfig.PREFIX.length()).split("\\s+");
        String commandName = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        EconomyCommand command = commandHandler.getCommand(commandName);
        if (command != null) {
            command.executePrefix(event, args);
        }
    }
}
