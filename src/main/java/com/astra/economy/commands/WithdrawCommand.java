package com.astra.economy.commands;

import com.astra.economy.service.EconomyService;
import com.astra.economy.service.InsufficientBalanceException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.NumberFormat;
import java.util.Locale;

public class WithdrawCommand implements EconomyCommand {
    private final EconomyService economyService = new EconomyService();

    @Override
    public String getName() {
        return "withdraw";
    }

    @Override
    public String getDescription() {
        return "Tarik uang dari bank ke dompet.";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        long amount = event.getOption("amount").getAsLong();
        if (amount <= 0) {
            event.reply("Jumlah withdraw harus lebih dari 0.").setEphemeral(true).queue();
            return;
        }

        try {
            economyService.withdraw(event.getUser().getId(), event.getGuild().getId(), amount);
            NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
            event.reply("✅ Berhasil menarik **Rp" + formatter.format(amount) + "** dari bank ke wallet.").queue();
        } catch (InsufficientBalanceException e) {
            event.reply("❌ " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("Gunakan: `" + com.astra.config.BotConfig.PREFIX + "withdraw <jumlah>`").queue();
            return;
        }

        long amount;
        try {
            amount = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("Jumlah harus berupa angka.").queue();
            return;
        }

        if (amount <= 0) {
            event.getChannel().sendMessage("Jumlah withdraw harus lebih dari 0.").queue();
            return;
        }

        try {
            economyService.withdraw(event.getAuthor().getId(), event.getGuild().getId(), amount);
            NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
            event.getChannel().sendMessage("✅ " + event.getAuthor().getName() + ", berhasil menarik **Rp" + formatter.format(amount) + "** dari bank ke wallet.").queue();
        } catch (InsufficientBalanceException e) {
            event.getChannel().sendMessage("❌ " + e.getMessage()).queue();
        }
    }
}
