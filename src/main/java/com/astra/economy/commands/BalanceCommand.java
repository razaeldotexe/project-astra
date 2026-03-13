package com.astra.economy.commands;

import com.astra.config.BotConfig;
import com.astra.economy.model.UserEconomy;
import com.astra.economy.service.EconomyService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.Locale;

public class BalanceCommand implements SlashCommand {
    private final EconomyService economyService = new EconomyService();

    @Override
    public String getName() {
        return "balance";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        User targetUser = event.getOption("user") != null ? event.getOption("user").getAsUser() : event.getUser();
        if (targetUser.isBot()) {
            event.reply("Bot tidak memiliki saldo ekonomi.").setEphemeral(true).queue();
            return;
        }

        UserEconomy userEconomy = economyService.getOrCreateUser(targetUser.getId(), event.getGuild().getId());
        NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("💰 Saldo Ekonomi: " + targetUser.getName())
                .setColor(BotConfig.EMBED_COLOR)
                .setThumbnail(targetUser.getEffectiveAvatarUrl())
                .addField("💵 Wallet", "Rp" + formatter.format(userEconomy.getBalance()), true)
                .addField("🏦 Bank", "Rp" + formatter.format(userEconomy.getBankBalance()), true)
                .addField("📊 Total", "Rp" + formatter.format(userEconomy.getTotalBalance()), false)
                .setFooter("Astra Economy System", event.getJDA().getSelfUser().getEffectiveAvatarUrl());

        event.replyEmbeds(embed.build()).queue();
    }
}
