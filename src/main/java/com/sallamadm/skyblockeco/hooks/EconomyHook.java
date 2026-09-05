package com.sallamadm.skyblockeco.hooks;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.api.EconomyProvider;
import com.sallamadm.skyblockeco.EconomyAPI;

import java.util.UUID;

public class EconomyHook implements EconomyProvider {

    public static void register() {
        SkyblockCore.setEconomyProvider(new EconomyHook());
    }

    @Override
    public double getBalance(UUID uuid) {
        return EconomyAPI.getBalance(uuid);
    }

    @Override
    public void addBalance(UUID uuid, double amount) {
        EconomyAPI.addBalance(uuid, amount);
    }

    @Override
    public void removeBalance(UUID uuid, double amount) {
        EconomyAPI.removeBalance(uuid, amount);
    }

    @Override
    public boolean hasBalance(UUID uuid, double amount) {
        return EconomyAPI.getBalance(uuid) >= amount;
    }
}