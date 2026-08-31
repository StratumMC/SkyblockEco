package com.sallamadm.skyblockeco.data;
import com.sallamadm.skyblockeco.SkyblockEco;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
public class DataManager {
    private static final long DEFAULT_BALANCE = 500L;

    private final SkyblockEco plugin;
    private Connection connection;

    public DataManager(SkyblockEco plugin) {
        this.plugin = plugin;
        connect();
        createTables();
    }

    public void loadData() {
        if (connection == null) {
            return;
        }
        plugin.getLogger().info("DataManager yuklendi.");
    }

    public void saveDataSync() {
        if (connection == null) {
            return;
        }
        plugin.getLogger().info("DataManager saveDataSync cagrildi.");
    }

    private void connect() {
        String host = plugin.getConfig().getString("mysql.host", "localhost");
        int port = plugin.getConfig().getInt("mysql.port", 3306);
        String database = plugin.getConfig().getString("mysql.database", "skyblock");
        String username = plugin.getConfig().getString("mysql.username", "root");
        String password = plugin.getConfig().getString("mysql.password", "");

        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true",
                    username,
                    password
            );
            plugin.getLogger().info("MySQL baglandi.");
        } catch (Exception e) {
            plugin.getLogger().severe("MySQL baglanamadi: " + e.getMessage());
        }
    }

    private void createTables() {
        if (connection == null) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE sb_accounts ADD COLUMN balance BIGINT NOT NULL DEFAULT 500");
        } catch (SQLException e) {
            if (!"42S21".equals(e.getSQLState())) {
                plugin.getLogger().severe("sb_accounts.balance eklenemedi: " + e.getMessage());
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("UPDATE sb_accounts SET balance = 500 WHERE balance IS NULL");
        } catch (SQLException e) {
            plugin.getLogger().severe("sb_accounts.balance duzeltilemedi: " + e.getMessage());
        }
    }

    public long getBalance(UUID playerUuid) {
        if (connection == null || playerUuid == null) {
            return DEFAULT_BALANCE;
        }

        String query = "SELECT balance FROM sb_accounts WHERE uuid = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUuid.toString());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("balance");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Balance okunamadi: " + e.getMessage());
        }

        return DEFAULT_BALANCE;
    }

    public boolean setBalance(UUID playerUuid, long balance) {
        if (connection == null || playerUuid == null) {
            return false;
        }

        String query = "UPDATE sb_accounts SET balance = ? WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, balance);
            statement.setString(2, playerUuid.toString());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Balance guncellenemedi: " + e.getMessage());
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("MySQL baglantisi kapatilamadi: " + e.getMessage());
        }
    }
}