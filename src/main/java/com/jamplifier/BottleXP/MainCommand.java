package com.jamplifier.BottleXP;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommand implements CommandExecutor {
    private final StowableXP plugin;

    public MainCommand(StowableXP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(sender);
                return true;
            case "info":
                sendInfo(sender);
                return true;
            case "reload":
                if (!sender.hasPermission("stowablexp.reload")) {
                    sender.sendMessage("You don't have permission to reload the plugin.");
                    return true;
                }
                this.plugin.reloadConfig();
                sender.sendMessage("Configuration reloaded.");
                return true;
        }

        sender.sendMessage("Unknown subcommand. Use /" + label + " help");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("---- StowableXP Help ----");
        sender.sendMessage("/stowablexp help   - Show this help message");
        sender.sendMessage("/stowablexp info   - Show plugin info");
        sender.sendMessage("/stowablexp reload - Reload the plugin config");
        sender.sendMessage("/bottlexp <amount> - Withdraw that many XP points into a bottle");
        sender.sendMessage("/bottlexp all      - Withdraw ALL your XP into bottle(s)");
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage("StowableXP v" + this.plugin.getDescription().getVersion());
        sender.sendMessage("Author(s): " + this.plugin.getDescription().getAuthors());
        sender.sendMessage("Store and withdraw your XP in bottles.");
    }
}
