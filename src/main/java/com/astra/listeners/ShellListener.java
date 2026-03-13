package com.astra.listeners;

import com.astra.services.SshService;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.io.FileWriter;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShellListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ShellListener.class);
    private final SshService sshService;
    private final String shellChannelId;
    private final String allowedRoleId;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public ShellListener(SshService sshService) {
        this.sshService = sshService;
        Dotenv dotenv = Dotenv.load();
        this.shellChannelId = dotenv.get("SHELL_CHANNEL_ID");
        this.allowedRoleId = dotenv.get("SHELL_ALLOWED_ROLE_ID");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().getId().equals(shellChannelId)) return;

        // Security check: Role validation
        if (allowedRoleId != null && !allowedRoleId.isEmpty()) {
            Role allowedRole = event.getGuild().getRoleById(allowedRoleId);
            if (allowedRole == null || !event.getMember().getRoles().contains(allowedRole)) {
                event.getChannel().sendMessage("❌ [DENIED] Kamu tidak memiliki izin untuk menggunakan shell.").queue();
                return;
            }
        }

        String command = event.getMessage().getContentRaw().trim();
        if (command.isEmpty()) return;

        // Auditing: Log command execution
        logger.info("[SHELL] User: {}#{} (ID: {}) executed command: {}", 
            event.getAuthor().getName(), event.getAuthor().getDiscriminator(), event.getAuthor().getId(), command);

        event.getChannel().sendTyping().queue();

        executor.submit(() -> {
            StringBuilder accumulatedOutput = new StringBuilder();
            final net.dv8tion.jda.api.entities.Message[] messageRef = {null};
            final long[] lastUpdate = {System.currentTimeMillis()};

            event.getChannel().sendMessage("`> Executing: " + command + "...`").queue(msg -> {
                messageRef[0] = msg;
            });

            sshService.executeCommandStreaming(command, chunk -> {
                accumulatedOutput.append(chunk);
                long now = System.currentTimeMillis();
                
                // Rate limit: Update once every 1200ms to avoid Discord rate limits
                if (now - lastUpdate[0] > 1200 && messageRef[0] != null) {
                    updateMessage(messageRef[0], command, accumulatedOutput.toString());
                    lastUpdate[0] = now;
                }
            });

            // Final update after command completion
            // Wait slightly to ensure messageRef is populated from the async queue
            int retries = 0;
            while (messageRef[0] == null && retries < 10) {
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                retries++;
            }
            if (messageRef[0] != null) {
                updateMessage(messageRef[0], command, accumulatedOutput.toString());
            }
        });
    }

    private void updateMessage(net.dv8tion.jda.api.entities.Message message, String command, String content) {
        String displayContent = content.trim().isEmpty() ? "[No Output]" : content;
        // Discord limit is 2000 chars. Let's keep the last 1900 to see current progress.
        if (displayContent.length() > 1900) {
            displayContent = "[...TRUNCATED...]\n" + displayContent.substring(displayContent.length() - 1800);
        }
        message.editMessage("`> " + command + "`\n```\n" + displayContent + "\n```").queue();
    }
}
