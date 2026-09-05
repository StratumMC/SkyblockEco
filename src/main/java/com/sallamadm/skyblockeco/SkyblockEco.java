package com.sallamadm.skyblockeco;

import com.sallamadm.skyblockcore.SkyblockCore;
import com.sallamadm.skyblockeco.commands.BalTopCommand;
import com.sallamadm.skyblockeco.commands.BalanceCommand;
import com.sallamadm.skyblockeco.commands.PayCommand;
import com.sallamadm.skyblockeco.hooks.EconomyHook;
import com.sallamadm.skyblockeco.listeners.BalanceChangeListener;
import com.sallamadm.skyblockeco.data.DataManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkyblockEco extends JavaPlugin {

    private DataManager dataManager;
    private static SkyblockEco instance;
    SkyblockCore core = SkyblockCore.getInstance();


    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(false));
    }

    @Override
    public void onEnable() {
        instance = this;

        CommandAPI.onEnable();

        this.dataManager = new DataManager(this);

        getServer().getPluginManager().registerEvents(new BalanceChangeListener(core), this);

        EconomyHook.register();

        BalanceCommand.registerCommand(this);
        PayCommand.registerCommand(this);
        BalTopCommand.registerCommand(this);

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
