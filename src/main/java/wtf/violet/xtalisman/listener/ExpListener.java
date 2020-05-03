/*
 * Created 5/2/2020 by Violet M.
 *
 * Copyright (c) 2020 Violet M. <vi@violet.wtf>.
 * Copyright (c) 2020 [redacted] <https://[redacted]>
 * All Rights Reserved.
 */

package wtf.violet.xtalisman.listener;

import wtf.violet.xtalisman.model.Talisman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class ExpListener implements Listener {

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();

        // Go through hot-bar
        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);

            if (item == null) {
                continue;
            }

            Talisman talisman = Talisman.of(inventory.getItem(i));

            if (talisman != null && talisman.canAddExp()) {
                talisman.addExp(event.getAmount(), player);
                event.setAmount(0);
                break;
            }
        }
    }

}
