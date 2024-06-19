package chatzis.nikolas.mc.npcsystem.event;

import chatzis.nikolas.mc.nikoapi.player.APIPlayer;
import chatzis.nikolas.mc.npcsystem.NPC;
import org.bukkit.event.inventory.ClickType;

/**
 * This event will be called when a player interacts with a npc.
 * The event contains data about NPC, Player and Hit method.
 *
 * @author Niko
 * @since 0.0.1
 */
public class PlayerInteractAtNPCEvent {

    private final NPC npc;
    private final APIPlayer player;
    private final ClickType clickType;

    /**
     * Event is thrown, when NPC has been clicked.
     * @param npc    NPC - NPC that got interacted
     * @param player APIPlayer - Player who interacted
     * @param clickType ClickType - how the player clicked
     * @since 0.0.1
     */
    public PlayerInteractAtNPCEvent(NPC npc, APIPlayer player, ClickType clickType) {
        this.npc = npc;
        this.player = player;
        this.clickType = clickType;
    }

    /**
     * Get the player who interacted with the npc
     * @return Player - Interactor
     * @since 0.0.1
     */
    public APIPlayer getPlayer() {
        return player;
    }

    /**
     * Get the NPC that got interacted with
     * @return NPC - The interacted NPC
     * @since 0.0.1
     */
    public NPC getNPC() {
        return npc;
    }

    /**
     * Return true wether the player clicks RIGHT or SHIFT_RIGHT.
     * @return boolean - player clicks with a secondary mouse key.
     * @since 1.0.1
     */
    public boolean isRightClicking() {
        return clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT;
    }
    /**
     * The click the player made on the npc
     * @return ClickType - either LEFT, RIGHT, SHIFT LEFT, SHIFT RIGHT
     * @since 0.0.1
     */
    public ClickType getAction() {
        return clickType;
    }

}
