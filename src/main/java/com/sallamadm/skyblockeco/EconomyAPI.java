package com.sallamadm.skyblockeco;


import java.util.UUID;

public class EconomyAPI {

    private EconomyAPI() {}


    private static SkyblockEco eco() {
        return SkyblockEco.getInstance();
    }

    private static boolean ready() {
        SkyblockEco inst = eco();
        return inst != null && inst.getDataManager() != null;
    }


    public static long getBalance(UUID playerUuid) {
        if (!ready()) return 0L;
        return eco().getDataManager().getBalance(playerUuid);
    }

    public static boolean setBalance(UUID playerUuid, long amount) {
        if (!ready()) return false;
        return eco().getDataManager().setBalance(playerUuid, amount);
    }

    public static void addBalance(UUID playerUuid, long amount) {
        if (!ready()) return;
        eco().getDataManager().addBalanceAsync(playerUuid, amount);
    }

    public static boolean removeBalance(UUID playerUuid, long amount) {
        if (!ready()) return false;
        return eco().getDataManager().removeBalance(playerUuid, amount);
    }

    public static boolean hasBalance(UUID playerUuid, long amount) {
        if (!ready()) return false;
        return eco().getDataManager().hasBalance(playerUuid, amount);
    }
}