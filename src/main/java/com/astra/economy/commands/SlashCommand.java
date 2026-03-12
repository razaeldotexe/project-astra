package com.astra.economy.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface SlashCommand {
    String getName();
    void execute(SlashCommandInteractionEvent event);
}
