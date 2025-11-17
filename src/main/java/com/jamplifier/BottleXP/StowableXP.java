package com.jamplifier.BottleXP;

import java.util.List;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class StowableXP extends JavaPlugin {
    private static StowableXP instance;

    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();
        getLogger().info("StowableXP enabled!");
        getLogger().info("Thank you for using my plugin <3");
        getServer().getPluginManager().registerEvents(new XPBottleUseListener(), (Plugin) this);
        if (getConfig().getBoolean("enable-bottles", true)) {
            getCommand("bottlexp").setExecutor(new BottleXPCommand(false));
        } else {
            getLogger().info("XP Bottles are disabled in the config.");
        }
        if (getConfig().getBoolean("enable-books", true)) {
            getCommand("bookxp").setExecutor(new BottleXPCommand(true));
        } else {
            getLogger().info("XP Books are disabled in the config.");
        }
        getCommand("stowablexp").setExecutor(new MainCommand(this));
        TypeForPlayer tabCompleter = new TypeForPlayer();
        getCommand("stowablexp").setTabCompleter(tabCompleter);
        getCommand("bottlexp").setTabCompleter(tabCompleter);
        getCommand("bookxp").setTabCompleter(tabCompleter);
    }

    public void onDisable() {
        getLogger().info("StowableXP disabled!");
    }

    public static StowableXP getInstance() {
        return instance;
    }

    /**
     * Returns true if SXP redemption is blocked in the given world.
     * World names are matched case-insensitively.
     */
    public boolean isRedeemBlockedInWorld(String worldName) {
        if (worldName == null) return false;
        List<String> blocked = getConfig().getStringList("blocked-redeem-worlds");
        if (blocked == null || blocked.isEmpty()) return false;

        for (String w : blocked) {
            if (w != null && w.equalsIgnoreCase(worldName)) {
                return true;
            }
        }
        return false;
    }
}
