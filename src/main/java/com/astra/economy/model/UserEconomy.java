package com.astra.economy.model;

public class UserEconomy {
    private String userId;
    private String guildId;
    private long balance;
    private long bankBalance;
    private long lastDaily;
    private long lastWork;

    public UserEconomy(String userId, String guildId, long balance, long bankBalance, long lastDaily, long lastWork) {
        this.userId = userId;
        this.guildId = guildId;
        this.balance = balance;
        this.bankBalance = bankBalance;
        this.lastDaily = lastDaily;
        this.lastWork = lastWork;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public String getGuildId() { return guildId; }
    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = balance; }
    public long getBankBalance() { return bankBalance; }
    public void setBankBalance(long bankBalance) { this.bankBalance = bankBalance; }
    public long getLastDaily() { return lastDaily; }
    public void setLastDaily(long lastDaily) { this.lastDaily = lastDaily; }
    public long getLastWork() { return lastWork; }
    public void setLastWork(long lastWork) { this.lastWork = lastWork; }

    public long getTotalBalance() {
        return balance + bankBalance;
    }
}
