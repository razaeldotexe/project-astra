package com.astra.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:economy.db");
        config.setDriverClassName("org.sqlite.JDBC");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(10);
        
        dataSource = new HikariDataSource(config);
        initializeTables();
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private static void initializeTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Users Economy Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users_economy (" +
                    "user_id TEXT NOT NULL," +
                    "guild_id TEXT NOT NULL," +
                    "balance INTEGER DEFAULT 0," +
                    "bank_balance INTEGER DEFAULT 0," +
                    "last_daily INTEGER," +
                    "last_work INTEGER," +
                    "PRIMARY KEY (user_id, guild_id))");

            // Items Table
            stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                    "item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "item_name TEXT NOT NULL," +
                    "description TEXT," +
                    "price INTEGER NOT NULL," +
                    "is_active INTEGER DEFAULT 1)");

            // Inventory Table
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory (" +
                    "inventory_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id TEXT NOT NULL," +
                    "guild_id TEXT NOT NULL," +
                    "item_id INTEGER NOT NULL," +
                    "quantity INTEGER DEFAULT 1," +
                    "acquired_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(item_id) REFERENCES items(item_id)," +
                    "UNIQUE(user_id, guild_id, item_id))");

            // Transactions Table
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "tx_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sender_id TEXT," +
                    "receiver_id TEXT," +
                    "guild_id TEXT NOT NULL," +
                    "amount INTEGER NOT NULL," +
                    "tx_type TEXT NOT NULL," +
                    "description TEXT," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
            
            // Seed items if empty
            stmt.execute("INSERT OR IGNORE INTO items (item_name, description, price) VALUES " +
                    "('Kopi Luwak', 'Menambah energi untuk bekerja', 100)," +
                    "('PC Gaming', 'Meningkatkan hasil kerja', 5000)," +
                    "('Mobil Mewah', 'Hanya untuk pamer kekayaan', 50000)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
