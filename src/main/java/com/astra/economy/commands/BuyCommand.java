package com.astra.economy.commands;

import com.astra.economy.service.InsufficientBalanceException;
import com.astra.economy.service.ShopService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class BuyCommand implements SlashCommand {
    private final ShopService shopService = new ShopService();

    @Override
    public String getName() {
        return "buy";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        int itemId = event.getOption("item_id").getAsInt();

        try {
            shopService.buyItem(event.getUser().getId(), event.getGuild().getId(), itemId);
            event.reply("✅ Kamu berhasil membeli item ID **" + itemId + "**! Silakan cek `/inventory`.").queue();
        } catch (InsufficientBalanceException | IllegalArgumentException e) {
            event.reply("❌ " + e.getMessage()).setEphemeral(true).queue();
        }
    }
}
