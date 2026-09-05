package com.sallamadm.skyblockeco.commands;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockeco.EconomyAPI;
import com.sallamadm.skyblockeco.SkyblockEco;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.entity.Player;

import java.util.*;

public class PayCommand {

    private PayCommand () {}

    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    private static final Map<UUID, PendingPayment> PENDING_PAYMENTS = new HashMap<>();
    private static final long CONFIRMATION_TIMEOUT_MILLIS = 30_000L;

    public static void registerCommand(SkyblockEco plugin) {
        new CommandAPICommand("pay")
                .withHelp("Bir oyuncuya para gönderin.", "Para gönder.")
                .withArguments(
                        new PlayerArgument("target"),
                        new DoubleArgument("amount"))
                .executesPlayer((player, args) -> {
                    double amount = (double) args.get("amount");
                    Player target = (Player) args.get("target");

                    if(amount <= 0) {
                        player.sendMessage(msg.getMessage("economy.zero"));
                        return;
                    }
                    if(target.getUniqueId().equals(player.getUniqueId())) {
                        player.sendMessage(msg.getMessage("economy.self-payment"));
                        return;
                    }

                    double playerBalance = EconomyAPI.getBalance(player.getUniqueId());
                    if(amount > playerBalance) {
                        player.sendMessage(msg.getMessage("economy.not-enough-money"));
                        return;
                    }

                    UUID senderUuid = player.getUniqueId();
                    PendingPayment pending = PENDING_PAYMENTS.get(senderUuid);
                    boolean expired = pending != null
                            && (System.currentTimeMillis() - pending.timestamp) > CONFIRMATION_TIMEOUT_MILLIS;

                    if (pending != null && !expired
                            && pending.targetUuid.equals(target.getUniqueId())
                            && pending.amount == amount) {
                        PENDING_PAYMENTS.remove(senderUuid);

                        double currentBalance = EconomyAPI.getBalance(senderUuid);
                        if (amount > currentBalance) {
                            player.sendMessage(msg.getMessage("economy.not-enough-money"));
                            return;
                        }

                        plugin.getDataManager().exchangeBal(senderUuid, target.getUniqueId(), amount);

                        player.sendMessage(msg.getMessage("economy.pay-success")
                                .replace("{target}", target.getName())
                                .replace("{amount}", String.valueOf(amount)));

                        target.sendMessage(msg.getMessage("economy.pay-received")
                                .replace("{sender}", player.getName())
                                .replace("{amount}", String.valueOf(amount)));
                        return;
                    }

                    PENDING_PAYMENTS.put(senderUuid, new PendingPayment(target.getUniqueId(), amount));

                    player.sendMessage(msg.getMessage("economy.pay-confirm")
                            .replace("{target}", target.getName())
                            .replace("{amount}", String.valueOf(amount)));
                })
                .register();
    }

    private static class PendingPayment {
        private final UUID targetUuid;
        private final double amount;
        private final long timestamp;

        private PendingPayment(UUID targetUuid, double amount) {
            this.targetUuid = targetUuid;
            this.amount = amount;
            this.timestamp = System.currentTimeMillis();
        }
    }
}