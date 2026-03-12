package com.astra.economy.commands;

import com.astra.economy.service.EconomyService;
import com.astra.economy.service.InsufficientBalanceException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.text.NumberFormat;
import java.util.Locale;

public class PayCommand implements SlashCommand {
    private final EconomyService economyService = new EconomyService();

    @Override
    public String getName() {
        return "pay";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        User target = event.getOption("user").getAsUser();
        long amount = event.getOption("amount").getAsLong();

        if (target.isBot()) {
            event.reply("Kamu tidak bisa mengirim uang ke bot.").setEphemeral(true).queue();
            return;
        }

        try {
            economyService.transfer(event.getUser().getId(), target.getId(), event.getGuild().getId(), amount);
            NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
            event.reply("💸 Berhasil mengirim **Rp" + formatter.format(amount) + "** kepada " + target.getAsMention() + "!").queue();
        } catch (InsufficientBalanceException | IllegalArgumentException e) {
            event.reply("❌ " + e.getMessage()).setEphemeral(true).queue();
        }
    }
}
