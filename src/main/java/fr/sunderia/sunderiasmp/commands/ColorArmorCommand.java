package fr.sunderia.sunderiasmp.commands;

import fr.sunderia.sunderiautils.commands.CommandInfo;
import fr.sunderia.sunderiautils.commands.PluginCommand;
import fr.sunderia.sunderiautils.utils.ItemStackUtils;
import fr.sunderia.sunderiautils.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@CommandInfo(name = "colorarmor", aliases = "ca", usage = "/<command> <red> <green> <blue>")
public class ColorArmorCommand extends PluginCommand {

    public ColorArmorCommand(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onCommand(Player player, String[] args) {
        if(args.length != 3) {
            player.sendMessage(getUsage());
            return;
        }

        int[] color;

        try {
            int red = Integer.parseInt(args[0]);
            int green = Integer.parseInt(args[1]);
            int blue = Integer.parseInt(args[2]);
            color = new int[]{red, green, blue};
        } catch(NumberFormatException e) {
            player.sendMessage(getUsage());
            return;
        }
        if(!player.getInventory().getItemInMainHand().getType().name().contains("LEATHER") || !ItemStackUtils.isAnArmor(player.getInventory().getItemInMainHand())) {
            player.sendMessage(ChatColor.RED + "You must be holding a leather armor to use this command.");
            return;
        }
        player.getInventory().setItemInMainHand(new ItemBuilder(player.getInventory().getItemInMainHand()).setDisplayName(player.getInventory().getItemInMainHand().getType().name()).setHideIdentifier(true).setColor(Color.fromRGB(color[0], color[1], color[2])).build());
    }
}
