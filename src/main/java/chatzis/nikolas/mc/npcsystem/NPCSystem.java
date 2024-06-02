package chatzis.nikolas.mc.npcsystem;


import chatzis.nikolas.mc.nikoapi.util.RegisterBuilderUtil;
import chatzis.nikolas.mc.npcsystem.commands.NPCDebugCommand;
import chatzis.nikolas.mc.npcsystem.event.NPCClickEvent;
import chatzis.nikolas.mc.npcsystem.listener.NPCPlayerListener;
import chatzis.nikolas.mc.npcsystem.listener.PlayerQuitListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Main class of the NPCSystem plugin.
 * Loads databases on startup and registers events.
 * With this class you can add NPCClickListener.
 *
 * @author Niko
 * @since 0.0.1
 */
public class NPCSystem extends JavaPlugin {

    private static NPCSystem instance;
    private List<NPCClickEvent> clickListenerList;

    /**
     * Registers the event.
     * Saves the default config.
     *
     * @since 0.0.1
     */
    @Override
    public void onEnable() {
        instance = this;
        this.clickListenerList = new ArrayList<>();

        RegisterBuilderUtil registerUtil = new RegisterBuilderUtil(this);
        NPCPlayerListener playerListener = new NPCPlayerListener();
        registerUtil.addListeners(playerListener, new PlayerQuitListener());
        registerUtil.addPacketListener(playerListener);
        registerUtil.register();
        Objects.requireNonNull(getCommand("npcdebug")).setExecutor(new NPCDebugCommand());
    }

    /**
     * Despawns the NPC.
     *
     * @since 0.0.1
     */
    @Override
    public void onDisable() {
        NPC.destroyAllNPC();
    }

    /**
     * Gets the instance of {@link NPCSystem}
     *
     * @return NPCSystem - instance of this plugin
     * @since 0.0.1
     */
    public static NPCSystem getInstance() {
        return instance;
    }

    /**
     * Registers a NPCClickListener.
     * This Listener will be called, when a NPC has been interacted with.
     *
     * @param listener NPCClickListener - {@link NPCClickEvent} instance
     * @since 0.0.1
     */
    public void registerNPCListener(NPCClickEvent listener) {
        this.clickListenerList.add(listener);
    }

    /**
     * Gets the copy of the NPCClickListener list
     *
     * @return List<NPCClickListener> - List of all registered listeners
     * @since 0.0.1
     */
    public List<NPCClickEvent> getClickListenerList() {
        return new ArrayList<>(clickListenerList);
    }
}
