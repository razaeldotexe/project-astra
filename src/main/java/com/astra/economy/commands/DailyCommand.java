package com.astra.economy.commands;

import com.astra.economy.service.CooldownException;
import com.astra.economy.service.DailyService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.text.NumberFormat;
import java.util.Locale;

public class DailyCommand implements SlashCommand {
    private final DailyService dailyService = new DailyService();

    @Override
    public String getName() {
        return "daily";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        try {
            long reward = dailyService.claimDaily(event.getUser().getId(), event.getGuild().getId());
            NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
            event.reply("✅ Selamat! Kamu telah mengklaim hadiah harian sebesar **Rp" + formatter.format(reward) + "**! 💸").queue();
        } catch (CooldownException e) {
            long hours = e.getRemainingTimeSeconds() / 3600;
            long minutes = (e.getRemainingTimeSeconds() % 3600) / 60;
            event.reply("⏳ Kamu sudah mengklaim hadiah harian! Tunggu **" + hours + " jam " + minutes + " menit** lagi.").setEphemeral(true).queue();
        }
    }
}
