package chatzis.nikolas.mc.npcsystem;

import chatzis.nikolas.mc.nikoapi.NikoAPI;
import chatzis.nikolas.mc.nikoapi.hologram.APIHologram;
import chatzis.nikolas.mc.nikoapi.player.APIPlayer;
import chatzis.nikolas.mc.nikoapi.util.ReflectionHelper;
import chatzis.nikolas.mc.nikoapi.util.Utils;
import chatzis.nikolas.mc.npcsystem.event.NPCClickEvent;
import chatzis.nikolas.mc.npcsystem.nms.NMSHelper;
import chatzis.nikolas.mc.npcsystem.utils.NPCConnection;
import chatzis.nikolas.mc.npcsystem.utils.NPCPacketListener;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The Main NPC class.
 * Instantiate a new NPC in this class and spawn it to the players you want.
 * if the NPC name is larger than 16, there will occur a Hologram.
 *
 * @author Niko
 * @since 0.0.1
 */
public class NPC {

    public static final NPCSkin DEFAULT_SKIN = new NPCSkin(
            "ewogICJ0aW1lc3RhbXAiIDogMTYxMjM1NDc5NzYyMCwKICAicHJvZmlsZUlkIiA6ICIyYmRmODYwOGFjNmM0NDdiYTg1MzBiMTBjODQ5ZWUyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJldENyYWZ0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M5Y2VhYmZmZTljZWE0ZjQzOTEzYzJjYzgwM2UwZmY2ODQ3ODFiZTQ2ZTlkNDRhZjI2NzE2MzIxZDRhNDM3NDgiCiAgICB9CiAgfQp9",
            "NpppNFiMehdYplRmNqOlGo2daKwsPdxi2KHXhimQQ4T/F2jqBRvFbN2qseL9svEjbYhumKEd+nIzYCxJSBg1/OGRqD0iY8L9rNlI+EvLB9JHmDfnq8IY2QOEDciOAAN5iw5zfzrOCtnfEBU82NkvUyH7BZsZtN7XshSK3lS3r4bvOdUdjM5Z2qw/cjjSnDQJ1g1sWOIjZjBGTrwIfzvHbkvd65K78uj6lIIHW4Y3nlCA1nqmwT2SdtSviQyYzEpYdFSHOFiuEahM2C3BGea3QtqsgYBn7h0G4inOM2XZ7sLtQMZXPoYNGm77N08dz2nXp/5eJUiXLVTgwVvMX1WY2ssi97jv4HNZpOeFByhkvrG5XUtHWCtr1Thb7EwdLdQLeodxpTrgi0qyMgkqVYdJLPUbwlUuvx8VljCZPNUSDZSV6IO5S8Y0ESSb2eS9PKnqzV1DhHlIgp79mmlgLuwGHPpMR+R6OFvcKXyCnOeW4qVnktFKy5qPFEClBze81UjgWnusLDqoUgAHc6Ko4FWMD1WPsUi0bnKii8t5ADzdsmh5L0mEDl829MRDrhMAz0w/OwsyUI5QJkxodg9Nvy8t7FDcJShvUrZITJy9iWukdlXYUVvJBi/y9u5KJZxzllICybWBQshaWo9jXzruWMh6rTA7pkrPTmO183TkPAtM1IY=",
            Optional.empty()
    );


    private static final MinecraftServer SERVER = NMSHelper.getMCServer();
    private static final Map<Integer, Set<NPC>> CHUNK_NPC = new HashMap<>();
    private static final Map<Integer, NPC> NPC_IDS = new HashMap<>();


    /**
     * Adds an NPC to the list
     * Registers NPC in chunk
     *
     * @param npc NPC - NPC to add
     */
    public static void addNPC(NPC npc) {
        CHUNK_NPC.compute(Utils.getChunkId(npc.getLocation()), (integer, npcs) -> {
            if (npcs == null)
                npcs = new HashSet<>();
            npcs.add(npc);
            return npcs;
        });
        for (int id : npc.getNPCIds()) {
            NPC_IDS.put(id, npc);
        }
    }



    /**
     * Remove a NPC from the list
     * Unregister it out of the chunk
     *
     * @param npc NPC - NPC to remove
     */
    public static void removeNPC(NPC npc) {
        CHUNK_NPC.computeIfPresent(Utils.getChunkId(npc.getLocation()), (integer, npcs) -> {
            npcs.remove(npc);
            return npcs.isEmpty() ? null : npcs;
        });
        for (int id : npc.getNPCIds()) {
            NPC_IDS.remove(id);
        }
    }

