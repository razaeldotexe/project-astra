package com.astra.showcase.repository;

import com.astra.config.DatabaseConfig;
import com.astra.showcase.model.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {

    public void addProject(Project project) throws SQLException {
        String sql = "INSERT INTO projects (name, description, link, tags, owner_id, guild_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, project.getName());
            pstmt.setString(2, project.getDescription());
            pstmt.setString(3, project.getLink());
            pstmt.setString(4, project.getTags());
            pstmt.setString(5, project.getOwnerId());
            pstmt.setString(6, project.getGuildId());
            pstmt.executeUpdate();
        }
    }

    public List<Project> getAllProjects(String guildId) throws SQLException {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE guild_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSet(rs));
            }
        }
        return projects;
    }

    public Project getProjectByName(String name, String guildId) throws SQLException {
        String sql = "SELECT * FROM projects WHERE name = ? AND guild_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, guildId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        }
        return null;
    }

    public void deleteProject(String name, String ownerId, String guildId) throws SQLException {
        String sql = "DELETE FROM projects WHERE name = ? AND owner_id = ? AND guild_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, ownerId);
            pstmt.setString(3, guildId);
            pstmt.executeUpdate();
        }
    }

    public String getMetadata(String key) throws SQLException {
        String sql = "SELECT value FROM showcase_metadata WHERE key = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("value");
        }
        return null;
    }

    public void setMetadata(String key, String value) throws SQLException {
        String sql = "INSERT OR REPLACE INTO showcase_metadata (key, value) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        }
    }

    private Project mapResultSet(ResultSet rs) throws SQLException {
        return new Project(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("link"),
                rs.getString("tags"),
                rs.getString("owner_id"),
                rs.getString("guild_id"),
                rs.getInt("stars"),
                rs.getString("created_at")
        );
    }
}
