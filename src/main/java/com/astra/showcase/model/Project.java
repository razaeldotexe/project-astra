package com.astra.showcase.model;

import java.time.LocalDateTime;

public class Project {
    private int id;
    private String name;
    private String description;
    private String link;
    private String tags;
    private String ownerId;
    private String guildId;
    private int stars;
    private String createdAt;

    public Project() {}

    public Project(int id, String name, String description, String link, String tags, String ownerId, String guildId, int stars, String createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.link = link;
        this.tags = tags;
        this.ownerId = ownerId;
        this.guildId = guildId;
        this.stars = stars;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getGuildId() { return guildId; }
    public void setGuildId(String guildId) { this.guildId = guildId; }
    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
