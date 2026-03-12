package com.astra.economy.service;

public class CooldownException extends Exception {
    private final long remainingTimeSeconds;

    public CooldownException(long remainingTimeSeconds) {
        super("Masih dalam cooldown.");
        this.remainingTimeSeconds = remainingTimeSeconds;
    }

    public long getRemainingTimeSeconds() {
        return remainingTimeSeconds;
    }
}
