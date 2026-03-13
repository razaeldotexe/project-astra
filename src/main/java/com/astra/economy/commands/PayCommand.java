package com.astra.economy.commands;

import com.astra.economy.service.EconomyService;
import com.astra.economy.service.InsufficientBalanceException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.NumberFormat;
import java.util.Locale;

public class PayCommand implements EconomyCommand {
    private final EconomyService economyService = new EconomyService();

    @Override
    public String getName() {
        return "pay";
    }

    @Override
    public String getDescription() {
        return "Kirim uang ke user lain.";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
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

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        if (args.length < 2) {
            event.getChannel().sendMessage("Gunakan: `" + com.astra.config.BotConfig.PREFIX + "pay <@user> <jumlah>`").queue();
            return;
        }

        if (event.getMessage().getMentions().getUsers().isEmpty()) {
            event.getChannel().sendMessage("Kamu harus mention user yang ingin dikirim uang.").queue();
            return;
        }

        User target = event.getMessage().getMentions().getUsers().get(0);
        long amount;
        try {
            amount = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("Jumlah harus berupa angka.").queue();
            return;
        }

        if (target.isBot()) {
            event.getChannel().sendMessage("Kamu tidak bisa mengirim uang ke bot.").queue();
            return;
        }

        try {
            economyService.transfer(event.getAuthor().getId(), target.getId(), event.getGuild().getId(), amount);
            NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
            event.getChannel().sendMessage("💸 " + event.getAuthor().getName() + ", berhasil mengirim **Rp" + formatter.format(amount) + "** kepada " + target.getAsMention() + "!").queue();
        } catch (InsufficientBalanceException | IllegalArgumentException e) {
            event.getChannel().sendMessage("❌ " + e.getMessage()).queue();
        }
    }
}
