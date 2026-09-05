package com.sallamadm.skyblockeco.commands;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockeco.EconomyAPI;
import com.sallamadm.skyblockeco.SkyblockEco;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.CommandAPIArgumentType;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.entity.Player;

public class PayCommand {

    private PayCommand () {}

    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    public static void registerCommand(SkyblockEco plugin) {
        new CommandAPICommand("pay")
                .withHelp("Bir oyuncuya para gönderin.", "Para gönder.")
                .withArguments(
                        new PlayerArgument("target"),
                        new DoubleArgument("amount"))
                .executesPlayer((player, args) -> {
                    double amount = (double) args.get("amount");
                    Player target = (Player) args.get("target");

                    double playerBalance = EconomyAPI.getBalance(player.getUniqueId());
                    if(amount <= 0) {
                        player.sendMessage(msg.getMessage("pay.zero"));
                        return;
                    }
                    if(amount > playerBalance) {
                        player.sendMessage(msg.getMessage("pay.not-enough-money"));
                        return;
                    }
                    if(target.getUniqueId().equals(player.getUniqueId())) {
                        player.sendMessage(msg.getMessage("pay.self-payment"));
                        return;
                    }
                    EconomyAPI.removeBalance(player.getUniqueId(), amount);
                    EconomyAPI.addBalance(target.getUniqueId(), amount);

                    player.sendMessage(msg.getMessage("pay.success")
                            .replace("{target}", target.getName())
                            .replace("{amount}", String.valueOf(amount)));

                    target.sendMessage(msg.getMessage("pay-received")
                            .replace("{sender}", player.getName())
                            .replace("{amount}", String.valueOf(amount)));

                })
                .register();
    }
}
