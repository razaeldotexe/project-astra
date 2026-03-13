package com.astra.economy.commands;

import com.astra.config.BotConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.Collection;

public class HelpCommand implements EconomyCommand {
    private final CommandHandler commandHandler;

    public HelpCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Menampilkan daftar semua perintah yang tersedia.";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = createHelpEmbed();
        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embed = createHelpEmbed();
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    private EmbedBuilder createHelpEmbed() {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📚 Daftar Perintah Astra")
                .setColor(BotConfig.EMBED_COLOR)
                .setDescription("Berikut adalah daftar perintah yang tersedia untuk sistem ekonomi.")
                .setFooter("Astra Economy Help", null);

        Collection<EconomyCommand> commands = commandHandler.getCommands().values();
        for (EconomyCommand cmd : commands) {
            String slashFormat = "`/" + cmd.getName() + "`";
            String prefixFormat = "`" + BotConfig.PREFIX + cmd.getName() + "`";
            embed.addField(
                    slashFormat + " | " + prefixFormat,
                    cmd.getDescription(),
                    false
            );
        }

        return embed;
    }
}
