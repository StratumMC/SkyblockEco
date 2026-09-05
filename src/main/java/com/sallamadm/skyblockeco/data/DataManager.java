package com.sallamadm.skyblockeco.data;
import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockeco.SkyblockEco;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.UUID;

public class DataManager {
    private static final long DEFAULT_BALANCE = 500L;

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

    public long getBalance(UUID playerUuid) {
        Connection conn = connection();
        if (conn == null || playerUuid == null) return DEFAULT_BALANCE;

        String sql = "SELECT balance FROM sb_accounts WHERE uuid = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("balance");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Balance okunamadı: " + e.getMessage());
        }
        return DEFAULT_BALANCE;
    }

    public boolean setBalance(UUID playerUuid, long balance) {
        Connection conn = connection();
        if (conn == null || playerUuid == null) return false;

        long clamped = Math.max(0L, balance);
        String sql = "UPDATE sb_accounts SET balance = ? WHERE uuid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, clamped);
            ps.setString(2, playerUuid.toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Balance güncellenemedi: " + e.getMessage());
            return false;
        }
    }

    public void setBalanceAsync(UUID playerUuid, long balance) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> setBalance(playerUuid, balance));
    }

    public void addBalanceAsync(UUID playerUuid, long amount) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            long current = getBalance(playerUuid);
            setBalance(playerUuid, current + amount);
        });
    }

    public boolean removeBalance(UUID playerUuid, long amount) {
        Connection conn = connection();
        if (conn == null || playerUuid == null) return false;

        long current = getBalance(playerUuid);
        if (current < amount) return false;
        return setBalance(playerUuid, current - amount);
    }

    public boolean hasBalance(UUID playerUuid, long amount) {
        return getBalance(playerUuid) >= amount;
    }
}