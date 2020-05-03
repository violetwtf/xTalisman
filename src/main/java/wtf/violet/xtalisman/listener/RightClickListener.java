/*
 * Created 5/2/2020 by Violet M.
 *
 * Copyright (c) 2020 Violet M. <vi@violet.wtf>.
 * Copyright (c) 2020 [redacted] <https://[redacted]>
 * All Rights Reserved.
 */

package wtf.violet.xtalisman.listener;

import wtf.violet.xtalisman.model.Talisman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class RightClickListener implements Listener {

    // This must be HIGH, has Minecraft (or somewhere down the server chain) cancels the event when
    // the player right clicks the air.
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item != null) {
            Talisman talisman = Talisman.of(event.getItem());

            // If it can't add exp, it's full
            if (talisman != null && !talisman.canAddExp()) {
                event.setCancelled(true);
                talisman.doReward(event.getPlayer());
            }
        }
    }

}
