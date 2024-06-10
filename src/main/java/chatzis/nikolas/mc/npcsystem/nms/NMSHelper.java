package chatzis.nikolas.mc.npcsystem.nms;

import chatzis.nikolas.mc.npcsystem.NPCSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;

public class NMSHelper {

    public static ServerLevel getServerWorld(World world) {
        return ((CraftWorld) world).getHandle();
    }

    public static MinecraftServer getMCServer() {
        return ((CraftServer) NPCSystem.getInstance().getServer()).getServer();
    }
}
