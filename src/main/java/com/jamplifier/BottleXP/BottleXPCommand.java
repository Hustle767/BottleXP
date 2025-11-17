package com.jamplifier.BottleXP;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Locale;

public class BottleXPCommand implements CommandExecutor {

  private final NamespacedKey xpLevelsKey =
      new NamespacedKey((Plugin) StowableXP.getInstance(), "stored_xp_levels"); // kept only for legacy compatibility (read-only elsewhere)
  private final NamespacedKey xpPointsKey =
      new NamespacedKey((Plugin) StowableXP.getInstance(), "stored_xp_points");

  private final boolean isBook;

  public BottleXPCommand(boolean isBook) {
    this.isBook = isBook;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.RED + "Only players can use this command.");
      return true;
    }
    Player player = (Player) sender;

    final Plugin plugin = StowableXP.getInstance();
    final boolean debug = plugin.getConfig().getBoolean("debug-mode", false);

    if (!this.isBook && !plugin.getConfig().getBoolean("enable-bottles", true)) {
      player.sendMessage(ChatColor.RED + "XP Bottles are disabled on this server.");
      return true;
    }
    if (this.isBook && !plugin.getConfig().getBoolean("enable-books", true)) {
      player.sendMessage(ChatColor.RED + "XP Books are disabled on this server.");
      return true;
    }

    // Usage: points only now
    if (args.length < 1) {
      player.sendMessage(ChatColor.YELLOW + "/" + label + " <amount>");
      return true;
    }

    final double amount;
    try {
      amount = Double.parseDouble(args[0]);
      if (amount <= 0.0D) {
        player.sendMessage(ChatColor.RED + "Amount must be greater than 0.");
        return true;
      }
    } catch (NumberFormatException e) {
      player.sendMessage(ChatColor.RED + "Invalid number: " + ChatColor.YELLOW + args[0]);
      return true;
    }

    // Max per item (points). Backward-compat: fall back to old level keys if new ones absent.
    final int maxPoints = this.isBook
        ? plugin.getConfig().getInt("max-xp-points-book",
            plugin.getConfig().getInt("max-xp-levels-book", -1))
        : plugin.getConfig().getInt("max-xp-points-bottle",
            plugin.getConfig().getInt("max-xp-levels-bottle", -1));
    if (maxPoints > 0 && amount > maxPoints) {
      player.sendMessage(ChatColor.RED + "You cannot store more than "
          + ChatColor.YELLOW + maxPoints + ChatColor.RED + " points in one "
          + (this.isBook ? "book" : "bottle") + "!");
      return true;
    }

    // Check player has enough raw XP points
    final double currentXP = getTotalExperience(player);
    if (currentXP < amount) {
      player.sendMessage(ChatColor.RED + "You don't have enough XP points!");
      return true;
    }

    // Remove points
    setTotalExperience(player, currentXP - amount);

    // Create item (points only)
    final Material itemType = this.isBook ? Material.KNOWLEDGE_BOOK : Material.EXPERIENCE_BOTTLE;
    final String primary = this.isBook ? ChatColor.DARK_AQUA.toString() : ChatColor.GOLD.toString();
    final String accent = ChatColor.GRAY.toString();
    final String reset = ChatColor.RESET.toString();

    ItemStack item = new ItemStack(itemType, 1);
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      String itemName = primary + (this.isBook ? "XP Book" : "XP Bottle") + reset
          + accent + " (Points " + formatXP(amount) + ")";
      meta.setDisplayName(itemName);
      meta.getPersistentDataContainer().set(this.xpPointsKey, PersistentDataType.DOUBLE, amount);
      // do NOT set levels key anymore
      item.setItemMeta(meta);
    }

    if (debug) {
      plugin.getLogger().info("(DEBUG) Created " + (this.isBook ? "XP Book" : "XP Bottle")
          + " with " + amount + " points for " + player.getName());
    }

    player.getInventory().addItem(item);
    player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.PLAYERS, 1.0F, 0.7F);
    player.sendMessage(
    	    primary + "Stored " + ChatColor.WHITE + formatXP(amount)
    	    + " XP Point" + (Double.compare(amount, 1.0D) == 0 ? "" : "s")
    	    + reset
    	);
	return true;
  }

  private String formatXP(double value) {
    return (value % 1.0D == 0.0D)
        ? String.valueOf((int) value)
        : String.format(Locale.ROOT, "%.2f", value);
  }

  /** Total raw XP points for current level/progress using vanilla curve. */
  private double getTotalExperience(Player player) {
    double total = 0.0D;
    for (int i = 0; i < player.getLevel(); i++) {
      total += getExpAtLevel(i);
    }
    total += (player.getExp() * getExpAtLevel(player.getLevel()));
    return total;
  }

  /** Sets total raw XP points (distributes into level + progress). */
  private void setTotalExperience(Player player, double amount) {
    player.setExp(0.0F);
    player.setLevel(0);
    player.setTotalExperience(0);

    double remaining = Math.max(0.0D, amount);
    int level = 0;
    while (remaining >= getExpAtLevel(level)) {
      remaining -= getExpAtLevel(level);
      level++;
    }
    player.setLevel(level);
    player.setExp(getExpAtLevel(level) > 0 ? (float) (remaining / getExpAtLevel(level)) : 0.0F);
  }

  /** Vanilla XP required to go from level N to N+1. */
  private int getExpAtLevel(int level) {
    if (level <= 15) return 2 * level + 7;
    if (level <= 30) return 5 * level - 38;
    return 9 * level - 158;
  }
}
