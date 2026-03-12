package com.astra;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class App extends ListenerAdapter {
    public static void main(String[] args) throws Exception {
        // Ganti "YOUR_BOT_TOKEN" dengan token bot Discord Anda (Disarankan menggunakan Environment Variable)
        String token = System.getenv("DISCORD_TOKEN");
        if (token == null) {
            token = "YOUR_BOT_TOKEN";
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
