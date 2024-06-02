package chatzis.nikolas.mc.npcsystem.listener;

import chatzis.nikolas.mc.npcsystem.NPC;
import chatzis.nikolas.mc.npcsystem.NPCSystem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * This class is a listener class for the PlayerQuitEvent.
 * @author Niko
 * @since 0.0.1
 */
public class PlayerQuitListener implements Listener {

    /**
     * Removes the visibility of the npc to the player.
     * @param event APIPlayerJoinEvent - The event that was triggered
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        NPC.getAllNPC().forEach(npc -> npc.hide(event.getPlayer()));
    }

}
