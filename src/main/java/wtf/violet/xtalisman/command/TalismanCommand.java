/*
 * Created 5/2/2020 by Violet M.
 *
 * Copyright (c) 2020 Violet M. <vi@violet.wtf>.
 * Copyright (c) 2020 [redacted] <https://[redacted]>
 * All Rights Reserved.
 */

package wtf.violet.xtalisman.command;

import wtf.violet.xtalisman.XTalisman;
import wtf.violet.xtalisman.model.Talisman;
import wtf.violet.xtalisman.model.TalismanType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class TalismanCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp() || sender.hasPermission("xtalisman.admin")) {
            if (args.length == 0) {
                help(sender);
            } else {
                switch (args[0]) {
                    case "give": give(sender, args); break;
                    case "fill": fill(sender); break;
                    case "check": check(sender); break;
                    default: help(sender); break;
                }
            }
        } else {
            sender.sendMessage(XTalisman.HELP_MESSAGE);
        }

        return true;
    }

    private static void help(CommandSender sender) {
        sender.sendMessage(
            ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "TALISMAN HELP" + "\n" +
                definition("give <player> <type> [amount]", "Give a player a Talisman") +
                definition("fill", "Fill the Talisman in your hand to max exp") +
                definition("check", "Info about your Talisman")
        );
    }

    private static void give(CommandSender sender, String[] args) {
        if (args.length > 2) {
            String playerName = args[1];
            String typeName = args[2];
            int amount = 1;

            if (args.length > 3) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (Throwable rock) {
                    error(sender, "Amount must be an integer");
                    return;
                }
            }

            if (amount < 1) {
                error(sender, "Amount must be at least 1.");
                return;
            }

            Player player = Bukkit.getPlayer(playerName);

            if (player == null) {
                error(sender, playerName + " is not online");
                return;
            }

            TalismanType type = XTalisman.getInstance().getTalismanTypeByName(typeName);

            if (type == null) {
                error(sender, typeName + " is not a valid Talisman");
                return;
            }

            Talisman talisman = null;

            for (int i = 0; i < amount; i++) {
                talisman = Talisman.give(type, player);
            }

            success(
                sender,
                "Gave " + playerName + " " + talisman.getColouredName() + " "
            );
        }
    }

    private static void fill(CommandSender sender) {
        Talisman talisman = getSenderTalisman(sender);

        if (talisman == null) {
            return;
        }

        if (talisman.canAddExp()) {
            talisman.addExp(talisman.getType().getMaxExp(), (Player) sender);
            success(sender, "Filled your Talisman!");
        } else {
            error(sender, "This Talisman is already full");
        }
    }

    private static void check(CommandSender sender) {
        Talisman talisman = getSenderTalisman(sender);

        if (talisman == null) {
            return;
        }

        String ign = talisman.getReceiverIgn();
        String uuid = talisman.getReceiverUuid();

        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        if (player != null) {
            ign = player.getName();
        }

        sender.sendMessage(
            ChatColor.GRAY + "This is Talisman ID " + ChatColor.LIGHT_PURPLE + talisman.getId() +
                ChatColor.GRAY + ", originating from player " + ChatColor.LIGHT_PURPLE +
                ign + ChatColor.GRAY + "(" + uuid + ")."
        );
    }

    public static Talisman getSenderTalisman(CommandSender sender) {
        if (!(sender instanceof Player)) {
            error(sender, "You must be a player to use this command");
            return null;
        }

        ItemStack hand = ((Player) sender).getInventory().getItemInHand();

        // NBT breaks if this check isn't done
        if (hand.getType() == Material.AIR) {
            error(sender, "You must be holding an item");
            return null;
        }

        Talisman talisman = Talisman.of(hand);

        if (talisman == null) {
            error(sender, "You must be holding a Talisman");
        }

        return talisman;
    }

    private static String definition(String command, String definition) {
        return "\n" +
            ChatColor.LIGHT_PURPLE + "/talisman " + command + ": " +
            ChatColor.GRAY + definition + ".";
    }

    private static void error(CommandSender sender, String error) {
        sender.sendMessage(ChatColor.RED + error + ".");
    }

    private static void success(CommandSender sender, String success) {
        sender.sendMessage(ChatColor.GREEN + success);
    }

}
