package com.jamplifier.BottleXP;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommand implements CommandExecutor {
  private final StowableXP plugin;
  
  public MainCommand(StowableXP plugin) {
    this.plugin = plugin;
  }
  
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
          sender.sendMessage("don't have permission to reload the plugin.");
          return true;
        } 
        this.plugin.reloadConfig();
        sender.sendMessage("configuration reloaded.");
        return true;
    } 
    sender.sendMessage("subcommand. Use help");
    return true;
  }
  
  private void sendHelp(CommandSender sender) {
    sender.sendMessage("StowableXP Help ---");
    sender.sendMessage("help Show this help message");
    sender.sendMessage("info Show plugin info");
    sender.sendMessage("reload Reload the plugin config");
    sender.sendMessage("<amount> [points] Withdraw XP into a bottle");
    sender.sendMessage("<amount> [points] Withdraw XP into a book");
  }
  
  private void sendInfo(CommandSender sender) {
    sender.sendMessage("v" + this.plugin.getDescription().getVersion());
    sender.sendMessage("+ String.valueOf(this.plugin.getDescription().getAuthors()));");
    sender.sendMessage("and withdraw your XP in bottles and books!");
  }
}
