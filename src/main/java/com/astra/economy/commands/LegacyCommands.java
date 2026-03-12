package com.astra.economy.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import com.astra.StatFoxAnalyst;
import com.astra.MinecraftWarden;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class LegacyCommands {
    
    public static void handlePing(SlashCommandInteractionEvent event) {
        long time = System.currentTimeMillis();
        event.reply("Pong!").setEphemeral(true)
             .flatMap(v -> 
                event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)
             ).queue();
    }

    public static void handleHello(SlashCommandInteractionEvent event) {
        event.reply("Halo " + event.getUser().getAsMention() + "! Selamat datang di project-astra! 🚀").queue();
    }

    public static void handleAnalisis(SlashCommandInteractionEvent event) {
        String mockJson = "{\"total_messages\": 1250, \"voice_minutes\": 450, \"member_retention\": 0.65, \"peak_hour\": 20}";
        StatFoxAnalyst analyst = new StatFoxAnalyst();
        StatFoxAnalyst.AnalysisResult result = analyst.analyze(mockJson);
        event.reply("📊 **StatFox Analyst Report** 🦊\n\n" + result.toString()).setEphemeral(false).queue();
    }

    public static void handleMonitorMC(SlashCommandInteractionEvent event) {
        String mcTelemetry = "{\"tps\": 14.5, \"ram_usage_percent\": 92.4, \"cpu_usage_percent\": 78.0, \"player_count\": 12, \"config\": {\"view-distance\": 10, \"max-players\": 50}, \"active_plugins\": [\"EssentialsX\", \"WorldGuard\", \"Dynmap\", \"LuckPerms\"]}";
        MinecraftWarden warden = new MinecraftWarden();
        MinecraftWarden.WardenReport report = warden.diagnose(mcTelemetry);
        event.reply(report.toString()).setEphemeral(false).queue();
    }

    public static void handleMonitorMC2(SlashCommandInteractionEvent event) {
        OptionMapping ipOption = event.getOption("ip");
        if (ipOption == null) return;
        String ip = ipOption.getAsString();
        event.deferReply().queue();
        new Thread(() -> {
            try {
                MinecraftWarden warden = new MinecraftWarden();
                String statusJson = warden.fetchRemoteStatus(ip);
                if (statusJson.contains("\"error\"")) {
                    event.getHook().sendMessage("❌ **Gagal mengambil status:** " + statusJson).queue();
                    return;
                }
                MinecraftWarden.WardenReport report = warden.diagnose(statusJson);
                event.getHook().sendMessage("🌐 **Remote Monitoring: " + ip + "**\n\n" + report.toString()).queue();
            } catch (Exception e) {
                event.getHook().sendMessage("❌ **Terjadi kesalahan:** " + e.getMessage()).queue();
            }
        }).start();
    }
}
