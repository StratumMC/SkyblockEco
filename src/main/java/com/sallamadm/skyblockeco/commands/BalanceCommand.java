package com.sallamadm.skyblockeco.commands;

import com.sallamadm.skyblockeco.EconomyAPI;
import com.sallamadm.skyblockeco.SkyblockEco;
import dev.jorel.commandapi.CommandAPICommand;

public class BalanceCommand {

    private BalanceCommand() {}

    public static void registerCommand(SkyblockEco plugin) {
        new CommandAPICommand("balance")
                .withAliases("bal")
                .withHelp("Oyuncunun parasını gösterir.", "Paranı gösterir.")
                .executesPlayer((player, args) -> {
                    long balance = EconomyAPI.getBalance(player.getUniqueId());
                    player.sendMessage("§6Bakiyen: §e" + balance + " §6coin");
                })
                .register();
    }
}