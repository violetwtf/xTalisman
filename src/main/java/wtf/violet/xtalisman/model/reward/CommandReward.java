/*
 * Created 5/2/2020 by Violet M.
 *
 * Copyright (c) 2020 Violet M. <vi@violet.wtf>.
 * Copyright (c) 2020 [redacted] <https://[redacted]>
 * All Rights Reserved.
 */

package wtf.violet.xtalisman.model.reward;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandReward extends Reward {

    private String command;

    public CommandReward(String name, float weight, String command) {
        super(name, weight);
        this.command = command;
    }

    @Override
    public void reward(Player player) {
        player
            .getServer()
            .dispatchCommand(
                Bukkit.getConsoleSender(),
                command.replaceAll("\\{name}", player.getName())
            );
    }
}
