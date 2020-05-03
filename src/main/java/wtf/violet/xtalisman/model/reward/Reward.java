/*
 * Created 5/2/2020 by Violet M.
 *
 * Copyright (c) 2020 Violet M. <vi@violet.wtf>.
 * Copyright (c) 2020 [redacted] <https://[redacted]>
 * All Rights Reserved.
 */

package wtf.violet.xtalisman.model.reward;

import wtf.violet.xtalisman.exception.RewardFailType;
import wtf.violet.xtalisman.exception.RewardFailedException;
import wtf.violet.xtalisman.util.ObjectRandomizer;
import org.bukkit.entity.Player;

public class Reward implements ObjectRandomizer.Probable {

    private String name;
    private float weight;

    public Reward(String name, float weight) {
        this.name = name;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public float getWeight() {
        return weight;
    }

    /** Reward player with the reward, should be modified in other classes */
    public void reward(Player player) throws RewardFailedException {
        throw new RewardFailedException(RewardFailType.TYPE_NOT_IMPLEMENTED);
    }

}
