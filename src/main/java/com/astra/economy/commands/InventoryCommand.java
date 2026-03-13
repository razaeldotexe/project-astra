package com.astra.economy.commands;

import com.astra.economy.repository.InventoryRepository;
import com.astra.config.BotConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.List;

public class InventoryCommand implements EconomyCommand {
    private final InventoryRepository inventoryRepository = new InventoryRepository();

    @Override
    public String getName() {
        return "inventory";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        User target = event.getOption("user") != null ? event.getOption("user").getAsUser() : event.getUser();
        if (target.isBot()) {
            event.reply("Bot tidak memiliki inventaris.").setEphemeral(true).queue();
            return;
        }

        List<InventoryRepository.InventoryItem> items = inventoryRepository.getUserInventory(target.getId(), event.getGuild().getId());

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎒 Inventaris: " + target.getName())
                .setColor(BotConfig.EMBED_COLOR)
                .setThumbnail(target.getEffectiveAvatarUrl());

        if (items.isEmpty()) {
            embed.setDescription("Inventaris kosong. Gunakan `/shop` untuk membeli item!");
        } else {
            for (InventoryRepository.InventoryItem entry : items) {
                embed.addField(entry.item().getItemName(), "Jumlah: **" + entry.quantity() + "**\n*" + entry.item().getDescription() + "*", false);
            }
        }

        embed.setFooter("Economy Inventory", event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        User target = event.getMessage().getMentions().getUsers().isEmpty() ? 
                event.getAuthor() : event.getMessage().getMentions().getUsers().get(0);
        
        if (target.isBot()) {
            event.getChannel().sendMessage("Bot tidak memiliki inventaris.").queue();
            return;
        }

        List<InventoryRepository.InventoryItem> items = inventoryRepository.getUserInventory(target.getId(), event.getGuild().getId());

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎒 Inventaris: " + target.getName())
                .setColor(BotConfig.EMBED_COLOR)
                .setThumbnail(target.getEffectiveAvatarUrl());

        if (items.isEmpty()) {
            embed.setDescription("Inventaris kosong. Gunakan `" + BotConfig.PREFIX + "shop` untuk membeli item!");
        } else {
            for (InventoryRepository.InventoryItem entry : items) {
                embed.addField(entry.item().getItemName(), "Jumlah: **" + entry.quantity() + "**\n*" + entry.item().getDescription() + "*", false);
            }
        }

        embed.setFooter("Economy Inventory", event.getJDA().getSelfUser().getEffectiveAvatarUrl());
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
}
