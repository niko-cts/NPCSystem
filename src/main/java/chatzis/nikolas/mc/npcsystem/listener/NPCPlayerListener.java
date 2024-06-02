package chatzis.nikolas.mc.npcsystem.listener;

import chatzis.nikolas.mc.nikoapi.NikoAPI;
import chatzis.nikolas.mc.nikoapi.packet.reader.*;
import chatzis.nikolas.mc.nikoapi.player.PlayerHandler;
import chatzis.nikolas.mc.npcsystem.NPC;
import chatzis.nikolas.mc.npcsystem.NPCSystem;
import chatzis.nikolas.mc.npcsystem.event.NPCClickEvent;
import chatzis.nikolas.mc.npcsystem.event.PlayerInteractAtNPCEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;
import java.util.logging.Level;

/**
 * Manages the Chunk-loading and unloading.
 * Manages the move event from players.
 * Manages the interaction from players with npc.
 *
 * @author Niko
 * @since 0.0.1
 */
public class NPCPlayerListener implements Listener, PacketEntityUseListener {

    private final PlayerHandler playerHandler;

    /**
     * Gets the instance of {@link PlayerHandler}
     * Registers the ChunkLoading and Unloading in {@link chatzis.nikolas.mc.nikoapi.packet.reader.APIListenerManager}
     *
     * @since 0.0.1
     */
    public NPCPlayerListener() {
        this.playerHandler = NikoAPI.getInstance().getPlayerHandler();
    }

    /**
     * Is called, when a Player interacts with an entity.
     * Will check if the interacted entity is a NPC
     *
     * @param packet PacketPlayInUseEntity - Packet that got called
     * @param player Player - player that interacted
     * @since 0.0.1
     */
    @Override
    public void useEntity(Player player, EntityUsePacket packet) {
        NPC.getById(packet.entityId()).ifPresent(npc -> {
            List<NPCClickEvent> clickEvents = npc.getClickEvents();
            clickEvents.addAll(NPCSystem.getInstance().getClickListenerList());
            clickEvents.sort(Comparator.comparing(NPCClickEvent::getPriority));

            PlayerInteractAtNPCEvent event = new PlayerInteractAtNPCEvent(npc, playerHandler.getPlayer(player), packet.clickType());
            for (NPCClickEvent clickEvent : clickEvents) {
                try {
                    clickEvent.npcClicked(event);
                } catch (Exception exception) {
                    NPCSystem.getInstance().getLogger().log(Level.WARNING, "Caught {0} while npc was clicked: {1}",
                            new String[]{exception.getClass().getSimpleName(), exception.getMessage()});
                }
            }
        });
    }

    @Override
    public List<APIListenerManager.PacketTypes> getListenerType() {
        return List.of(APIListenerManager.PacketTypes.INTERACT);
    }

    /**
     * Will be executed when a player moves
     *
     * @param event PlayerMoveEvent - Event that got called
     * @since 0.0.1
     */
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getX() == event.getTo().getX() &&
            event.getFrom().getY() == event.getTo().getY() && event.getFrom().getZ() == event.getTo().getZ()) return;

        for (NPC npc : NPC.getAllNPC()) {
            if (npc.shouldLookAtPlayer() &&
                event.getPlayer().getWorld().equals(npc.getLocation().getWorld()) &&
                npc.isPlayerAllowedToSee(event.getPlayer()) &&
                npc.getLocation().distanceSquared(event.getTo()) <= npc.getDistanceToLookAt() * npc.getDistanceToLookAt()) {
                npc.lookAtPlayer(event.getPlayer());
            }
        }
    }
}
