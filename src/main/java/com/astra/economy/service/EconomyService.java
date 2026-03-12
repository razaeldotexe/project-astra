package com.astra.economy.service;

import com.astra.economy.model.UserEconomy;
import com.astra.economy.repository.EconomyRepository;
import com.astra.economy.repository.TransactionRepository;

public class EconomyService {
    private final EconomyRepository economyRepository = new EconomyRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();

    public UserEconomy getOrCreateUser(String userId, String guildId) {
        UserEconomy user = economyRepository.getUser(userId, guildId);
        if (user == null) {
            user = new UserEconomy(userId, guildId, 0, 0, 0, 0);
            economyRepository.saveUser(user);
        }
        return user;
    }

    public void addBalance(String userId, String guildId, long amount, String type, String description) {
        UserEconomy user = getOrCreateUser(userId, guildId);
        user.setBalance(user.getBalance() + amount);
        economyRepository.saveUser(user);
        transactionRepository.logTransaction(null, userId, guildId, amount, type, description);
    }

    public void transfer(String senderId, String receiverId, String guildId, long amount) throws InsufficientBalanceException {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Tidak dapat mengirim uang ke diri sendiri.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Jumlah transfer harus lebih dari 0.");
        }

        UserEconomy sender = getOrCreateUser(senderId, guildId);
        if (sender.getBalance() < amount) {
            throw new InsufficientBalanceException("Saldo wallet tidak cukup.");
        }

        UserEconomy receiver = getOrCreateUser(receiverId, guildId);

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        economyRepository.saveUser(sender);
        economyRepository.saveUser(receiver);

        transactionRepository.logTransaction(senderId, receiverId, guildId, amount, "TRANSFER", "Transfer ke " + receiverId);
    }

    public void deposit(String userId, String guildId, long amount) throws InsufficientBalanceException {
        UserEconomy user = getOrCreateUser(userId, guildId);
        if (user.getBalance() < amount) {
            throw new InsufficientBalanceException("Saldo wallet tidak cukup untuk deposit.");
        }
        user.setBalance(user.getBalance() - amount);
        user.setBankBalance(user.getBankBalance() + amount);
        economyRepository.saveUser(user);
    }

    public void withdraw(String userId, String guildId, long amount) throws InsufficientBalanceException {
        UserEconomy user = getOrCreateUser(userId, guildId);
        if (user.getBankBalance() < amount) {
            throw new InsufficientBalanceException("Saldo bank tidak cukup untuk withdraw.");
        }
        user.setBankBalance(user.getBankBalance() - amount);
        user.setBalance(user.getBalance() + amount);
        economyRepository.saveUser(user);
    }
}