    /**
     * Destroy all NPC
     * Will be called at the server stop
     */
    public static void destroyAllNPC() {
        getAllNPC().forEach(NPC::destroy);
    }

    /**
     * Copy of all NPC, that are currently on
     *
     * @return Set<NPC> - Copied NPC HashSet
     */
    public static Collection<NPC> getAllNPC() {
        return new ArrayList<>(NPC_IDS.values());
    }

    public static Optional<NPC> getById(int id) {
        return Optional.ofNullable(NPC_IDS.get(id));
    }




    private final NPCPlayer npcPlayer;
    private final List<NPCClickEvent> clickEvents;
    protected final Set<UUID> visibleTo;
    private boolean lookAtPlayer;
    private int distanceToLookAt;

    // npc name
    APIHologram npcHologramName;

    /**
     * NPC will be spawned and shown to all players
     *
     * @param name      String - Name of NPC - A ';' splits the lines above the npc
     * @param location  Location - Spawn location of NPC
     * @param skin      {@link NPCSkin} - Skin of NPC (username = texture)
     * @since 0.0.1
     */
    public NPC(String name, Location location, NPCSkin skin) {
        this(name, location, skin, new HashSet<>());
    }

    /**
     * NPC will be spawned and shown to all players
     *
     * @param name      String - Name of NPC - A ';' splits the lines above the npc
     * @param location Location - Spawn location of NPC
     * @since 0.0.1
     */
    public NPC(String name, Location location) {
        this(name, location, null, new HashSet<>());
    }


    /**
     * NPC will be spawned and shown to all players
     *
     * @param wholeName String - Name of NPC - A ';' splits the line
     * @param location  Location - Spawn location of NPC
     * @param skin      {@link NPCSkin} - Skin of NPC
     * @param visibleTo Set<UUID> - List of every player who can see the npc (empty = all players)
     * @since 0.0.1
     */
    public NPC(String wholeName, Location location, NPCSkin skin, Set<UUID> visibleTo) {
        Objects.requireNonNull(location, "Location of NPC is null");

        this.clickEvents = new ArrayList<>();
        this.visibleTo = visibleTo;
        this.lookAtPlayer = true;
        setDistanceToLookAt(10);

        String name = ".";
        if (wholeName != null) {
            if (wholeName.split(";").length > 1) {
                this.npcHologramName = new APIHologram(location.clone().add(0, 1.8, 0), Arrays.asList(wholeName.split(";")));
                getAllAPIPlayersWhoSeeNPC().forEach(p -> p.showHologram(npcHologramName));
            } else {
                name = wholeName;
            }
        }
        this.npcHologramName = null;

        Objects.requireNonNull(location);
        Objects.requireNonNull(location.getWorld());

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
        if (skin != null) {
            gameProfile.getProperties().put("textures", new Property("textures", skin.value(), skin.signature()));
        }

        ServerLevel nmsWorld = NMSHelper.getServerWorld(location.getWorld());
        ClientInformation info = new ClientInformation("en_us", 0, ChatVisiblity.FULL, false, 0, HumanoidArm.RIGHT, false, false);
        this.npcPlayer = new NPCPlayer(this, SERVER, nmsWorld, gameProfile, info, location);
        this.npcPlayer.connection = new NPCPacketListener(SERVER, new NPCConnection(PacketFlow.CLIENTBOUND), npcPlayer,
                new CommonListenerCookie(gameProfile, 0, info, false));

        nmsWorld.addNewPlayer(npcPlayer);

        SynchedEntityData dataWatcher = this.npcPlayer.getEntityData();
        dataWatcher.set(net.minecraft.world.entity.player.Player.DATA_HEALTH_ID, 20F); // life

        EntityDataAccessor<Byte> skinAccessor = ReflectionHelper.get(net.minecraft.world.entity.player.Player.class, null, "bV");
        if (skinAccessor == null)
            skinAccessor = new EntityDataAccessor<>(17, EntityDataSerializers.BYTE);
        dataWatcher.set(skinAccessor, (byte) 0xFF); // skin

        NPC.addNPC(this);
    }

    /**
     * Sets the navigator of the npc
     * @param location Location - location to move to
     */
    public void moveTo(Location location) {
        npcPlayer.moveTo(location.getX(), location.getY(), location.getZ());
    }


