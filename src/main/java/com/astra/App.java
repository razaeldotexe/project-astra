package com.astra;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import io.github.cdimascio.dotenv.Dotenv;

public class App extends ListenerAdapter {
    public static void main(String[] args) throws Exception {
        // Load the .env file
        Dotenv dotenv = Dotenv.load();

        // Mengambil token dari Environment Variable bernama "DISCORD_TOKEN"
        String token = dotenv.get("DISCORD_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("DISCORD_TOKEN tidak ditemukan di file .env");
        }

        JDABuilder.createDefault(token)
                .setActivity(Activity.playing("Developing project-astra"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT) // Diperlukan untuk membaca isi pesan
                .addEventListeners(new App())
                .build();

        System.out.println("Bot is starting...");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;

        String message = event.getMessage().getContentRaw();

        if (message.equalsIgnoreCase("!ping")) {
            event.getChannel().sendMessage("Pong!").queue();
        }
    }
}
