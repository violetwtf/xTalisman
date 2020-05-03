/*
 * Created 5/2/2020 by Violet M.
 *
 * Copyright (c) 2020 Violet M. <vi@violet.wtf>.
 * Copyright (c) 2020 [redacted] <https://[redacted]>
 * All Rights Reserved.
 */

package wtf.violet.xtalisman.model.reward;

import wtf.violet.xtalisman.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import wtf.violet.xtalisman.util.ObjectRandomizer;

import java.util.ArrayList;
import java.util.List;

public class ItemReward extends Reward {

    private String itemName;
    private Material material;
    private int amount;
    private short durability;
    private List<String> lore;

    /**
     * Create an ItemReward by its config options.
     *
     * @param name The name of the reward.
     * @param weight The weight of probability for the {@link ObjectRandomizer}.
     * @param material The material to use ("item") in the config
     * @param amount The amount of the item to give.
     * @param durability The durability of the item. (optional)
     * @param itemName The display name to give the item. (optional)
     * @param lore The lines of lore to put on the item. (optional)
     */
    public ItemReward(
        String name,
        float weight,
        Material material,
        int amount,
        short durability,
        String itemName,
        List<String> lore
    ) {
        super(name, weight);

        this.material = material;
        this.amount = amount;
        this.durability = durability;
        this.itemName = itemName;
        this.lore = lore;
    }

    @Override
    public void reward(Player player) {
        ItemStack stack = new ItemStack(material);
        stack.setAmount(amount);
        stack.setDurability(durability);

        ItemMeta meta = stack.getItemMeta();

        if (itemName != null) {
            meta.setDisplayName(StringUtil.colourise(itemName));
        }

        List<String> lore = new ArrayList<>();

        for (String line : this.lore) {
            lore.add(StringUtil.colourise(line));
        }

        meta.setLore(lore);

        stack.setItemMeta(meta);

        player.getInventory().addItem(stack);
    }

}
