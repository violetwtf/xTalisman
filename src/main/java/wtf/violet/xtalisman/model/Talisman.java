/*
 * Created 5/2/2020 by Violet M.
 *
 * Copyright (c) 2020 Violet M. <vi@violet.wtf>.
 * Copyright (c) 2020 [redacted] <https://[redacted]>
 * All Rights Reserved.
 */

package wtf.violet.xtalisman.model;

import wtf.violet.xtalisman.XTalisman;
import wtf.violet.xtalisman.exception.RewardFailedException;
import wtf.violet.xtalisman.model.reward.Reward;
import wtf.violet.xtalisman.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Represents a Talisman item.
 * @author Violet M. vi@violet.wtf
 */
public final class Talisman {

    public static final String NBT_TALISMAN = "isTalisman";
    public static final String NBT_HELD = "talismanHeldExp";
    public static final String NBT_MAX = "talismanMaxExp";
    public static final String NBT_TYPE = "talismanType";
    public static final String NBT_ID = "talismanId";
    public static final String NBT_RECEIVER_UUID = "talismanReceiverUuid";
    public static final String NBT_RECEIVER_IGN = "talismanReceiverIgn";

    private static final int PROGRESS_BAR_LENGTH = 30;

    private ItemStack item;
    private NBTItem nbtItem;
    private int heldExp, maxExp;
    private TalismanType type;
    private String receiverUuid, id, receiverIgn;

    /** Returns true if you can add XP, false if you can't. */
    public boolean canAddExp() {
        return heldExp < maxExp;
    }

    /**
     * Constructs a base Talisman class.
     *
     * @param nbtItem The NBTItem instance
     * @param heldExp XP held in the talisman
     * @param maxExp Max XP that the item can hold
     */
    private Talisman(
        NBTItem nbtItem,
        int heldExp,
        int maxExp,
        String id,
        String receiverUuid,
        String receiverIgn,
        TalismanType type
    ) {
        this.nbtItem = nbtItem;
        this.heldExp = heldExp;
        this.maxExp = maxExp;
        this.type = type;
        this.receiverUuid = receiverUuid;
        this.receiverIgn = receiverIgn;
        this.id = id;

        item = nbtItem.getItem();

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(getColouredName());
        item.setItemMeta(meta);
    }

    /**
     * Adds XP to the NBT, lore, and object.
     * @param amount Amount of XP to add
     * @param player Player to add the item to
     */
    public void addExp(int amount, Player player) {
        PlayerInventory inventory = player.getInventory();
        int slot = inventory.first(item);

        heldExp += amount;

        nbtItem.setInteger(NBT_HELD, heldExp);
        item = nbtItem.getItem();

        int add = heldExp - maxExp;

        String lastLoreLine = "Current Exp: " +
            ChatColor.GRAY +
            heldExp + "/" + maxExp;

        XTalisman instance = XTalisman.getInstance();

        // Reward time!
        if (add >= 0) {
            heldExp = maxExp;
            player.giveExp(add);
            lastLoreLine = ChatColor.BOLD + "CHARGED";
            player.sendMessage(name(instance.getTalismanChargedMessage()));
        }

        double ratio = (double) heldExp / maxExp;
        int progress = (int) (ratio * PROGRESS_BAR_LENGTH);

        ItemMeta meta = item.getItemMeta();
        List<String> lore = Arrays.asList(
            "",
            ChatColor.GRAY + "Charge this talisman with exp to",
            ChatColor.GRAY + "reveal an exclusive reward!",
            "",
            ChatColor.GREEN.toString() +
                ChatColor.BOLD +
                colons(progress) +
                ChatColor.RED +
                ChatColor.BOLD +
                colons(PROGRESS_BAR_LENGTH - progress) +
                ChatColor.GRAY +
                " (" + (int) (ratio * 100) + "%)",
            "",
            type.getColor() + lastLoreLine
        );

        meta.setLore(lore);

        item.setItemMeta(meta);

        inventory.setItem(slot, item);
    }

