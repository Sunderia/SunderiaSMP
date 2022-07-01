package fr.sunderia.sunderiasmp.commands;

import fr.sunderia.sunderiautils.SunderiaUtils;
import fr.sunderia.sunderiautils.commands.CommandInfo;
import fr.sunderia.sunderiautils.commands.PluginCommand;
import fr.sunderia.sunderiautils.utils.StructureUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.structure.Structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

@CommandInfo(name = "spawnstructure", aliases = "ss")
public class SpawnStructureCommand extends PluginCommand {


    /**
     * This constructor is used to register the command and check if the command has the correct annotation.
     *
     * @param plugin An instance of the plugin.
     */
    public SpawnStructureCommand(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onCommand(Player player, String[] args) {
        new ArrayList<>(6);
        player.sendMessage("Spawning structure");
        try {
            Structure structure = Bukkit.getStructureManager().loadStructure(Objects.requireNonNull(this.getClass().getResourceAsStream("/water_trap.nbt")));
            Location loc = player.getLocation();
            Location add = loc.subtract(StructureUtils.getCenter(structure));
            structure.place(add, false, StructureRotation.NONE, Mirror.NONE, 0, 1, SunderiaUtils.getRandom());
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage("Error while loading structure");
            return;
        }
        player.sendMessage("Structure loaded");
    }
}
