package com.astra.economy.repository;

import com.astra.config.DatabaseConfig;
import com.astra.economy.model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemRepository {

    public List<Item> getAllActiveItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE is_active = 1";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new Item(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("description"),
                        rs.getLong("price"),
                        rs.getInt("is_active") == 1
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public Item getItemById(int itemId) {
        String sql = "SELECT * FROM items WHERE item_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Item(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("description"),
                        rs.getLong("price"),
                        rs.getInt("is_active") == 1
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
