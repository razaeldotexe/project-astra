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
}