    /**
     * Hide the NPC for a given player,
     * The NPC will respawn, if the player loads the chunk again
     *
     * @param player Player - player to hide
     * @since 0.0.1
     */
    public void hide(Player player) {
        if (npcHologramName != null) {
            APIPlayer apiPlayer = NikoAPI.getInstance().getPlayerHandler().getPlayer(player);
            if (apiPlayer != null) {
                apiPlayer.hideHologram(npcHologramName);
            }
        }

        Utils.sendPackets(player, new ClientboundRemoveEntitiesPacket(getNPCIds()));
    }

    /**
     * Destroy the NPC only for one player
     * the npc will hide and won't spawn anymore, for the player
     * destroys the NPC completely if no player is allowed to see the npc
     *
     * @param player Player - Player to destroy the npc for
     * @since 0.0.1
     */
    public void destroy(Player player) {
        if (isPlayerAllowedToSee(player)) {
            if (visibleTo.size() == 1) {
                destroy();
            } else {
                hide(player);
                visibleTo.remove(player.getUniqueId());
            }
        }
    }

    /**
     * Destroy the npc.
     * NPC won't spawn anymore.
     * Players won't see the NPC anymore.
     *
     * @since 0.0.1
     */
    public void destroy() {
        getAllPlayersWhoSeeNPC().forEach(this::hide);
        visibleTo.clear();
        clickEvents.clear();

        if (npcPlayer.isAlive())
            npcPlayer.die(null); // will call NPC.remove here

        npcPlayer.remove(Entity.RemovalReason.DISCARDED);

        NPC.removeNPC(this);
    }


    /**
     * Teleport the NPC
     *
     * @param teleportLocation Location - Location to teleport
     * @since 0.0.1
     */
    public void teleport(Location teleportLocation) {
        npcPlayer.teleport(teleportLocation);
    }

    /**
     * Turn the npc's body and head to the players location
     *
     * @param player Player - Player to look at
     * @since 0.0.1
     */
    public void lookAtPlayer(Player player) {
        lookAtLocation(player, player.getLocation());
    }

    /**
     * Let the npc look to the given location
     *
     * @param player       Player - Player that should see the effect
     * @param lookLocation Location - Location to look at
     * @since 0.0.1
     */
    public void lookAtLocation(Player player, Location lookLocation) {
        npcPlayer.mob.setXRot(lookLocation.getPitch());
        npcPlayer.mob.setYRot(lookLocation.getYaw());
        npcPlayer.setXRot(lookLocation.getPitch());
        npcPlayer.setYRot(lookLocation.getYaw());
        Utils.sendPackets(player, getLookAtPackets(lookLocation).toArray(new Packet<?>[0]));
    }

    public List<Packet<?>> getLookAtPackets(Location lookLocation) {
        Location npcLocation = getLocation();
        float angle = (float) Math.toDegrees(Math.atan2(npcLocation.getX() - lookLocation.getX(),
                (npcLocation.getZ() - lookLocation.getZ()) * -1));
        float yAngle = (float) Math.atan2((npcLocation.getY() - lookLocation.getY()), lookLocation.distance(npcLocation)) * 45.0F;
        return List.of(new ClientboundRotateHeadPacket(this.npcPlayer, (byte) ((int) (angle / 360.0F * 256.0F))),
                new ClientboundMoveEntityPacket.Rot(this.npcPlayer.getId(), (byte) ((int) (angle / 360.0F * 256.0F)), (byte) yAngle, true));
    }

    /**
     * Let the NPC equips an item.
     *
     * @param slot      EnumItemSlot - The slot the NPC should hold the item
     * @param itemStack ItemStack - the item the NPC should hold
     * @since 0.0.1
     */
    public void equip(EquipmentSlot slot, ItemStack itemStack) {
        equip(new EquipmentSlot[]{slot}, new ItemStack[]{itemStack});
    }

