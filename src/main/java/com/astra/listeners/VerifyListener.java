package com.astra.listeners;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class VerifyListener extends ListenerAdapter {
    private final Dotenv dotenv = Dotenv.load();

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equals("verify_button")) {
            String roleId = dotenv.get("VERIFY_ROLE_ID");
            if (roleId == null || roleId.isEmpty()) {
                event.reply("❌ Error: `VERIFY_ROLE_ID` tidak ditemukan di `.env`.").setEphemeral(true).queue();
                return;
            }

            Role role = event.getGuild().getRoleById(roleId);
            if (role == null) {
                event.reply("❌ Error: Role dengan ID `" + roleId + "` tidak ditemukan.").setEphemeral(true).queue();
                return;
            }

            if (event.getMember().getRoles().contains(role)) {
                event.reply("ℹ️ Kamu sudah terverifikasi!").setEphemeral(true).queue();
                return;
            }

            event.getGuild().addRoleToMember(event.getMember(), role).queue(
                success -> event.reply("✅ Kamu berhasil terverifikasi! Role **" + role.getName() + "** ditambahkan.").setEphemeral(true).queue(),
                error -> event.reply("❌ Gagal memberikan role: " + error.getMessage()).setEphemeral(true).queue()
            );
        }
    }
}
