package com.astra;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import io.github.cdimascio.dotenv.Dotenv;
import com.astra.listeners.SlashCommandListener;
import com.astra.listeners.PrefixCommandListener;
import com.astra.listeners.VerifyListener;
import com.astra.listeners.ShellListener;
import com.astra.services.SshService;
import com.astra.economy.commands.CommandHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        logger.info("Initializing Astra Environment...");

        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("DISCORD_TOKEN");
        String guildId = dotenv.get("GUILD_ID");

        if (token == null || token.isBlank()) {
            logger.error("FATAL: DISCORD_TOKEN is missing. Please check your .env file.");
            System.exit(1);
        }

        try {
            JDA jda = JDABuilder.createDefault(token)
                    .setActivity(Activity.playing("Astra Projects | .help - /help"))
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS)
                    .build();
            CommandHandler commandHandler = new CommandHandler();
            SshService sshService = new SshService();
            jda.addEventListener(new App(), new SlashCommandListener(), new PrefixCommandListener(commandHandler), new VerifyListener(), new ShellListener(sshService), new com.astra.commands.PurgeCommand());

            jda.awaitReady();
            logger.info("JDA Session Established successfully.");

            // Clear Global Commands to avoid any leftover sync issues
            jda.updateCommands().queue();

            // Register Slash Commands
            if (guildId != null && !guildId.isBlank()) {
                var guild = jda.getGuildById(guildId.trim());
                if (guild != null) {
                    guild.updateCommands().addCommands(
                            Commands.slash("ping", "Menghitung latensi bot"),
                            Commands.slash("hello", "Menyapa bot astra"),
                            Commands.slash("analisis", "Mendapatkan wawasan bisnis StatFox untuk server ini"),
                            Commands.slash("monitor-mc", "Dapatkan diagnosis kesehatan server Minecraft FoxSync"),
                            Commands.slash("monitor-mc2", "Monitor server Minecraft luar berdasarkan IP")
                                    .addOption(OptionType.STRING, "ip", "Alamat IP atau Hostname server Minecraft",
                                            true),

                            Commands.slash("setup", "Setup bot features")
                                    .addSubcommands(new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("verify", "Setup verification message"))
                                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),

                            Commands.slash("purge", "Hapus pesan secara massal")
                                    .addSubcommands(
                                            new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("all", "Hapus semua pesan")
                                                    .addOption(OptionType.INTEGER, "jumlah", "Jumlah pesan yang dihapus (maks 100)", true),
                                            new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("bot", "Hapus pesan dari bot")
                                                    .addOption(OptionType.INTEGER, "jumlah", "Jumlah pesan yang dicek", true),
                                            new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("embed", "Hapus pesan yang mengandung embed")
                                                    .addOption(OptionType.INTEGER, "jumlah", "Jumlah pesan yang dicek", true),
                                            new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("links", "Hapus pesan yang mengandung link")
                                                    .addOption(OptionType.INTEGER, "jumlah", "Jumlah pesan yang dicek", true),
                                            new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("images", "Hapus pesan yang mengandung gambar")
                                                    .addOption(OptionType.INTEGER, "jumlah", "Jumlah pesan yang dicek", true),
                                            new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("user", "Hapus pesan dari user tertentu")
                                                    .addOption(OptionType.USER, "target", "User yang pesannya dihapus", true)
                                                    .addOption(OptionType.INTEGER, "jumlah", "Jumlah pesan yang dicek", true),
                                            new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("mentions", "Hapus pesan yang mengandung mention")
                                                    .addOption(OptionType.INTEGER, "jumlah", "Jumlah pesan yang dicek", true),
                                            new net.dv8tion.jda.api.interactions.commands.build.SubcommandData("pinned", "Hapus pesan yang di-pin")
                                                    .addOption(OptionType.INTEGER, "jumlah", "Jumlah pesan yang dicek", true)
                                    )
                                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)),

                            // Economy Commands
                            Commands.slash("balance", "Tampilkan saldo wallet & bank")
                                    .addOption(OptionType.USER, "user", "User yang ingin dilihat saldonya", false),
                            Commands.slash("daily", "Klaim reward harian"),
                            Commands.slash("work", "Kerjakan pekerjaan random untuk mendapatkan koin"),
                            Commands.slash("deposit", "Pindahkan koin dari wallet ke bank")
                                    .addOption(OptionType.INTEGER, "amount", "Jumlah koin yang didepositkan", true),
                            Commands.slash("withdraw", "Pindahkan koin dari bank ke wallet")
                                    .addOption(OptionType.INTEGER, "amount", "Jumlah koin yang ditarik", true),
                            Commands.slash("pay", "Kirim koin ke pengguna lain")
                                    .addOption(OptionType.USER, "user", "Penerima koin", true)
                                    .addOption(OptionType.INTEGER, "amount", "Jumlah koin yang dikirim", true),
                            Commands.slash("shop", "Tampilkan daftar item di toko"),
                            Commands.slash("buy", "Beli item dari toko")
                                    .addOption(OptionType.INTEGER, "item_id", "ID item yang ingin dibeli", true),
                            Commands.slash("inventory", "Lihat inventaris item")
                                    .addOption(OptionType.USER, "user", "User yang ingin dilihat inventarisnya", false),
                            Commands.slash("leaderboard", "Tampilkan top-10 terkaya di server"),
                            Commands.slash("help", "Tampilkan daftar perintah astra")).queue();
                    logger.info("Production Environment: Commands registered to Guild [{}]", guild.getName());
                } else {
                    logger.warn("Guild ID [{}] not found. Falling back to global registration.", guildId);
                    registerGlobalCommands(jda);
                }
            } else {
                registerGlobalCommands(jda);
            }

            logger.info("Astra Bot is Online!");

            // Cleanup on shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Termination signal received. Shutting down...");
                sshService.disconnect();
                jda.shutdown();
            }));

        } catch (Exception e) {
            logger.error("Initialization Failed", e);
            System.exit(1);
        }
    }

    private static void registerGlobalCommands(JDA jda) {
        logger.info("Registering commands globally...");
        jda.updateCommands().addCommands(
                Commands.slash("ping", "Menghitung latensi bot"),
                Commands.slash("hello", "Menyapa bot astra"),
                Commands.slash("balance", "Tampilkan saldo wallet & bank"),
                Commands.slash("help", "Tampilkan daftar perintah astra")).queue();
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
}
