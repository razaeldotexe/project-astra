package com.astra.economy.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler {
    private final Map<String, SlashCommand> commands = new HashMap<>();

    public CommandHandler() {
        registerCommand(new BalanceCommand());
        registerCommand(new DailyCommand());
        registerCommand(new WorkCommand());
        registerCommand(new DepositCommand());
        registerCommand(new WithdrawCommand());
        registerCommand(new PayCommand());
        registerCommand(new ShopCommand());
        registerCommand(new BuyCommand());
        registerCommand(new InventoryCommand());
        registerCommand(new LeaderboardCommand());
    }

    private void registerCommand(SlashCommand command) {
        commands.put(command.getName(), command);
    }

    public void handle(SlashCommandInteractionEvent event) {
        SlashCommand command = commands.get(event.getName());
        if (command != null) {
            command.execute(event);
            return;
        }

        // Handle Legacy Commands
        switch (event.getName()) {
            case "ping" -> LegacyCommands.handlePing(event);
            case "hello" -> LegacyCommands.handleHello(event);
            case "analisis" -> LegacyCommands.handleAnalisis(event);
            case "monitor-mc" -> LegacyCommands.handleMonitorMC(event);
            case "monitor-mc2" -> LegacyCommands.handleMonitorMC2(event);
        }
    }
}
