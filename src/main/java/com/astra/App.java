package com.astra;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import io.github.cdimascio.dotenv.Dotenv;

public class App extends ListenerAdapter {
    public static void main(String[] args) throws Exception {
        // Load the .env file
        Dotenv dotenv = Dotenv.load();

        // Mengambil token dan Guild ID dari Environment Variable
        String token = dotenv.get("DISCORD_TOKEN");
        String guildId = dotenv.get("GUILD_ID");
        
        System.out.println("DEBUG: Loaded GUILD_ID from .env: [" + (guildId != null ? guildId : "NULL") + "]");

        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("DISCORD_TOKEN tidak ditemukan di file .env");
        }

        JDA jda = JDABuilder.createDefault(token)
                .setActivity(Activity.playing("Developing project-astra"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT) // Masih dibutuhkan untuk !ping
                .addEventListeners(new App())
                .build();

        // Tunggu sampai JDA siap (penting untuk mengakses Guild)
        jda.awaitReady();

        // 1. Bersihkan Global Commands (agar tidak duplikat)
        jda.updateCommands().queue();

        // 2. Mendaftarkan Guild Commands (Instan)
        if (guildId != null) guildId = guildId.trim();

        if (guildId != null && !guildId.isEmpty() && !guildId.equals("YOUR_GUILD_ID_HERE")) {
            var guild = jda.getGuildById(guildId);
            if (guild != null) {
                guild.updateCommands().addCommands(
                    Commands.slash("ping", "Menghitung latensi bot"),
                    Commands.slash("hello", "Menyapa bot astra"),
                    Commands.slash("analisis", "Mendapatkan wawasan bisnis StatFox untuk server ini"),
                    Commands.slash("monitor-mc", "Dapatkan diagnosis kesehatan server Minecraft FoxSync"),
                    Commands.slash("monitor-mc2", "Monitor server Minecraft luar berdasarkan IP")
                        .addOption(OptionType.STRING, "ip", "Alamat IP atau Hostname server Minecraft", true)
                ).queue();
                System.out.println("Guild Commands registered to: " + guild.getName());
            } else {
                System.err.println("Guild dengan ID " + guildId + " tidak ditemukan! Pastikan bot sudah ada di server tersebut.");
            }
        } else {
            System.out.println("GUILD_ID belum diatur dengan benar di .env. Menggunakan Global Commands sebagai fallback...");
            jda.updateCommands().addCommands(
                Commands.slash("ping", "Menghitung latensi bot"),
                Commands.slash("hello", "Menyapa bot astra"),
                Commands.slash("analisis", "Mendapatkan wawasan bisnis StatFox untuk server ini"),
                Commands.slash("monitor-mc", "Dapatkan diagnosis kesehatan server Minecraft FoxSync"),
                Commands.slash("monitor-mc2", "Monitor server Minecraft luar berdasarkan IP")
                    .addOption(OptionType.STRING, "ip", "Alamat IP atau Hostname server Minecraft", true)
            ).queue();
        }

        System.out.println("Bot is starting!");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        String message = event.getMessage().getContentRaw();

        if (message.equalsIgnoreCase("!ping")) {
            event.getChannel().sendMessage("Pong! (Legacy command)").queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("ping")) {
            long time = System.currentTimeMillis();
            event.reply("Pong!").setEphemeral(true)
                 .flatMap(v -> 
                    event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)
                 ).queue();
        } else if (event.getName().equals("hello")) {
            event.reply("Halo " + event.getUser().getAsMention() + "! Selamat datang di project-astra! 🚀").queue();
        } else if (event.getName().equals("analisis")) {
            // Simulasi pengambilan data (MOCK DATA)
            String mockJson = "{" +
                "\"total_messages\": 1250," +
                "\"voice_minutes\": 450," +
                "\"member_retention\": 0.65," +
                "\"peak_hour\": 20" +
            "}";

            StatFoxAnalyst analyst = new StatFoxAnalyst();
            StatFoxAnalyst.AnalysisResult result = analyst.analyze(mockJson);

            event.reply("📊 **StatFox Analyst Report** 🦊\n\n" + result.toString())
                 .setEphemeral(false).queue();
        } else if (event.getName().equals("monitor-mc")) {
            // Simulasi telemetri Minecraft (MOCK TELEMETRY)
            String mcTelemetry = "{" +
                "\"tps\": 14.5," +
                "\"ram_usage_percent\": 92.4," +
                "\"cpu_usage_percent\": 78.0," +
                "\"player_count\": 12," +
                "\"config\": {" +
                    "\"view-distance\": 10," +
                    "\"max-players\": 50" +
                "}," +
                "\"active_plugins\": [\"EssentialsX\", \"WorldGuard\", \"Dynmap\", \"LuckPerms\"]" +
            "}";

            MinecraftWarden warden = new MinecraftWarden();
            MinecraftWarden.WardenReport report = warden.diagnose(mcTelemetry);

            event.reply(report.toString()).setEphemeral(false).queue();
        } else if (event.getName().equals("monitor-mc2")) {
            String ip = event.getOption("ip").getAsString();
            
            // Memberikan respon awal karena fetching data bisa memakan waktu (deference)
            event.deferReply().queue();

            // Jalankan fetching dalam thread terpisah agar tidak memblokir JDA
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
}
