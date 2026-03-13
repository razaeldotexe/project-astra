package com.astra.showcase.service;

import com.astra.config.BotConfig;
import com.astra.showcase.model.Project;
import com.astra.showcase.repository.ProjectRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ShowcaseManager {
    private static final Logger logger = LoggerFactory.getLogger(ShowcaseManager.class);
    private final ProjectRepository repository = new ProjectRepository();
    private final JDA jda;
    private final String channelId;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ShowcaseManager(JDA jda, String channelId) {
        this.jda = jda;
        this.channelId = channelId;
    }

    public void start() {
        if (channelId == null || channelId.isBlank()) {
            logger.warn("Showcase Channel ID is not set. Background updates disabled.");
            return;
        }

        scheduler.scheduleAtFixedRate(this::updateStatusMessage, 5, 10, TimeUnit.SECONDS);
        logger.info("Showcase Manager started (Channel: {})", channelId);
    }

    private void updateStatusMessage() {
        try {
            TextChannel channel = jda.getTextChannelById(channelId);
            if (channel == null) return;

            List<Project> projects = repository.getAllProjects(channel.getGuild().getId());
            MessageEmbed embed = buildMasterEmbed(projects);

            String messageId = repository.getMetadata("status_message_id");

            if (messageId == null) {
                channel.sendMessageEmbeds(embed).queue(msg -> {
                    try {
                        repository.setMetadata("status_message_id", msg.getId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                channel.retrieveMessageById(messageId).queue(
                    msg -> msg.editMessageEmbeds(embed).queue(),
                    error -> {
                        // If message deleted, send new one
                        channel.sendMessageEmbeds(embed).queue(msg -> {
                            try {
                                repository.setMetadata("status_message_id", msg.getId());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                );
            }
        } catch (Exception e) {
            logger.error("Error updating showcase status message", e);
        }
    }

    private MessageEmbed buildMasterEmbed(List<Project> projects) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("📦 Community Projects Showcase");
        eb.setDescription("Daftar proyek keren dari komunitas Astra! Gunakan `/project add` untuk menambahkan milikmu.");
        eb.setColor(BotConfig.EMBED_COLOR);
        eb.setTimestamp(java.time.Instant.now());
        eb.setFooter("Auto-updates every 10 seconds");

        if (projects.isEmpty()) {
            eb.addField("", "Belum ada project yang disubmit. Jadilah yang pertama!", false);
        } else {
            for (int i = 0; i < Math.min(projects.size(), 20); i++) {
                Project p = projects.get(i);
                String value = String.format("%s\n🔗 [Link](%s) | 🏷️ %s", 
                    p.getDescription(), p.getLink(), p.getTags());
                eb.addField((i + 1) + ". " + p.getName(), value, false);
            }
            if (projects.size() > 20) {
                eb.addField("", "...dan " + (projects.size() - 20) + " lainnya.", false);
            }
        }
        return eb.build();
    }

    public void stop() {
        scheduler.shutdown();
    }
}
