package com.sallamadm.skyblockeco.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class BalanceChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final UUID playerUUID;
    private final double oldBalance;
    private final double newBalance;

    public BalanceChangeEvent(UUID playerUUID, double oldBalance, double newBalance) {
        this.playerUUID = playerUUID;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }

    public double getOldBalance() {
        return oldBalance;
    }

    public double getNewBalance() {
        return newBalance;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}