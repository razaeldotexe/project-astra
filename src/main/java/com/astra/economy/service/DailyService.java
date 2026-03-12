package com.astra.economy.service;

import com.astra.economy.model.UserEconomy;
import com.astra.economy.repository.EconomyRepository;

import java.time.Instant;
import java.util.Random;

public class DailyService {
    private final EconomyRepository economyRepository = new EconomyRepository();
    private final EconomyService economyService = new EconomyService();
    private static final long DAILY_COOLDOWN = 86400; // 24 jam dalam detik

    public long claimDaily(String userId, String guildId) throws CooldownException {
        UserEconomy user = economyService.getOrCreateUser(userId, guildId);
        long now = Instant.now().getEpochSecond();
        
        if (user.getLastDaily() != 0 && (now - user.getLastDaily() < DAILY_COOLDOWN)) {
            throw new CooldownException(DAILY_COOLDOWN - (now - user.getLastDaily()));
        }

        long reward = 500 + new Random().nextInt(501); // 500 - 1000
        user.setBalance(user.getBalance() + reward);
        user.setLastDaily(now);
        economyRepository.saveUser(user);
        
        return reward;
    }
}
