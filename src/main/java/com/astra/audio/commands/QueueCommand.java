package com.astra.audio.commands;

import com.astra.audio.GuildMusicManager;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueCommand implements MusicCommand {
    @Override
    public String getName() { return "queue"; }

    @Override
    public String getDescription() { return "Tampilkan queue musik"; }

    @Override
    public void executeSlash(SlashCommandInteractionEvent event) {
        event.reply(getQueueString(event.getGuild())).queue();
    }

    @Override
    public void executePrefix(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(getQueueString(event.getGuild())).queue();
    }

    private String getQueueString(net.dv8tion.jda.api.entities.Guild guild) {
        GuildMusicManager manager = PlayCommand.getOrCreate(guild);
        Queue<Track> queue = manager.getScheduler().getQueue();
        Track current = manager.getScheduler().getCurrentTrack();

        StringBuilder sb = new StringBuilder();
        sb.append("🎶 **Sekarang diputar:** ")
          .append(current != null ? current.getInfo().getTitle() : "Tidak ada")
          .append("\n\n**Queue:**\n");

        if (queue.isEmpty()) {
            sb.append("Queue kosong.");
        } else {
            AtomicInteger i = new AtomicInteger(1);
            queue.stream().limit(10).forEach(t ->
                sb.append(i.getAndIncrement())
                  .append(". ")
                  .append(t.getInfo().getTitle())
                  .append("\n")
            );
            if (queue.size() > 10)
                sb.append("... dan ").append(queue.size() - 10).append(" lagu lainnya.");
        }
        return sb.toString();
    }
}
