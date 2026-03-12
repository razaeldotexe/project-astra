package com.astra.economy.repository;

import com.astra.config.DatabaseConfig;
import com.astra.economy.model.UserEconomy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EconomyRepository {

    public UserEconomy getUser(String userId, String guildId) {
        String sql = "SELECT * FROM users_economy WHERE user_id = ? AND guild_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, guildId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new UserEconomy(
                        rs.getString("user_id"),
                        rs.getString("guild_id"),
                        rs.getLong("balance"),
                        rs.getLong("bank_balance"),
                        rs.getLong("last_daily"),
                        rs.getLong("last_work")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveUser(UserEconomy user) {
        String sql = "INSERT OR REPLACE INTO users_economy (user_id, guild_id, balance, bank_balance, last_daily, last_work) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getGuildId());
            pstmt.setLong(3, user.getBalance());
            pstmt.setLong(4, user.getBankBalance());
            pstmt.setLong(5, user.getLastDaily());
            pstmt.setLong(6, user.getLastWork());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<UserEconomy> getLeaderboard(String guildId, int limit) {
        List<UserEconomy> list = new ArrayList<>();
        String sql = "SELECT * FROM users_economy WHERE guild_id = ? ORDER BY (balance + bank_balance) DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, guildId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new UserEconomy(
                        rs.getString("user_id"),
                        rs.getString("guild_id"),
                        rs.getLong("balance"),
                        rs.getLong("bank_balance"),
                        rs.getLong("last_daily"),
                        rs.getLong("last_work")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
