package com.astra.economy.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface EconomyCommand {
    String getName();
    void executeSlash(SlashCommandInteractionEvent event);
    void executePrefix(MessageReceivedEvent event, String[] args);
}
