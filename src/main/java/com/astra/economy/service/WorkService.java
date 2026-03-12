package com.astra.economy.service;

import com.astra.economy.model.UserEconomy;
import com.astra.economy.repository.EconomyRepository;

import java.time.Instant;
import java.util.Random;

public class WorkService {
    private final EconomyRepository economyRepository = new EconomyRepository();
    private final EconomyService economyService = new EconomyService();
    private static final long WORK_COOLDOWN = 3600; // 1 jam dalam detik

    private final String[] jobs = {
            "Programmer Java", "Penambang Koin", "Kurir Paket", "Karyawan Toko",
            "Petani Modern", "Content Creator", "Freelance Designer"
    };

    public WorkResult work(String userId, String guildId) throws CooldownException {
        UserEconomy user = economyService.getOrCreateUser(userId, guildId);
        long now = Instant.now().getEpochSecond();

        if (user.getLastWork() != 0 && (now - user.getLastWork() < WORK_COOLDOWN)) {
            throw new CooldownException(WORK_COOLDOWN - (now - user.getLastWork()));
        }

        long reward = 100 + new Random().nextInt(201); // 100 - 300
        String job = jobs[new Random().nextInt(jobs.length)];
        
        user.setBalance(user.getBalance() + reward);
        user.setLastWork(now);
        economyRepository.saveUser(user);

        return new WorkResult(job, reward);
    }

    public static record WorkResult(String job, long reward) {}
}
