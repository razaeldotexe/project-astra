package com.astra.economy.commands;

import com.astra.economy.service.EconomyService;
import com.astra.economy.service.InsufficientBalanceException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.text.NumberFormat;
import java.util.Locale;

public class WithdrawCommand implements SlashCommand {
    private final EconomyService economyService = new EconomyService();

    @Override
    public String getName() {
        return "withdraw";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
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
}
