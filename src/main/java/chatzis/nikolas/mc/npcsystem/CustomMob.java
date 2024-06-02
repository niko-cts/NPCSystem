package chatzis.nikolas.mc.npcsystem;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Wolf;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

public class CustomMob extends Wolf {

    private final NPCPlayer npcPlayer;

    public CustomMob(NPCPlayer npcPlayer) {
        super(EntityType.WOLF, npcPlayer.serverLevel());
        this.npcPlayer = npcPlayer;
        setInvulnerable(true);
        noCulling = true;

        this.goalSelector.getAvailableGoals().clear();
        if (!npcPlayer.npc.shouldLookAtPlayer())
            this.goalSelector.addGoal(1, new RandomLookAroundGoal(this));

//        setOwnerUUID(new ArrayList<>(Bukkit.getOnlinePlayers()).getFirst().getUniqueId());
//        this.goalSelector.addGoal(0, new FollowOwnerGoal(this, 1, 5, 10, false));

        setPos(npcPlayer.getX(), npcPlayer.getY(), npcPlayer.getZ());
        npcPlayer.serverLevel().getWorld().addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);

        ((LivingEntity) getBukkitEntity()).setCollidable(false);

        new BukkitRunnable() {
            @Override
            public void run() {
                setInvisible(true);
            }
        }.runTaskLater(NPCSystem.getInstance(), 10);
    }

    public void tellToMoveTo(double x, double y, double z) {
        this.navigation.moveTo(x, y, z, 1D);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.xo != this.getX() || this.yo != this.getY() || this.zo != this.getZ()) {
            npcPlayer.absMoveTo(this.getX(), this.getY(), this.getZ(), this.yRotO, this.xRotO);
        }
    }

    @Override
    public boolean teleportTo(ServerLevel worldserver, double d0, double d1, double d2, Set<RelativeMovement> set, float f, float f1) {
        var teleport = super.teleportTo(worldserver, d0, d1, d2, set, f, f1);
        npcPlayer.teleportTo(worldserver, d0, d1, d2, set, f, f1);
        return teleport;
    }
}
