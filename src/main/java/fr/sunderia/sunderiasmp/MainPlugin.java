package fr.sunderia.sunderiasmp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.sunderia.sunderiasmp.commands.CustomItemsCommand;
import fr.sunderia.sunderiasmp.listener.DryerListener;
import fr.sunderia.sunderiasmp.objects.CustomItems;
import fr.sunderia.sunderiasmp.recipes.DryerRecipe;
import fr.sunderia.sunderiautils.SunderiaUtils;
import fr.sunderia.sunderiautils.customblock.CustomBlock;
import fr.sunderia.sunderiautils.listeners.CustomBlockListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class MainPlugin extends JavaPlugin implements Listener {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        SunderiaUtils.of(this);
        try {
            SunderiaUtils.registerCommands(CustomItemsCommand.class.getPackage().getName());
            SunderiaUtils.registerListeners(DryerListener.class.getPackage().getName());
            Bukkit.getPluginManager().registerEvents(this, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Arrays.stream(CustomItems.values()).filter(CustomItems::isCustomBlock).map(CustomItems::getKey).forEach(key -> getLogger().info("Registered: " + key.getKey()));
        World overworld = Bukkit.getWorlds().get(0);
        overworld.getPopulators().add(new BlockPopulator() {
            @Override
            public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int x, int z, @NotNull LimitedRegion limitedRegion) {
                if(random.nextInt(50) == 1) {
                    //limitedRegion.getBlockState(x, worldInfo.getUID().getHighestBlockYAt(x, z), z).setType(Material.EMERALD_ORE);
                    int y = random.nextInt(20) + 1;
                    Bukkit.getScheduler().runTask(SunderiaUtils.getPlugin(), () -> {
                        CustomBlock customBlock = CustomItems.RUBY_ORE.getCustomBlock();
                        customBlock.setLoc(new Location(overworld, x, y, z));
                        customBlock.setBlock(customBlock.getLoc());
                        CustomBlockListener.putCustomBlock(customBlock);
                        System.out.println("Added custom block at " + x + " " + y + " " + z);
                    });
                }
            }
        });
        new DryerRecipe(SunderiaUtils.key("rotten_flesh"), new ItemStack(Material.ROTTEN_FLESH), new ItemStack(Material.LEATHER), 10).addRecipe();
        try {
            loadDryers();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to load dryers from config\n" + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void loadDryers() throws IOException {
        try(BufferedReader reader = Files.newBufferedReader(getDataFolder().toPath().resolve("dryer.json"))) {
            JsonObject object = gson.fromJson(reader, JsonObject.class);
            object.entrySet().forEach(entry -> {
                String[] pos = entry.getKey().split("\\|");
                int x = Integer.parseInt(pos[0]);
                int y = Integer.parseInt(pos[1]);
                int z = Integer.parseInt(pos[2]);
                World world = Bukkit.getWorld(UUID.fromString(pos[3]));
                if(world == null) {
                    Bukkit.getLogger().severe("Failed to load dryer at " + x + " " + y + " " + z + " because world " + pos[3] + " does not exist");
                    return;
                }
                Location location = new Location(world, x, y, z);
                byte[] bytes = Base64.getDecoder().decode(gson.fromJson(entry.getValue(), String.class));
                try {
                    CompoundTag tag = TagParser.parseTag(new String(bytes, StandardCharsets.UTF_8));
                    ArmorStand entity = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
                    ((CraftArmorStand) entity).getHandle().load(tag);
                    entity.setVisible(true);
                    DryerListener.astMap.put(world.getBlockAt(location), entity);
                    Bukkit.getLogger().info("Loaded dryer at " + entity.getLocation().getBlockX() + " " + entity.getLocation().getBlockY() + " " + entity.getLocation().getBlockZ());
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    public void onDisable() {
        JsonObject root = new JsonObject();
        JsonObject object = new JsonObject();
        DryerListener.astMap.forEach((block, armorStand) -> {
            CompoundTag tag = new CompoundTag();
            ((CraftArmorStand)armorStand).getHandle().save(tag);
            Bukkit.getLogger().info(() -> "Saving armor stand " + armorStand.getUniqueId() + " with tag " + tag);
            object.addProperty(block.getX() + "|" + block.getY() + "|" + block.getZ() + "|" + block.getWorld().getUID(),
                    Base64.getEncoder().encodeToString(tag.getAsString().getBytes(StandardCharsets.UTF_8)));
            armorStand.remove();
        });
        root.add("dryers", object);
        try {
            Files.createDirectories(getDataFolder().toPath());
            Files.writeString(getDataFolder().toPath().resolve("dryer.json"), gson.toJson(object), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
