package com.astra.economy.commands;

import com.astra.economy.service.CooldownException;
import com.astra.economy.service.WorkService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.text.NumberFormat;
import java.util.Locale;

public class WorkCommand implements EconomyCommand {
    private final WorkService workService = new WorkService();

    @Override
    public String getName() {
        return "work";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        try {
            WorkService.WorkResult result = workService.work(event.getUser().getId(), event.getGuild().getId());
            NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
            event.reply("💼 Kamu bekerja sebagai **" + result.job() + "** dan mendapatkan gaji sebesar **Rp" + formatter.format(result.reward()) + "**! 🚀").queue();
        } catch (CooldownException e) {
            long minutes = e.getRemainingTimeSeconds() / 60;
            long seconds = e.getRemainingTimeSeconds() % 60;
            event.reply("⏳ Kamu lelah bekerja. Istirahatlah sejenak selama **" + minutes + " menit " + seconds + " detik** lagi.").setEphemeral(true).queue();
        }
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        try {
            WorkService.WorkResult result = workService.work(event.getAuthor().getId(), event.getGuild().getId());
            NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
            event.getChannel().sendMessage("💼 " + event.getAuthor().getName() + ", kamu bekerja sebagai **" + result.job() + "** dan mendapatkan gaji sebesar **Rp" + formatter.format(result.reward()) + "**! 🚀").queue();
        } catch (CooldownException e) {
            long minutes = e.getRemainingTimeSeconds() / 60;
            long seconds = e.getRemainingTimeSeconds() % 60;
            event.getChannel().sendMessage("⏳ " + event.getAuthor().getName() + ", kamu lelah bekerja. Istirahatlah sejenak selama **" + minutes + " menit " + seconds + " detik** lagi.").queue();
        }
    }
}
