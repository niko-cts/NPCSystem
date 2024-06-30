package chatzis.nikolas.mc.npcsystem;

import chatzis.nikolas.mc.nikoapi.util.Utils;
import chatzis.nikolas.mc.npcsystem.nms.NMSHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * The NPCPlayer class represents an instance of a NPC
 *
 * @author Nikolas Chatzis
 */
public class NPCPlayer extends ServerPlayer {

	final NPC npc;

	private boolean moved = false;
	protected final CustomMob mob;

	public NPCPlayer(NPC npc, MinecraftServer minecraftserver, ServerLevel world, GameProfile gameprofile, Location location) {
		super(minecraftserver, world, gameprofile);
		this.npc = npc;
		setPos(location.getX(), location.getY(), location.getZ());
		setXRot(Mth.clamp(location.getPitch(), -90.0F, 90.0F) % 360.0F);
		setYRot(location.getYaw() % 360);
		setYHeadRot(location.getYaw() % 360);
		setInvulnerable(true);

		mob = npc.withAi ? new CustomMob(this) : null;
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
		npc.send(packet);
	}

	@Override
	public void moveTo(double x, double y, double z) {
		if (mob != null)
			mob.tellToMoveTo(x, y, z);
	}

	public void teleport(Location location) {
		Objects.requireNonNull(location.getWorld());
		this.teleportTo(NMSHelper.getServerWorld(location.getWorld()), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(),
				PlayerTeleportEvent.TeleportCause.PLUGIN);
	}

	@Override
	public void teleportTo(ServerLevel worldserver, double d0, double d1, double d2, float f, float f1, PlayerTeleportEvent.TeleportCause cause) {
		super.teleportTo(worldserver, d0, d1, d2, f, f1, cause);
		npc.send(new ClientboundTeleportEntityPacket(this));
	}

	@Override
	public void die(DamageSource damageSource) {
		remove(RemovalReason.KILLED);
	}

	@Override
	public void remove(RemovalReason entity_removalreason) {
		super.remove(entity_removalreason);
		if (mob != null)
			mob.remove(entity_removalreason);
	}

	/**
	 * Checks for knockback and if it was moved.
	 * If the NPC was moved, and it was not caused by it's pathfinder mob it will move the mob to the new location.
	 */
	@Override
	public void tick() {
		super.tick();
		doTick();
		// move mob if npc moved by itself
		if (mob != null && !this.moved && (this.xo != this.getX() || this.yo != this.getY() || this.zo != this.getZ())) {
			this.mob.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
		}
		this.moved = false;
	}


	/**
	 * Will actually send every needed spawn packet to the given player.
	 * This method gets called as soon as the given player is capable of displaying the NPC.
	 * E.g., chunk loading, joining, viewing distance
	 *
	 * @param serverPlayer ServerPlayer - the player to show the NPC
	 */
	@Override
	public void startSeenByPlayer(ServerPlayer serverPlayer) {
		Player player = serverPlayer.getBukkitEntity();
		if (npc.isPlayerAllowedToSee(player)) {
			// spawn packets
			Utils.sendPackets(player, new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, this),
					new ClientboundAddPlayerPacket(this),
					new ClientboundRotateHeadPacket(this, (byte) ((this.getYRot() * 256f) / 360f)),
					new ClientboundPlayerInfoRemovePacket(List.of(uuid)));
		}
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
		if (!isInvulnerable())
			doCheckFallDamage(getX() - x, getY() - y, getZ() - z, onGround);
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (isInvulnerable()) return false;
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
}
