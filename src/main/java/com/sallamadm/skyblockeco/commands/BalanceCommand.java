package com.sallamadm.skyblockeco.commands;

import com.sallamadm.skyblockeco.SkyblockEco;
import dev.jorel.commandapi.CommandAPICommand;

public class BalanceCommand {

    private BalanceCommand() {}

    public static void registerCommand(SkyblockEco plugin) {
        new CommandAPICommand("balance")
                .withAliases("bal")
                .withHelp("oyuncunun parasını gösterir", "Paranı gösterir.")
                .executesPlayer((player, args) -> {
                    long balance = plugin.getDataManager().getBalance(player.getUniqueId());
                    player.sendMessage("Bakiyen: " + balance);
                })

                .register();
    }
}
