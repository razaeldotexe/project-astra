package com.astra.economy.commands;

import com.astra.economy.service.EconomyService;
import com.astra.economy.service.InsufficientBalanceException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.NumberFormat;
import java.util.Locale;

public class DepositCommand implements EconomyCommand {
    private final EconomyService economyService = new EconomyService();

    @Override
    public String getName() {
        return "deposit";
    }

    @Override
    public String getDescription() {
        return "Simpan uang dari dompet ke bank.";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        long amount = event.getOption("amount").getAsLong();
        if (amount <= 0) {
            event.reply("Jumlah deposit harus lebih dari 0.").setEphemeral(true).queue();
            return;
        }

        try {
            economyService.deposit(event.getUser().getId(), event.getGuild().getId(), amount);
            NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
            event.reply("✅ Berhasil menyimpan **Rp" + formatter.format(amount) + "** ke bank.").queue();
        } catch (InsufficientBalanceException e) {
            event.reply("❌ " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("Gunakan: `" + com.astra.config.BotConfig.PREFIX + "deposit <jumlah>`").queue();
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
            event.getChannel().sendMessage("Jumlah deposit harus lebih dari 0.").queue();
            return;
        }

        try {
            economyService.deposit(event.getAuthor().getId(), event.getGuild().getId(), amount);
            NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
            event.getChannel().sendMessage("✅ " + event.getAuthor().getName() + ", berhasil menyimpan **Rp" + formatter.format(amount) + "** ke bank.").queue();
        } catch (InsufficientBalanceException e) {
            event.getChannel().sendMessage("❌ " + e.getMessage()).queue();
        }
    }
}
