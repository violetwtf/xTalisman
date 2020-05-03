/*
 * Created 5/2/2020 by Violet M.
 *
 * Copyright (c) 2020 Violet M. <vi@violet.wtf>.
 * Copyright (c) 2020 [redacted] <https://[redacted]>
 * All Rights Reserved.
 */

package wtf.violet.xtalisman.model;

import wtf.violet.xtalisman.model.reward.Reward;
import wtf.violet.xtalisman.util.ObjectRandomizer;
import org.bukkit.ChatColor;

import java.util.List;

public class TalismanType {

    private String name;
    private String configName;
    private ChatColor color;
    private int expRequired;
    private ObjectRandomizer<Reward> randomizer;

    public TalismanType(
        String name, ChatColor color, int expRequired, String configName, List<Reward> rewards
    ) {
        this.name = name;
        this.color = color;
        this.expRequired = expRequired;
        this.configName = configName;
        randomizer = new ObjectRandomizer(rewards);
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public int getMaxExp() {
        return expRequired;
    }

    public String getConfigName() {
        return configName;
    }

    public Reward getRandomReward() {
        return randomizer.getRandom();
    }

}
