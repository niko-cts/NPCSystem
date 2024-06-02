package chatzis.nikolas.mc.npcsystem;

import chatzis.nikolas.mc.nikoapi.util.ReflectionHelper;
import chatzis.nikolas.mc.nikoapi.util.Utils;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.TrackingRange;

import java.io.IOException;
import java.util.*;

public class NPCPlayer extends ServerPlayer {

    final NPC npc;
    protected final CustomMob mob;

    private boolean moved = false;

    public NPCPlayer(NPC npc, MinecraftServer minecraftserver, ServerLevel world, GameProfile gameprofile, ClientInformation clientinformation, Location location) {
        super(minecraftserver, world, gameprofile, clientinformation);
        this.npc = npc;
        setPos(location.getX(), location.getY(), location.getZ());
        setXRot(location.getPitch());
        setYRot(location.getYaw());

        teleportTo(world, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), PlayerTeleportEvent.TeleportCause.PLUGIN);

        mob = new CustomMob(this);
    }


    @Override
    public void absMoveTo(double d0, double d1, double d2, float f, float f1) {
        this.moved = true;
        super.absMoveTo(d0, d1, d2, f, f1);

        ClientboundMoveEntityPacket packet = new ClientboundMoveEntityPacket.PosRot(
                getId(),
                (short) (d0 - this.xo),
                (short) (d1 - this.yo),
                (short) (d2 - this.zo),
                (byte) ((int) (f * 256F / 360.0F)),
                (byte) ((int) (f1 * 256F / 360.0F)), true);
        npc.getAllPlayersWhoSeeNPC().forEach(player -> Utils.sendPackets(player, packet));
    }

    @Override
    public void moveTo(double x, double y, double z) {
        mob.tellToMoveTo(x, y, z);
    }

    public void teleport(Location location) {
        Objects.requireNonNull(location.getWorld());
        this.teleportTo(((CraftWorld) location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public boolean teleportTo(ServerLevel worldserver, double d0, double d1, double d2, Set<RelativeMovement> set, float yaw, float pitch) {
        npc.send(new ClientboundTeleportEntityPacket(this),
                new ClientboundRotateHeadPacket(this, (byte) ((int) (yaw * 256F / 360.0F))),
                new ClientboundMoveEntityPacket.Rot(this.getId(), (byte) ((int) (yaw * 256F / 360.0F)), (byte) ((int) (pitch * 256F / 360.0F)), true));
        return super.teleportTo(worldserver, d0, d1, d2, set, yaw, pitch);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        npc.destroy();
    }

    @Override
    public void tick() {
        super.tick();
        doTick();
        // move mob if npc moved by itself
        if (!this.moved && (this.xo != this.getX() || this.yo != this.getY() || this.zo != this.getZ())) {
            this.mob.moveTo(this.getX(), this.getY(), this.getZ(), this.yRotO, this.xRotO);
        }
        this.moved = false;
    }

    @Override
    public void doTick() {
        super.baseTick();
        moveWithFallDamage();
    }


    private void moveWithFallDamage() {
        double x = getX();
        double y = getY();
        double z = getZ();
        travel(Vec3.ZERO);
        doCheckFallDamage(getX() - x, getY() - y, getZ() - z, onGround);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.checkBlocking(damageSource)) {
            try (var level = this.level()) {
                this.playSound(SoundEvents.SHIELD_BLOCK, 0.8F, 0.8F + level.random.nextFloat() * 0.4F);
            } catch (IOException ignored) {
            }
            return false;
        }

        boolean damaged = super.hurt(damageSource, f);
        if (damaged && this.hurtMarked) {
            this.hurtMarked = false;
            new BukkitRunnable() {
                @Override
                public void run() {
                    NPCPlayer.this.hurtMarked = true;
                }
            }.runTask(NPCSystem.getInstance());
        }
        return damaged;
    }

    private boolean checkBlocking(DamageSource damagesource) {
        Entity entity = damagesource.getDirectEntity();
        boolean flag = false;
        if (entity instanceof Arrow entityarrow) {
            if (entityarrow.getPierceLevel() > 0) {
                flag = true;
            }
        }
        if (!damagesource.is(DamageTypeTags.BYPASSES_ARMOR) && this.isBlocking() && !flag) {
            Vec3 vec3d = damagesource.getSourcePosition();
            if (vec3d != null) {
                Vec3 vec3d1 = this.getViewVector(1.0F);
                Vec3 vec3d2 = vec3d.vectorTo(this.getDeltaMovement()).normalize();
                vec3d2 = new Vec3(vec3d2.x, 0.0D, vec3d2.z);
                return vec3d2.dot(vec3d1) < 0.0D;
            }
        }
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer entityplayer) {
        CraftPlayer player = entityplayer.getBukkitEntity();
        if (npc.isPlayerAllowedToSee(player)) {
            // spawn packets
            Utils.sendPackets(player, new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, this),
                    new ClientboundAddEntityPacket(this),
                    new ClientboundRotateHeadPacket(this, (byte) ((this.yRotO * 256f) / 360f)),
                    new ClientboundPlayerInfoRemovePacket(List.of(uuid)));
        }
    }
}
