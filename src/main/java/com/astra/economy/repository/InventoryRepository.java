package com.astra.economy.repository;

import com.astra.config.DatabaseConfig;
import com.astra.economy.model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryRepository {

    public void addItem(String userId, String guildId, int itemId, int quantity) {
        String sql = "INSERT INTO inventory (user_id, guild_id, item_id, quantity) VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT(user_id, guild_id, item_id) DO UPDATE SET quantity = quantity + excluded.quantity";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, guildId);
            pstmt.setInt(3, itemId);
            pstmt.setInt(4, quantity);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<InventoryItem> getUserInventory(String userId, String guildId) {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT items.*, inventory.quantity FROM inventory " +
                     "JOIN items ON inventory.item_id = items.item_id " +
                     "WHERE inventory.user_id = ? AND inventory.guild_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, guildId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Item item = new Item(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("description"),
                        rs.getLong("price"),
                        rs.getInt("is_active") == 1
                );
                items.add(new InventoryItem(item, rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static record InventoryItem(Item item, int quantity) {}
}
