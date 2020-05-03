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
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

// Debug commands please ignore
// I know it's ugly, it's meant just for me to debug -Violet
public class DebugCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        XTalisman instance = XTalisman.getInstance();

        if (!(sender instanceof Player) || !instance.isEnableDebugCommand()) {
            return true;
        }

        Player player = (Player) sender;

        if (player.getName().equals("violetwtf") && sender.isOp()) {
            PlayerInventory inventory = player.getInventory();
            ItemStack hand = inventory.getItemInHand();

            switch (args[0]) {
                case "make":
                    Talisman.give(instance.getTalismanTypeByName(args[1]), player);
                    break;
                case "log":
                    boolean enabled = !instance.isDebugLogEnabled();
                    instance.setDebugLogEnabled(enabled);
                    sender.sendMessage("Debug logging: " + enabled);
                    break;
                case "nbt":
                    NBTItem item = new NBTItem(hand);
                    sender.sendMessage(
                        "TALISMAN: " + item.getBoolean(Talisman.NBT_TALISMAN) +
                            "\nHELD: " + item.getInteger(Talisman.NBT_HELD) +
                            "\nMAX: " + item.getInteger(Talisman.NBT_MAX) +
                            "\nTYPE: " + item.getString(Talisman.NBT_TYPE) +
                            "\nID: " + item.getString(Talisman.NBT_ID) +
                            "\nRECEIVER_UUID: " + item.getString(Talisman.NBT_RECEIVER_UUID) +
                            "\nRECEIVER_IGN: " + item.getString(Talisman.NBT_RECEIVER_IGN)
                    );
                    break;
                case "addexp":
                    Talisman talisman = Talisman.of(hand);
                    if (talisman.canAddExp()) {
                        talisman.addExp(
                            Integer.parseInt(args[1]),
                            player
                        );
                        sender.sendMessage("Added XP");
                    } else {
                        sender.sendMessage("Cannot add XP (Talisman full!)");
                    }
                    break;
                case "reward":
                    Talisman.of(hand).doReward(player);
                    break;
                default:
                    sender.sendMessage("Unknown debug command");
                    break;
            }
        } else {
            sender.sendMessage(XTalisman.HELP_MESSAGE);
        }

        return true;
    }
}
