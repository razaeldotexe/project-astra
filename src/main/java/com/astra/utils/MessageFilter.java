package com.astra.utils;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class MessageFilter {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "(https?://|www\\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    );

    /** Semua pesan */
    public static Predicate<Message> all() {
        return msg -> true;
    }

    /** Pesan dari bot */
    public static Predicate<Message> bot() {
        return msg -> msg.getAuthor().isBot();
    }

    /** Pesan yang mengandung embed */
    public static Predicate<Message> embed() {
        return msg -> !msg.getEmbeds().isEmpty();
    }

    /** Pesan yang mengandung URL */
    public static Predicate<Message> links() {
        return msg -> URL_PATTERN.matcher(msg.getContentRaw()).find();
    }

    /** Pesan yang mengandung attachment/gambar */
    public static Predicate<Message> images() {
        return msg -> !msg.getAttachments().isEmpty();
    }

    /** Pesan dari user tertentu */
    public static Predicate<Message> fromUser(User targetUser) {
        return msg -> msg.getAuthor().equals(targetUser);
    }

    /** Pesan yang mengandung mention user atau role */
    public static Predicate<Message> mentions() {
        return msg -> !msg.getMentions().getUsers().isEmpty()
                   || !msg.getMentions().getRoles().isEmpty();
    }

    /** Pesan yang di-pin */
    public static Predicate<Message> pinned() {
        return Message::isPinned;
    }
}
