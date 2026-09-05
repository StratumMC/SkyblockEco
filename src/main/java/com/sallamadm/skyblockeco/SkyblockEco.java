package com.sallamadm.skyblockeco;

import com.sallamadm.skyblockeco.commands.BalanceCommand;
import com.sallamadm.skyblockeco.data.DataManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkyblockEco extends JavaPlugin {

    private DataManager dataManager;
    private static SkyblockEco instance;


    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(false));
    }

    @Override
    public void onEnable() {
        instance = this;

        CommandAPI.onEnable();

        this.dataManager = new DataManager(this);

        BalanceCommand.registerCommand(this);

        getLogger().info("SkyblockEco aktif.");
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();

        getLogger().info("SkyblockEco kapatıldı.");
    }

    public static SkyblockEco getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
