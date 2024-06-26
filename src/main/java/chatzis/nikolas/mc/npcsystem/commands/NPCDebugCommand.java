package chatzis.nikolas.mc.npcsystem.commands;

import chatzis.nikolas.mc.npcsystem.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NPCDebugCommand implements CommandExecutor {

    NPC npc = null;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player player) {
            if (args.length == 1){
                if (npc != null)
                    npc.destroy();
                npc = new NPC(UUID.randomUUID(), ChatColor.translateAlternateColorCodes('&', args[0]), player.getLocation());
            }
            else if (npc != null)
                npc.teleport(player.getLocation());
        }
        return true;
    }
}
