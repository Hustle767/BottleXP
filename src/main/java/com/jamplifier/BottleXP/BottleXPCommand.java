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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Locale;

public class BottleXPCommand implements CommandExecutor {

    private final NamespacedKey xpPointsKey =
            new NamespacedKey((Plugin) StowableXP.getInstance(), "stored_xp_points");

    public BottleXPCommand() {
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

        if (!plugin.getConfig().getBoolean("enable-bottles", true)) {
            player.sendMessage(ChatColor.RED + "XP Bottles are disabled on this server.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "/" + label + " <amount|all>");
            return true;
        }

        final boolean withdrawAll = args[0].equalsIgnoreCase("all");

        // Total XP the player currently has (points, not levels)
        final double currentXP = getTotalExperience(player);
        if (currentXP <= 0.0D) {
            player.sendMessage(ChatColor.RED + "You don't have any XP to store.");
            return true;
        }

        final int maxPoints = plugin.getConfig().getInt(
                "max-xp-points-bottle",
                plugin.getConfig().getInt("max-xp-levels-bottle", -1)
        );

        // --------------------------
        // /bottlexp all
        // --------------------------
        if (withdrawAll) {
            if (maxPoints > 0 && currentXP > maxPoints) {
                int fullBottles = (int) (currentXP / maxPoints);
                double remainder = currentXP % maxPoints;
                int bottlesNeeded = fullBottles + (remainder > 0.0D ? 1 : 0);

                int empty = countEmptySlots(player);
                if (empty < bottlesNeeded) {
                    player.sendMessage(
                            ChatColor.RED + "Your inventory is full! You need at least "
                                    + ChatColor.YELLOW + bottlesNeeded
                                    + ChatColor.RED + " free slot"
                                    + (bottlesNeeded == 1 ? "" : "s")
                                    + " to store all of your XP."
                    );
                    return true;
                }

                setTotalExperience(player, 0.0D);

                int bottleCount = 0;
                for (int i = 0; i < fullBottles; i++) {
                    giveBottle(player, maxPoints, plugin, debug, false);
                    bottleCount++;
                }
                if (remainder > 0.0D) {
                    giveBottle(player, remainder, plugin, debug, false);
                    bottleCount++;
                }

                // One sound + one message instead of chat spam
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.PLAYERS, 1.0F, 0.7F);
                player.sendMessage(
                        ChatColor.GOLD + "Stored " + ChatColor.WHITE + formatXP(currentXP)
                                + " XP Points " + ChatColor.GOLD + "into "
                                + ChatColor.YELLOW + bottleCount
                                + ChatColor.GOLD + " bottle" + (bottleCount == 1 ? "" : "s") + "."
                );
                return true;
            } else {
                // Either no cap or total XP fits in a single bottle
                if (countEmptySlots(player) < 1) {
                    player.sendMessage(
                            ChatColor.RED + "Your inventory is full! You need at least "
                                    + ChatColor.YELLOW + "1"
                                    + ChatColor.RED + " free slot to store your XP."
                    );
                    return true;
                }

                double amount = currentXP;
                setTotalExperience(player, 0.0D);
                giveBottle(player, amount, plugin, debug, true);
                return true;
            }
        }

        // --------------------------
        // /bottlexp <amount>
        // --------------------------
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

        if (maxPoints > 0 && amount > maxPoints) {
            player.sendMessage(ChatColor.RED + "You cannot store more than "
                    + ChatColor.YELLOW + maxPoints + ChatColor.RED + " points in one bottle!");
            return true;
        }

        if (currentXP < amount) {
            player.sendMessage(ChatColor.RED + "You don't have enough XP points!");
            return true;
        }

        // Need at least one free slot for this bottle
        if (countEmptySlots(player) < 1) {
            player.sendMessage(
                    ChatColor.RED + "Your inventory is full! You need at least "
                            + ChatColor.YELLOW + "1"
                            + ChatColor.RED + " free slot to store your XP."
            );
            return true;
        }

        setTotalExperience(player, currentXP - amount);
        giveBottle(player, amount, plugin, debug, true);
        return true;
    }

    /**
     * Give an XP bottle to the player with the specified amount stored.
     *
     * @param sendMessage whether to send the standard "Stored X XP" chat message & play sound
     */
    private void giveBottle(Player player, double amount, Plugin plugin, boolean debug, boolean sendMessage) {
        final Material itemType = Material.EXPERIENCE_BOTTLE;
        final String primary = ChatColor.GOLD.toString();
        final String accent = ChatColor.GRAY.toString();
        final String reset = ChatColor.RESET.toString();

        ItemStack item = new ItemStack(itemType, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String itemName = primary + "XP Bottle" + reset
                    + accent + " (Points " + formatXP(amount) + ")";
            meta.setDisplayName(itemName);
            meta.getPersistentDataContainer().set(this.xpPointsKey, PersistentDataType.DOUBLE, amount);
            item.setItemMeta(meta);
        }

        if (debug) {
            plugin.getLogger().info("(DEBUG) Created XP Bottle with " + amount + " points for " + player.getName());
        }

        player.getInventory().addItem(item);

        if (sendMessage) {
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.PLAYERS, 1.0F, 0.7F);
            player.sendMessage(
                    primary + "Stored " + ChatColor.WHITE + formatXP(amount)
                            + " XP Point" + (Double.compare(amount, 1.0D) == 0 ? "" : "s")
                            + reset
            );
        }
    }

    private int countEmptySlots(Player player) {
        PlayerInventory inv = player.getInventory();
        int empty = 0;
        // getStorageContents() = main inventory + hotbar (no armor/offhand)
        ItemStack[] contents = inv.getStorageContents();
        for (ItemStack stack : contents) {
            if (stack == null || stack.getType() == Material.AIR) {
                empty++;
            }
        }
        return empty;
    }

    private String formatXP(double value) {
        return (value % 1.0D == 0.0D)
                ? String.valueOf((int) value)
                : String.format(Locale.ROOT, "%.2f", value);
    }

    private double getTotalExperience(Player player) {
        double total = 0.0D;
        for (int i = 0; i < player.getLevel(); i++) {
            total += getExpAtLevel(i);
        }
        total += (player.getExp() * getExpAtLevel(player.getLevel()));
        return total;
    }

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

    private int getExpAtLevel(int level) {
        if (level <= 15) return 2 * level + 7;
        if (level <= 30) return 5 * level - 38;
        return 9 * level - 158;
    }
}
