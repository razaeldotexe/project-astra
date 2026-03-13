package com.astra.economy.commands;

import com.astra.config.BotConfig;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class SetupCommand implements EconomyCommand {
    private final Dotenv dotenv = Dotenv.load();

    @Override
    public String getName() {
        return "setup";
    }

    @Override
    public String getDescription() {
        return "Setup bot features (Verify, etc)";
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("setup")) return;

        String subcommand = event.getSubcommandName();
        if (subcommand == null) return;

        if (subcommand.equals("verify")) {
            handleVerifySetup(event);
        }
    }

    private void handleVerifySetup(SlashCommandInteractionEvent event) {
        String channelId = dotenv.get("VERIFY_CHANNEL_ID");
        if (channelId == null || channelId.isEmpty()) {
            event.reply("❌ Error: `VERIFY_CHANNEL_ID` tidak ditemukan di `.env`.").setEphemeral(true).queue();
            return;
        }

        TextChannel channel = event.getGuild().getTextChannelById(channelId);
        if (channel == null) {
            event.reply("❌ Error: Channel dengan ID `" + channelId + "` tidak ditemukan.").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🛡️ Verifikasi Member")
                .setDescription("Klik tombol di bawah ini untuk mendapatkan akses ke server.")
                .setColor(BotConfig.EMBED_COLOR)
                .setFooter(BotConfig.FOOTER_TEXT, event.getJDA().getSelfUser().getEffectiveAvatarUrl());

        channel.sendMessageEmbeds(embed.build())
                .addActionRow(Button.primary("verify_button", "Verify"))
                .queue();

        event.reply("✅ Pesan verifikasi telah dikirim ke " + channel.getAsMention()).setEphemeral(true).queue();
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        // Setup command typically only available as Slash Command for security/subcommand convenience
        event.getChannel().sendMessage("Gunakan `/setup verify` untuk mengatur verifikasi.").queue();
    }
}
