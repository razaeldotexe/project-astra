package com.astra.economy.commands;

import com.astra.economy.service.InsufficientBalanceException;
import com.astra.economy.service.ShopService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class BuyCommand implements EconomyCommand {
    private final ShopService shopService = new ShopService();

    @Override
    public String getName() {
        return "buy";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        int itemId = event.getOption("item_id").getAsInt();

        try {
            shopService.buyItem(event.getUser().getId(), event.getGuild().getId(), itemId);
            event.reply("✅ Kamu berhasil membeli item ID **" + itemId + "**! Silakan cek `/inventory`.").queue();
        } catch (InsufficientBalanceException | IllegalArgumentException e) {
            event.reply("❌ " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        if (args.length < 1) {
            event.getChannel().sendMessage("Gunakan: `" + com.astra.config.BotConfig.PREFIX + "buy <item_id>`").queue();
            return;
        }

        int itemId;
        try {
            itemId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("Item ID harus berupa angka.").queue();
            return;
        }

        try {
            shopService.buyItem(event.getAuthor().getId(), event.getGuild().getId(), itemId);
            event.getChannel().sendMessage("✅ " + event.getAuthor().getName() + ", kamu berhasil membeli item ID **" + itemId + "**! Silakan cek `" + com.astra.config.BotConfig.PREFIX + "inventory`.").queue();
        } catch (InsufficientBalanceException | IllegalArgumentException e) {
            event.getChannel().sendMessage("❌ " + e.getMessage()).queue();
        }
    }
}
