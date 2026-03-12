package com.astra.economy.service;

import com.astra.economy.model.Item;
import com.astra.economy.model.UserEconomy;
import com.astra.economy.repository.EconomyRepository;
import com.astra.economy.repository.InventoryRepository;
import com.astra.economy.repository.ItemRepository;
import com.astra.economy.repository.TransactionRepository;

import java.util.List;

public class ShopService {
    private final ItemRepository itemRepository = new ItemRepository();
    private final InventoryRepository inventoryRepository = new InventoryRepository();
    private final EconomyRepository economyRepository = new EconomyRepository();
    private final EconomyService economyService = new EconomyService();
    private final TransactionRepository transactionRepository = new TransactionRepository();

    public List<Item> getShopItems() {
        return itemRepository.getAllActiveItems();
    }

    public void buyItem(String userId, String guildId, int itemId) throws InsufficientBalanceException, IllegalArgumentException {
        Item item = itemRepository.getItemById(itemId);
        if (item == null || !item.isActive()) {
            throw new IllegalArgumentException("Item tidak ditemukan atau tidak aktif.");
        }

        UserEconomy user = economyService.getOrCreateUser(userId, guildId);
        if (user.getBalance() < item.getPrice()) {
            throw new InsufficientBalanceException("Saldo tidak cukup untuk membeli " + item.getItemName());
        }

        user.setBalance(user.getBalance() - item.getPrice());
        economyRepository.saveUser(user);
        inventoryRepository.addItem(userId, guildId, itemId, 1);
        transactionRepository.logTransaction(userId, null, guildId, item.getPrice(), "SHOP", "Membeli item: " + item.getItemName());
    }
}
