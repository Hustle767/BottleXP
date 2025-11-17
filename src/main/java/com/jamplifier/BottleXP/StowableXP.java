package com.jamplifier.BottleXP;

import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public class StowableXP extends JavaPlugin {
    private static StowableXP instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();
        getLogger().info("StowableXP enabled!");
        getLogger().info("Thank you for using my plugin <3");


        getServer().getPluginManager().registerEvents(new XPBottleUseListener(), this);

        if (getConfig().getBoolean("enable-bottles", true)) {
            if (getCommand("bottlexp") != null) {
                getCommand("bottlexp").setExecutor(new BottleXPCommand());
            } else {
                getLogger().warning("Command 'bottlexp' is not defined in plugin.yml!");
            }
        } else {
            getLogger().info("XP Bottles are disabled in the config.");
        }

        if (getCommand("stowablexp") != null) {
            getCommand("stowablexp").setExecutor(new MainCommand(this));

  
            TypeForPlayer tabCompleter = new TypeForPlayer();
            getCommand("stowablexp").setTabCompleter(tabCompleter);
            if (getCommand("bottlexp") != null) {
                getCommand("bottlexp").setTabCompleter(tabCompleter);
            }
        } else {
            getLogger().warning("Command 'stowablexp' is not defined in plugin.yml!");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("StowableXP disabled!");
    }

    public static StowableXP getInstance() {
        return instance;
    }


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
