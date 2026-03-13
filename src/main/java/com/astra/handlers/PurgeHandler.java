package com.astra.handlers;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PurgeHandler {

    // Discord hanya izinkan bulk delete untuk pesan < 14 hari
    private static final int MAX_BULK_DELETE_AGE_DAYS = 14;

    /**
     * Ambil pesan dari channel dan filter sesuai kriteria.
     */
    public static List<Message> fetchFilteredMessages(
            TextChannel channel,
            int amount,
            Predicate<Message> filter) {

        // Batasi jumlah pesan maks 100 (limit API Discord)
        int fetchAmount = Math.min(amount, 100);

        List<Message> history = channel.getHistory()
            .retrievePast(fetchAmount)
            .complete();

        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(MAX_BULK_DELETE_AGE_DAYS);

        return history.stream()
            .filter(filter)
            .filter(msg -> msg.getTimeCreated().isAfter(cutoff)) // Skip pesan terlalu lama
            .collect(Collectors.toList());
    }

    /**
     * Hapus pesan yang sudah difilter dari channel.
     */
    public static void deleteMessages(
            SlashCommandInteraction interaction,
            TextChannel channel,
            List<Message> messages,
            String typeName) {

        if (messages.isEmpty()) {
            interaction.getHook().editOriginal("⚠️ Tidak ada pesan **" + typeName + "** yang ditemukan.")
                .queue();
            return;
        }

        if (messages.size() == 1) {
            // Bulk delete butuh minimal 2 pesan
            messages.get(0).delete().queue();
        } else {
            channel.deleteMessages(messages).queue();
        }

        interaction.getHook().editOriginal("✅ Berhasil menghapus **" + messages.size()
            + "** pesan [" + typeName + "].")
            .queue(success -> {
                // Auto-hapus pesan konfirmasi setelah 5 detik
                success.delete().queueAfter(5, TimeUnit.SECONDS);
            });
    }
}