    /**
     * Let the NPC equips an item.
     *
     * @param slots EquipmentSlot[] - The slot the NPC should hold the item
     * @param items ItemStack[] - the item the NPC should hold
     * @since 0.0.1
     */
    public void equip(EquipmentSlot[] slots, ItemStack[] items) {
        if (slots.length != items.length)
            throw new IllegalStateException("Equipment slots length is not itemstack length");

        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> equips = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) {
            equips.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.values()[slots[i].ordinal()], CraftItemStack.asNMSCopy(items[i])));
        }
        send(new ClientboundSetEquipmentPacket(npcPlayer.getId(), equips));
    }

    /**
     * Sends packet(s) to all players, that are allowed to see the npc
     *
     * @param packet Object<?>... all packets to send
     * @since 0.0.1
     */
    protected void send(Packet<?>... packet) {
        if (packet.length != 0) {
            getAllPlayersWhoSeeNPC().forEach(p -> Utils.sendPackets(p, packet));
        }
    }

    /**
     * Checks whether lookAtPlayer is enabled and the npc is not moving
     *
     * @return npc should look at player
     * @since 0.0.1
     */
    public boolean looksAtPlayer() {
        return lookAtPlayer && npcPlayer.mob.getNavigation().isDone();
    }

    /**
     * @return Location - current Location of NPC
     * @since 0.0.1
     */
    public Location getLocation() {
        return npcPlayer.getBukkitEntity().getLocation();
    }

    /**
     * @return UUID - uuid of npc
     * @since 0.0.1
     */
    public UUID getUniqueID() {
        return npcPlayer.getUUID();
    }

    /**
     * Gets the first line of the NPC's name
     * A name can be split with the col ';'
     *
     * @return String - First line
     * @since 0.0.1
     */
    public String getName() {
        return this.npcHologramName != null ? this.npcHologramName.getLines().getFirst() : npcPlayer.getName().getString();
    }

    /**
     * Registers a click event to the npc.
     *
     * @param event {@link NPCClickEvent} - the click event.
     * @since 1.0
     */
    public void click(NPCClickEvent event) {
        this.clickEvents.add(event);
    }


    /**
     * @return npc should look at the player
     * @since 0.0.1
     */
    public boolean shouldLookAtPlayer() {
        return lookAtPlayer;
    }

    /**
     * @return int - distance, the npc starts looking at a player
     * @since 0.0.1
     */
    public int getDistanceToLookAt() {
        return distanceToLookAt;
    }

    /**
     * Set the distance, the npc starts looking at a player. <br>
     * If its null or 0, npc won't look at player.
     *
     * @param distanceToLookAt int - the distance, the npc starts looking at a player
     * @since 0.0.1
     */
    public void setDistanceToLookAt(Integer distanceToLookAt) {
        this.lookAtPlayer = distanceToLookAt != null && distanceToLookAt > 0;
        this.distanceToLookAt = this.lookAtPlayer ? distanceToLookAt : 0;
    }

    /**
     * Get the EntityPlayer npc
     *
     * @return EntityPlayer - NPC
     * @since 0.0.1
     */
    public ServerPlayer getNPC() {
        return npcPlayer;
    }

    /**
     * Returns the id of the npc and every id corresponding to the npc (for pathfinding)
     *
     * @return int[] - all entity id's.
     */
    protected int[] getNPCIds() {
        return new int[]{this.npcPlayer.getId(), this.npcPlayer.mob.getId()};
    }

    /**
     * Get the spigot player instance.
     *
     * @return Player - spigot npc player.
     */
    public Player getSpigotNPC() {
        return this.npcPlayer.getBukkitEntity();
    }

    public List<NPCClickEvent> getClickEvents() {
        return new ArrayList<>(clickEvents);
    }

    public List<APIPlayer> getAllAPIPlayersWhoSeeNPC() {
        return visibleTo.isEmpty() ? new ArrayList<>(NikoAPI.getInstance().getPlayerHandler().getOnlinePlayers()) :
                NikoAPI.getInstance().getPlayerHandler().getOnlinePlayers()
                        .stream().filter(p -> visibleTo.contains(p.getUniqueId())).collect(Collectors.toList());
    }

    public List<Player> getAllPlayersWhoSeeNPC() {
        return visibleTo.isEmpty() ? new ArrayList<>(Bukkit.getOnlinePlayers()) :
                Bukkit.getOnlinePlayers().stream().filter(p -> visibleTo.contains(p.getUniqueId())).collect(Collectors.toList());
    }

    public boolean isPlayerAllowedToSee(Player player) {
        return isPlayerAllowedToSee(player.getUniqueId());
    }

    public boolean isPlayerAllowedToSee(UUID uuid) {
        return visibleTo.isEmpty() || visibleTo.contains(uuid);
    }
}
