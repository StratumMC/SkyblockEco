package com.sallamadm.skyblockeco.data;
import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockeco.SkyblockEco;
import com.sallamadm.skyblockeco.events.BalanceChangeEvent;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;

public class DataManager {
    private static final double DEFAULT_BALANCE = 500D;

    private final SkyblockEco plugin;

    public DataManager(SkyblockEco plugin) {
        this.plugin = plugin;
        ensureColumn();
    }

    private Connection connection() {
        try {
            SkyblockCore core = SkyblockCore.getInstance();
            if (core == null) return null;
            return core.getDataManager().getDatabaseConnection();
        } catch (Exception e) {
            plugin.getLogger().severe("SkyblockCore bağlantısına erişilemedi: " + e.getMessage());
            return null;
        }
    }

    private void ensureColumn() {
        Connection conn = connection();
        if (conn == null) {
            plugin.getLogger().severe("MySQL bağlantısı yok, balance kolonu eklenemedi.");
            return;
        }

        try (Statement st = conn.createStatement()) {
            st.execute(
                    "ALTER TABLE sb_accounts " +
                            "ADD COLUMN IF NOT EXISTS balance BIGINT NOT NULL DEFAULT 500"
            );
            plugin.getLogger().info("sb_accounts.balance kolonu hazır.");
        } catch (SQLException e) {
            plugin.getLogger().severe("sb_accounts.balance eklenemedi: " + e.getMessage());
        }
    }

    public double getBalance(UUID playerUuid) {
        Connection conn = connection();
        if (conn == null || playerUuid == null) return DEFAULT_BALANCE;

        String sql = "SELECT balance FROM sb_accounts WHERE uuid = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Balance okunamadı: " + e.getMessage());
        }
        return DEFAULT_BALANCE;
    }

    public boolean setBalance(UUID playerUuid, double balance) {
        Connection conn = connection();
        if (conn == null || playerUuid == null) return false;

        double clamped = Math.max(0D, balance);
        double oldBalance = getBalance(playerUuid);
        String sql = "UPDATE sb_accounts SET balance = ? WHERE uuid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, clamped);
            ps.setString(2, playerUuid.toString());

            boolean success = ps.executeUpdate() > 0;
            if(success && oldBalance != clamped) {
                 Bukkit.getScheduler().runTask(plugin, () -> {
                     Bukkit.getPluginManager().callEvent(new BalanceChangeEvent(playerUuid, (double) oldBalance, (double) clamped));
                 });
            }
            return success;
        } catch (SQLException e) {
            plugin.getLogger().severe("Balance güncellenemedi: " + e.getMessage());
            return false;
        }
    }

    public void setBalanceAsync(UUID playerUuid, double balance) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> setBalance(playerUuid, balance));
    }

    public void addBalanceAsync(UUID playerUuid, double amount) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            double oldBalance = getBalance(playerUuid);
            double newBalance = oldBalance + amount;
            if (setBalance(playerUuid, newBalance)) {
                // set balance kısmı buradaki event'i zaten tetikliyor, bu yüzden burada tekrar tetiklemeye gerek yok.
            }
        });
    }

    public boolean removeBalance(UUID playerUuid, double amount) {
        Connection conn = connection();
        if (conn == null || playerUuid == null) return false;

        double current = getBalance(playerUuid);
        if (current < amount) return false;

        double newBalance = current - amount;
        return setBalance(playerUuid, newBalance);
    }

    public void exchangeBal(UUID player, UUID target, double amount) {
        if (player == null || target == null || amount <= 0) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            double playerBalance = getBalance(player);
            if (playerBalance < amount) {
                plugin.getLogger().warning(SkyblockCore.getInstance().getMessageManager().getMessage("economy.not-enough-money"));
                return;
            }

            double targetBalance = getBalance(target);

            double newPlayerBalance = playerBalance - amount;
            double newTargetBalance = targetBalance + amount;

            setBalance(player, newPlayerBalance);
            setBalance(target, newTargetBalance);
        });
    }

    public List<BalanceEntry> getTopBalances(int limit, int offset) {
        List<BalanceEntry> result = new ArrayList<>();
        Connection conn = connection();
        if (conn == null) return result;

        String sql = "SELECT username, balance FROM sb_accounts ORDER BY balance DESC LIMIT ? OFFSET ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new BalanceEntry(rs.getString("username"), rs.getDouble("balance")));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Baltop okunamadı: " + e.getMessage());
        }
        return result;
    }

    public int getTotalAccountCount() {
        Connection conn = connection();
        if (conn == null) return 0;

        String sql = "SELECT COUNT(*) FROM sb_accounts";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            plugin.getLogger().severe("Hesap sayısı okunamadı: " + e.getMessage());
        }
        return 0;
    }

    public static class BalanceEntry {
        private final String username;
        private final double balance;

        public BalanceEntry(String username, double balance) {
            this.username = username;
            this.balance = balance;
        }

        public String getUsername() {
            return username;
        }

        public double getBalance() {
            return balance;
        }
    }
}