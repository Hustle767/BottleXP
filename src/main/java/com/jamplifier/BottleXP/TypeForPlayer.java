package com.jamplifier.BottleXP;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class TypeForPlayer implements TabCompleter {
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    List<String> completions = new ArrayList<>();
    if (command.getName().equalsIgnoreCase("stowablexp") && 
      args.length == 1) {
      completions.add("help");
      completions.add("info");
      completions.add("reload");
    } 
    if ((command.getName().equalsIgnoreCase("bottlexp") || command.getName().equalsIgnoreCase("bookxp")) && 
      args.length == 2) {
      completions.add("points");
    } 
    return completions;
  }
}
