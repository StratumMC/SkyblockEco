package com.sallamadm.skyblockeco.listeners;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockeco.events.BalanceChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BalanceChangeListener implements Listener {

    private final SkyblockCore core;

    public BalanceChangeListener(SkyblockCore core) {
        this.core = core;
    }

    @EventHandler
    public void onBalanceChange(BalanceChangeEvent event) {
        Player player = event.getPlayer();

        if (player != null && player.isOnline()) {
            core.getScoreboardManager().updateScoreboard(player);
        }
    }
}