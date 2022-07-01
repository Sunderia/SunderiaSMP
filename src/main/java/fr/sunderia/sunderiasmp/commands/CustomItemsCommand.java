package fr.sunderia.sunderiasmp.commands;

import fr.sunderia.sunderiasmp.objects.Inventories;
import fr.sunderia.sunderiautils.commands.CommandInfo;
import fr.sunderia.sunderiautils.commands.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@CommandInfo(name = "customitems", aliases = "ci")
public class CustomItemsCommand extends PluginCommand {

    /**
     * This constructor is used to register the command and check if the command has the correct annotation.
     *
     * @param plugin An instance of the plugin.
     */
    public CustomItemsCommand(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onCommand(Player player, String[] args) {
        player.openInventory(Inventories.getCustomItems());
    }
}
