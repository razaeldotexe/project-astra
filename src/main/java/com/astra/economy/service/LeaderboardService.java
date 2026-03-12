package com.astra.economy.service;

import com.astra.economy.model.UserEconomy;
import com.astra.economy.repository.EconomyRepository;

import java.util.List;

public class LeaderboardService {
    private final EconomyRepository economyRepository = new EconomyRepository();

    public List<UserEconomy> getTopUsers(String guildId, int limit) {
        return economyRepository.getLeaderboard(guildId, limit);
    }
}
