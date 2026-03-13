package com.astra.listeners;

import com.astra.services.SshService;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.io.FileWriter;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShellListener extends ListenerAdapter {
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

        event.getChannel().sendTyping().queue();

        executor.submit(() -> {
            String output = sshService.executeCommand(command);
            
            if (output.length() > 1900) {
                // Split or send as file
                sendLargeMessage(event, output);
            } else {
                event.getChannel().sendMessage("```\n" + output + "\n```").queue();
            }
        });
    }

    private void sendLargeMessage(MessageReceivedEvent event, String content) {
        try {
            File tempFile = File.createTempFile("output", ".txt");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(content);
            }
            event.getChannel().sendFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(tempFile, "output.txt")).queue(
                success -> tempFile.delete(),
                error -> tempFile.delete()
            );
        } catch (Exception e) {
            event.getChannel().sendMessage("❌ Error while sending large output: " + e.getMessage()).queue();
        }
    }
}
