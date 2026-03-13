package com.astra.showcase.listeners;

import com.astra.config.BotConfig;
import com.astra.showcase.model.Project;
import com.astra.showcase.repository.ProjectRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.sql.SQLException;
import java.util.List;

public class ProjectListener extends ListenerAdapter {
    private final ProjectRepository repository = new ProjectRepository();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("project")) return;

        String subcommand = event.getSubcommandName();
        if (subcommand == null) return;

        try {
            switch (subcommand) {
                case "add" -> handleAddSlash(event);
                case "list" -> handleList(event);
                case "view" -> handleViewSlash(event);
                case "delete" -> handleDeleteSlash(event);
            }
        } catch (Exception e) {
            event.reply("❌ Terjadi kesalahan: " + e.getMessage()).setEphemeral(true).queue();
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.isFromGuild()) return;

        String content = event.getMessage().getContentRaw();
        if (!content.startsWith(BotConfig.PREFIX + "project")) return;

        String[] parts = content.substring((BotConfig.PREFIX + "project").length()).trim().split("\\s+", 2);
        String sub = parts.length > 0 ? parts[0].toLowerCase() : "";
        String args = parts.length > 1 ? parts[1] : "";

        try {
            switch (sub) {
                case "add" -> handleAddPrefix(event, args);
                case "list" -> handleListPrefix(event);
                case "view" -> handleViewPrefix(event, args);
                case "delete" -> handleDeletePrefix(event, args);
            }
        } catch (Exception e) {
            event.getChannel().sendMessage("❌ Terjadi kesalahan: " + e.getMessage()).queue();
            e.printStackTrace();
        }
    }

    // --- Add Project ---
    private void handleAddSlash(SlashCommandInteractionEvent event) throws SQLException {
        String name = event.getOption("name").getAsString();
        String desc = event.getOption("description").getAsString();
        String link = event.getOption("link").getAsString();
        String tags = event.getOption("tags") != null ? event.getOption("tags").getAsString() : "none";

        Project p = new Project(0, name, desc, link, tags, event.getUser().getId(), event.getGuild().getId(), 0, null);
        repository.addProject(p);
        event.reply("✅ Project **" + name + "** berhasil ditambahkan!").queue();
    }

    private void handleAddPrefix(MessageReceivedEvent event, String args) throws SQLException {
        // Format: !project add name | description | link | [tags]
        String[] parts = args.split("\\|");
        if (parts.length < 3) {
            event.getChannel().sendMessage("❌ Format salah! Gunakan: `" + BotConfig.PREFIX + "project add name | description | link | [tags]`").queue();
            return;
        }
        String name = parts[0].trim();
        String desc = parts[1].trim();
        String link = parts[2].trim();
        String tags = parts.length > 3 ? parts[3].trim() : "none";

        Project p = new Project(0, name, desc, link, tags, event.getAuthor().getId(), event.getGuild().getId(), 0, null);
        repository.addProject(p);
        event.getChannel().sendMessage("✅ Project **" + name + "** berhasil ditambahkan!").queue();
    }

    // --- List Project ---
    private void handleList(SlashCommandInteractionEvent event) throws SQLException {
        List<Project> projects = repository.getAllProjects(event.getGuild().getId());
        event.replyEmbeds(buildListEmbed(projects)).queue();
    }

    private void handleListPrefix(MessageReceivedEvent event) throws SQLException {
        List<Project> projects = repository.getAllProjects(event.getGuild().getId());
        event.getChannel().sendMessageEmbeds(buildListEmbed(projects)).queue();
    }

    // --- View Project ---
    private void handleViewSlash(SlashCommandInteractionEvent event) throws SQLException {
        String name = event.getOption("name").getAsString();
        Project p = repository.getProjectByName(name, event.getGuild().getId());
        if (p == null) {
            event.reply("❌ Project tidak ditemukan.").setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(buildDetailEmbed(p, event.getJDA())).queue();
    }

    private void handleViewPrefix(MessageReceivedEvent event, String args) throws SQLException {
        String name = args.trim();
        Project p = repository.getProjectByName(name, event.getGuild().getId());
        if (p == null) {
            event.getChannel().sendMessage("❌ Project tidak ditemukan.").queue();
            return;
        }
        event.getChannel().sendMessageEmbeds(buildDetailEmbed(p, event.getJDA())).queue();
    }

    // --- Delete Project ---
    private void handleDeleteSlash(SlashCommandInteractionEvent event) throws SQLException {
        String name = event.getOption("name").getAsString();
        Project p = repository.getProjectByName(name, event.getGuild().getId());
        if (p == null) {
            event.reply("❌ Project tidak ditemukan.").setEphemeral(true).queue();
            return;
        }
        if (!p.getOwnerId().equals(event.getUser().getId())) {
            event.reply("❌ Kamu hanya bisa menghapus project milikmu sendiri!").setEphemeral(true).queue();
            return;
        }
        repository.deleteProject(name, event.getUser().getId(), event.getGuild().getId());
        event.reply("🗑️ Project **" + name + "** telah dihapus.").queue();
    }

    private void handleDeletePrefix(MessageReceivedEvent event, String args) throws SQLException {
        String name = args.trim();
        Project p = repository.getProjectByName(name, event.getGuild().getId());
        if (p == null) {
            event.getChannel().sendMessage("❌ Project tidak ditemukan.").queue();
            return;
        }
        if (!p.getOwnerId().equals(event.getAuthor().getId())) {
            event.getChannel().sendMessage("❌ Kamu hanya bisa menghapus project milikmu sendiri!").queue();
            return;
        }
        repository.deleteProject(name, event.getAuthor().getId(), event.getGuild().getId());
        event.getChannel().sendMessage("🗑️ Project **" + name + "** telah dihapus.").queue();
    }

    // --- Embed Builders ---
    private MessageEmbed buildListEmbed(List<Project> projects) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("📦 Community Projects");
        eb.setColor(BotConfig.EMBED_COLOR);
        if (projects.isEmpty()) {
            eb.setDescription("Belum ada project yang terdaftar.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < projects.size(); i++) {
                Project p = projects.get(i);
                sb.append(String.format("**%d. %s**\n%s\nTag: `%s`\n\n", i + 1, p.getName(), p.getDescription(), p.getTags()));
            }
            eb.setDescription(sb.toString());
        }
        return eb.build();
    }

    private MessageEmbed buildDetailEmbed(Project p, net.dv8tion.jda.api.JDA jda) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Project: " + p.getName());
        eb.setColor(BotConfig.EMBED_COLOR);
        eb.addField("Owner", jda.getUserById(p.getOwnerId()) != null ? jda.getUserById(p.getOwnerId()).getAsMention() : "Unknown", true);
        eb.addField("Link", "[Klik di sini](" + p.getLink() + ")", true);
        eb.addField("Tags", "`" + p.getTags() + "`", true);
        eb.addField("Description", p.getDescription(), false);
        eb.setFooter("Ditambahkan pada: " + p.getCreatedAt());
        return eb.build();
    }
}
