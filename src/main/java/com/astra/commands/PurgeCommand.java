package com.astra.commands;

import com.astra.handlers.PurgeHandler;
import com.astra.utils.MessageFilter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PurgeCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("purge")) return;

        // Cek permission pengguna
        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.reply("❌ Kamu tidak memiliki permission **Manage Messages**.")
                .setEphemeral(true).queue();
            return;
        }

        // Cek permission bot
        if (!event.getGuild().getSelfMember()
                .hasPermission(event.getGuildChannel(), Permission.MESSAGE_MANAGE)) {
            event.reply("❌ Bot tidak memiliki permission **Manage Messages** di channel ini.")
                .setEphemeral(true).queue();
            return;
        }

        TextChannel channel = event.getChannel().asTextChannel();
        String subcommand = event.getSubcommandName();

        // Defer reply karena bulk delete bisa memakan waktu
        event.deferReply(true).queue();

        switch (subcommand) {
            case "all" -> {
                int amount = (int) event.getOption("jumlah").getAsLong();
                List<Message> messages = PurgeHandler.fetchFilteredMessages(
                    channel, amount, MessageFilter.all());
                PurgeHandler.deleteMessages(event, channel, messages, "semua");
            }
            case "bot" -> {
                int amount = (int) event.getOption("jumlah").getAsLong();
                List<Message> messages = PurgeHandler.fetchFilteredMessages(
                    channel, amount, MessageFilter.bot());
                PurgeHandler.deleteMessages(event, channel, messages, "bot");
            }
            case "embed" -> {
                int amount = (int) event.getOption("jumlah").getAsLong();
                List<Message> messages = PurgeHandler.fetchFilteredMessages(
                    channel, amount, MessageFilter.embed());
                PurgeHandler.deleteMessages(event, channel, messages, "embed");
            }
            case "links" -> {
                int amount = (int) event.getOption("jumlah").getAsLong();
                List<Message> messages = PurgeHandler.fetchFilteredMessages(
                    channel, amount, MessageFilter.links());
                PurgeHandler.deleteMessages(event, channel, messages, "links");
            }
            case "images" -> {
                int amount = (int) event.getOption("jumlah").getAsLong();
                List<Message> messages = PurgeHandler.fetchFilteredMessages(
                    channel, amount, MessageFilter.images());
                PurgeHandler.deleteMessages(event, channel, messages, "gambar");
            }
            case "user" -> {
                int amount = (int) event.getOption("jumlah").getAsLong();
                User targetUser = event.getOption("target").getAsUser();
                List<Message> messages = PurgeHandler.fetchFilteredMessages(
                    channel, amount, MessageFilter.fromUser(targetUser));
                PurgeHandler.deleteMessages(event, channel, messages,
                    "dari @" + targetUser.getName());
            }
            case "mentions" -> {
                int amount = (int) event.getOption("jumlah").getAsLong();
                List<Message> messages = PurgeHandler.fetchFilteredMessages(
                    channel, amount, MessageFilter.mentions());
                PurgeHandler.deleteMessages(event, channel, messages, "mention");
            }
            case "pinned" -> {
                int amount = (int) event.getOption("jumlah").getAsLong();
                List<Message> messages = PurgeHandler.fetchFilteredMessages(
                    channel, amount, MessageFilter.pinned());
                for (Message msg : messages) {
                    try {
                        msg.unpin().complete();
                    } catch (Exception ignored) {}
                }
                PurgeHandler.deleteMessages(event, channel, messages, "pinned");
            }
        }
    }
}