    /**
     * Create a Talisman from a Spigot ItemStack. If there's no Talisman data to be extracted,
     * It'll return null.
     *
     * @param vanillaItem The ItemStack to create it from
     * @return The created Talisman instance
     */
    public static Talisman of(ItemStack vanillaItem) {
        NBTItem item = new NBTItem(vanillaItem);

        if (!item.hasNBTData() || !item.hasKey(NBT_TALISMAN)) {
            return null;
        }

        return new Talisman(
            item,
            item.getInteger(NBT_HELD),
            item.getInteger(NBT_MAX),
            item.getString(NBT_ID),
            item.getString(NBT_RECEIVER_UUID),
            item.getString(NBT_RECEIVER_IGN),
            XTalisman.getInstance().getTalismanTypeByName(item.getString(NBT_TYPE))
        );
    }

    public static Talisman give(TalismanType type, Player player) {
        ItemStack stack = new ItemStack(Material.DIAMOND, 1);
        NBTItem item = new NBTItem(stack);

        String id = UUID.randomUUID().toString();
        String uuid = player.getUniqueId().toString();
        String playerName = player.getName();

        item.setBoolean(NBT_TALISMAN, true);
        item.setInteger(NBT_HELD, 0);
        item.setInteger(NBT_MAX, type.getMaxExp());
        item.setString(NBT_TYPE, type.getConfigName());
        item.setString(NBT_ID, id);
        item.setString(NBT_RECEIVER_UUID, uuid);
        item.setString(NBT_RECEIVER_IGN, playerName);

        Talisman talisman = new Talisman(
            item, 0, type.getMaxExp(), id, uuid, playerName, type
        );

        player.getInventory().addItem(item.getItem());

        // Establish the lore and NBT data
        talisman.addExp(0, player);

        String displayName = playerName;

        XTalisman instance = XTalisman.getInstance();

        if (instance.isLogPlayerUuidOnReceive()) {
            displayName += " (" + uuid + ")";
        }

        // Log it
        Bukkit.getLogger().info(
            displayName + " received talisman with id " + id);

        player.sendMessage(talisman.name(instance.getTalismanReceivedMessage()));

        return talisman;
    }

    public void doReward(Player player) {
        Reward reward = type.getRandomReward();
        XTalisman instance = XTalisman.getInstance();

        try {
            reward.reward(player);
        } catch (RewardFailedException exception) {
            String rewardName = reward.getName();

            if (instance.isSendFailureMessage()) {
                exception.sendMessage(player, rewardName);
            }

            if (instance.isLogFailuresInConsole()) {
                Bukkit
                    .getLogger()
                    .severe(
                        "Could not issue reward " +
                            rewardName +
                            " to player " +
                            player.getName() + " error: " +
                            exception.getType()
                    );
            }
        }

        PlayerInventory inventory = player.getInventory();
        // Each talisman has a unique ID, so no two talisman items are the same
        inventory.clear(inventory.first(item));
        player.playSound(player.getLocation(), instance.getRewardSound(), 3.0F, 5.0F);
        player.sendMessage(
            StringUtil.colourise(
                instance.getRewardReceivedMessage().replaceAll("\\{name}", reward.getName())
            )
        );
    }

    /** Replaces {name} with Talisman name and colourises */
    private String name(String template) {
        return StringUtil.colourise(template.replaceAll("\\{name}", getColouredName()));
    }

    /** Get coloured name of Talisman */
    public String getColouredName() {
        return type.getColor() + type.getName() + ChatColor.WHITE + " Talisman";
    }

    public TalismanType getType() {
        return type;
    }

    /**
     * Helpful little function to repeat "|". It's called colons because I messed up.
     * @param length The amount of colons.
     * @return COLONS.
     */
    private static String colons(int length) {
        return StringUtil.repeat("|", length);
    }

    public String getId() {
        return id;
    }

    public String getReceiverIgn() {
        return receiverIgn;
    }

    public String getReceiverUuid() {
        return receiverUuid;
    }

}
