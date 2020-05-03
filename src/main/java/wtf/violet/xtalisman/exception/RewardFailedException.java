/*
 * Created 5/2/2020 by Violet M.
 *
 * Copyright (c) 2020 Violet M. <vi@violet.wtf>.
 * Copyright (c) 2020 [redacted] <https://[redacted]>
 * All Rights Reserved.
 */

package wtf.violet.xtalisman.exception;

import wtf.violet.xtalisman.XTalisman;
import wtf.violet.xtalisman.util.StringUtil;
import org.bukkit.entity.Player;

public final class RewardFailedException extends Exception {

    private RewardFailType type;

    public RewardFailedException(RewardFailType type) {
        this.type = type;
    }

    public void sendMessage(Player player, String rewardName) {
        player.sendMessage(
            StringUtil.colourise(
                XTalisman
                    .getInstance()
                    .getRewardFailedMessage()
                    .replaceAll("\\{code}", type.toString())
                    .replaceAll("\\{name}", rewardName)
            )
        );
    }

    public RewardFailType getType() {
        return type;
    }

}
