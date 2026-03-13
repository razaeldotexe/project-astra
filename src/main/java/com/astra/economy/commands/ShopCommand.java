package com.astra.economy.commands;

import com.astra.config.BotConfig;
import com.astra.economy.model.Item;
import com.astra.economy.service.ShopService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ShopCommand implements EconomyCommand {
    private final ShopService shopService = new ShopService();

    @Override
    public String getName() {
        return "shop";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        List<Item> items = shopService.getShopItems();
        NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🛒 Toko Item Astra")
                .setColor(BotConfig.EMBED_COLOR)
                .setDescription("Gunakan `/buy <id>` untuk membeli item.");

        for (Item item : items) {
            embed.addField(
                    "[" + item.getItemId() + "] " + item.getItemName() + " — Rp" + formatter.format(item.getPrice()),
                    item.getDescription(),
                    false
            );
        }

        embed.setFooter("Economy Shop", event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        List<Item> items = shopService.getShopItems();
        NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🛒 Toko Item Astra")
                .setColor(BotConfig.EMBED_COLOR)
                .setDescription("Gunakan `" + BotConfig.PREFIX + "buy <id>` untuk membeli item.");

        for (Item item : items) {
            embed.addField(
                    "[" + item.getItemId() + "] " + item.getItemName() + " — Rp" + formatter.format(item.getPrice()),
                    item.getDescription(),
                    false
            );
        }

        embed.setFooter("Economy Shop", event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}
