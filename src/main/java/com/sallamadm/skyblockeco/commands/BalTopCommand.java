package com.sallamadm.skyblockeco.commands;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockcore.config.MessageManager;
import com.sallamadm.skyblockeco.SkyblockEco;
import com.sallamadm.skyblockeco.data.DataManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BalTopCommand {

    private static final int PAGE_SIZE = 10;
    private static MessageManager msg = SkyblockCore.getInstance().getMessageManager();

    private BalTopCommand() {};

    public static void registerCommand(SkyblockEco plugin) {
        new CommandAPICommand("baltop")
                .withAliases("topbal", "topbalance", "balancetop")
                .withHelp("En zengin oyuncuları gösterir.", "Bakiye sıralamasını sayfalı şekilde gösterir.")
                .withOptionalArguments(new IntegerArgument("page", 1))
                .executes((sender, args) -> {
                    int page = args.get("page") != null ? (int) args.get("page") : 1;
                    sendBalTopPage(plugin, sender, page);
                })
                .register();
    }



    private static void sendBalTopPage(SkyblockEco plugin, CommandSender sender, int page) {
        DataManager dataManager = plugin.getDataManager();

        int totalAccounts = dataManager.getTotalAccountCount();
        if (totalAccounts <= 0) {
            sender.sendMessage(msg.getMessage("economy.baltop-nouser"));
            return;
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) totalAccounts / PAGE_SIZE));
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<DataManager.BalanceEntry> entries = dataManager.getTopBalances(offset, PAGE_SIZE);

        sender.sendMessage(ChatColor.GOLD + "=== Bakiye Sıralaması (Sayfa " + page + "/" + totalPages + ") ===");

        int rank = offset + 1;
        for (DataManager.BalanceEntry entry : entries) {
            String name = entry.getUsername() != null ? entry.getUsername() : "Bilinmiyor";
            sender.sendMessage(ChatColor.YELLOW + "#" + rank + " " + ChatColor.WHITE + name
                    + ChatColor.GRAY + " - " + ChatColor.GREEN + formatBalance(entry.getBalance()) + " coin");
            rank++;
        }

        if (page < totalPages) {
            sender.sendMessage(ChatColor.GRAY + "Devamı için: " + ChatColor.AQUA + "/baltop " + (page + 1));
        }
    }


    private static String formatBalance(double balance) {
        if (balance == Math.floor(balance)) {
            return String.valueOf((long) balance);
        }
        return String.format("%.2f", balance);
    }
}
