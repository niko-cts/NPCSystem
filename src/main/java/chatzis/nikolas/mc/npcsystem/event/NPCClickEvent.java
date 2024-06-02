package chatzis.nikolas.mc.npcsystem.event;

/**
 * This class is a listener class.
 * It will be called, when a npc has been interacted.
 * To register this class, add it via the {@link net.fununity.npc.NPCSystem}#registerNPCListener(NPCClickListener);
 *
 * @author Niko
 * @since 0.0.1
 */
public interface NPCClickEvent {

    /**
     * Determine the Priority of the NPC Event.
     * Is used in the {@link NPCClickEvent} to define the priority of an listener.
     * @author Niko
     * @since 0.0.1
     */
    final class Priority {
        public static final int LOW = 0;
        public static final int NORMAL = 1;
        public static final int HIGH = 2;

        private Priority() {
            throw new IllegalStateException("Only use static variables to determine the Priority.");
        }
    }

    /**
     * Change the priority of the Listener
     * Higher priorities are getting called later, but can decide about the cancellation
     * @return int - Priority of the event
     * @see Priority
     * @since 0.0.1
     */
    default int getPriority() {
        return Priority.NORMAL;
    }

    /**
     * This method will be called, when an NPC Interacted with
     * @param event PlayerInteractAtEntityEvent - {@link PlayerInteractAtNPCEvent} instance that has been called
     * @since 0.0.1
     */
    void npcClicked(PlayerInteractAtNPCEvent event);

}
