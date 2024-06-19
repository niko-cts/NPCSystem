package chatzis.nikolas.mc.npcsystem.nms;

import chatzis.nikolas.mc.npcsystem.NPCSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;

public class NMSHelper {

    public static ServerLevel getServerWorld(World world) {
        return ((CraftWorld) world).getHandle();
    }

    public static MinecraftServer getMCServer() {
        return ((CraftServer) NPCSystem.getInstance().getServer()).getServer();
    }

    public static ItemStack getItemInNMS(org.bukkit.inventory.ItemStack item) {
        return CraftItemStack.asNMSCopy(item);
    }
}
