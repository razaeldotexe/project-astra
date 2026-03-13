package com.astra.economy.commands;

import com.astra.config.BotConfig;
import com.astra.economy.model.UserEconomy;
import com.astra.economy.service.LeaderboardService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class LeaderboardCommand implements EconomyCommand {
    private final LeaderboardService leaderboardService = new LeaderboardService();

    @Override
    public String getName() {
        return "leaderboard";
    }

    @Override
    public String getDescription() {
        return "Lihat daftar user terkaya di server.";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        List<UserEconomy> tops = leaderboardService.getTopUsers(event.getGuild().getId(), 10);
        NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 Papan Peringkat Terkaya: " + event.getGuild().getName())
                .setColor(BotConfig.EMBED_COLOR);

        if (tops.isEmpty()) {
            embed.setDescription("Belum ada data ekonomi di server ini.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tops.size(); i++) {
                UserEconomy ue = tops.get(i);
                User user = event.getJDA().getUserById(ue.getUserId());
                String userName = user != null ? user.getName() : "Unknown User (" + ue.getUserId() + ")";
                
                sb.append(String.format("**%d.** %s — **Rp%s**\n", i + 1, userName, formatter.format(ue.getTotalBalance())));
            }
            embed.setDescription(sb.toString());
        }

        embed.setFooter("Top 10 Global Wallet + Bank", event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        List<UserEconomy> tops = leaderboardService.getTopUsers(event.getGuild().getId(), 10);
        NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 Papan Peringkat Terkaya: " + event.getGuild().getName())
                .setColor(BotConfig.EMBED_COLOR);

        if (tops.isEmpty()) {
            embed.setDescription("Belum ada data ekonomi di server ini.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tops.size(); i++) {
                UserEconomy ue = tops.get(i);
                User user = event.getJDA().getUserById(ue.getUserId());
                String userName = user != null ? user.getName() : "Unknown User (" + ue.getUserId() + ")";
                
                sb.append(String.format("**%d.** %s — **Rp%s**\n", i + 1, userName, formatter.format(ue.getTotalBalance())));
            }
            embed.setDescription(sb.toString());
        }

        embed.setFooter("Top 10 Global Wallet + Bank", event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}
