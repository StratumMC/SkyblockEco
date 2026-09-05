package com.sallamadm.skyblockeco.data;
import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockeco.SkyblockEco;
import com.sallamadm.skyblockeco.events.BalanceChangeEvent;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.UUID;

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

    public boolean hasBalance(UUID playerUuid, double amount) {
        return getBalance(playerUuid) >= amount;
    }
}