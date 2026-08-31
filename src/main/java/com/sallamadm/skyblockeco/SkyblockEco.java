package com.sallamadm.skyblockeco;

import com.sallamadm.skyblockeco.data.DataManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkyblockEco extends JavaPlugin {

    private DataManager dataManager;


    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(false));
    }

    @Override
    public void onEnable() {
        getConfig().addDefault("mysql.host", "localhost");
        getConfig().addDefault("mysql.port", 3306);
        getConfig().addDefault("mysql.database", "skyblockeco");
        getConfig().addDefault("mysql.username", "root");
        getConfig().addDefault("mysql.password", "");
        getConfig().options().copyDefaults(true);
        saveConfig();

        CommandAPI.onEnable();

        this.dataManager = new DataManager(this);
        this.dataManager.loadData();
    }

    @Override
    public void onDisable() {
        if(dataManager != null) {
            dataManager.closeConnection();
            dataManager.saveDataSync();
        }
        CommandAPI.onDisable();
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
