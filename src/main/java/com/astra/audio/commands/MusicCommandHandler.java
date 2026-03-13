package com.astra.audio.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.Map;

public class MusicCommandHandler {
    private final Map<String, MusicCommand> commands = new HashMap<>();

    public MusicCommandHandler() {
        registerCommand(new PlayCommand());
        registerCommand(new StopCommand());
        registerCommand(new SkipCommand());
        registerCommand(new PauseCommand());
        registerCommand(new QueueCommand());
    }

    private void registerCommand(MusicCommand command) {
        commands.put(command.getName(), command);
    }

    public MusicCommand getCommand(String name) {
        return commands.get(name);
    }

    public Map<String, MusicCommand> getCommands() {
        return commands;
    }

    public boolean handleSlash(SlashCommandInteractionEvent event) {
        MusicCommand command = commands.get(event.getName());
        if (command != null) {
            command.executeSlash(event);
            return true;
        }
        return false;
    }

    public boolean handlePrefix(MessageReceivedEvent event, String commandName, String[] args) {
        MusicCommand command = commands.get(commandName);
        if (command != null) {
            command.executePrefix(event, args);
            return true;
        }
        return false;
    }
}
