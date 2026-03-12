package com.astra.economy.repository;

import com.astra.config.DatabaseConfig;

import java.sql.*;

public class TransactionRepository {

    public void logTransaction(String senderId, String receiverId, String guildId, long amount, String type, String description) {
        String sql = "INSERT INTO transactions (sender_id, receiver_id, guild_id, amount, tx_type, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, senderId);
            pstmt.setString(2, receiverId);
            pstmt.setString(3, guildId);
            pstmt.setLong(4, amount);
            pstmt.setString(5, type);
            pstmt.setString(6, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
