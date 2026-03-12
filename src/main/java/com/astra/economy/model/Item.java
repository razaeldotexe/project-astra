package com.astra.economy.model;

public class Item {
    private int itemId;
    private String itemName;
    private String description;
    private long price;
    private boolean isActive;

    public Item(int itemId, String itemName, String description, long price, boolean isActive) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.description = description;
        this.price = price;
        this.isActive = isActive;
    }

    // Getters
    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getDescription() { return description; }
    public long getPrice() { return price; }
    public boolean isActive() { return isActive; }
}
