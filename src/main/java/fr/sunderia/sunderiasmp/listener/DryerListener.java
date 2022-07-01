package fr.sunderia.sunderiasmp.listener;

import fr.sunderia.sunderiasmp.recipes.DryerRecipe;
import fr.sunderia.sunderiasmp.utils.FaceUtil;
import fr.sunderia.sunderiautils.SunderiaUtils;
import fr.sunderia.sunderiautils.utils.ItemStackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ObserverBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.block.data.type.Slab;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_18_R1.block.impl.CraftObserver;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DryerListener implements Listener {

    public static final Map<Block, ArmorStand> astMap = new HashMap<>();
    private final Biome[] hotBiomes = new Biome[]{Biome.DESERT, Biome.ERODED_BADLANDS, Biome.WOODED_BADLANDS, Biome.BADLANDS};
    private static final Map<ArmorStand, BukkitTask> astTaskMap = new HashMap<>();

    @EventHandler
    public void onClickOnDryer(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND &&
                event.getClickedBlock().getType().name().endsWith("_SLAB") && event.getClickedBlock().getBlockData() instanceof Slab slab && !slab.isWaterlogged() && !event.getPlayer().isSneaking()) {
            event.setCancelled(true);
            if (astMap.containsKey(event.getClickedBlock())) {
                ArmorStand ast = astMap.get(event.getClickedBlock());
                if (event.getPlayer().getInventory().firstEmpty() == -1) {
                    event.getClickedBlock().getWorld().dropItemNaturally(event.getClickedBlock().getLocation(), ast.getEquipment().getItemInMainHand());
                } else {
                    event.getPlayer().getInventory().addItem(ast.getEquipment().getItemInMainHand());
                }
                if(astTaskMap.containsKey(ast)) {
                    astTaskMap.get(ast).cancel();
                    astTaskMap.remove(ast);
                }
                ast.getEquipment().setItemInMainHand(ItemStackUtils.EMPTY);
                ast.remove();
                astMap.remove(event.getClickedBlock());
                activateNearbyObservers(event.getClickedBlock());
            } else {
                Optional<DryerRecipe> recipeFor = DryerRecipe.getRecipeFor(event.getItem());
                if (recipeFor.isEmpty()) {
                    event.setCancelled(false);
                    return;
                }
                Location loc = event.getClickedBlock().getLocation().add(.5, -.5 + (slab.getType() == Slab.Type.TOP ? 0 : -.5), .5);
                float yaw = FaceUtil.faceToYaw(FaceUtil.yawToFace(event.getPlayer().getEyeLocation().getYaw(), false).getOppositeFace());
                if (ItemStackUtils.isToolOrWeapon(event.getItem().getType())) {
                    yaw = rotateLeft(yaw, -90);
                    loc = backward(loc, -.5d, FaceUtil.yawToFace(rotateLeft(yaw, -90), false)).add(0, -.35, 0);
                } else if(event.getItem().getType().isBlock()) {
                    loc = backward(loc, .25, FaceUtil.yawToFace(rotateLeft(yaw, -90), false)).add(0, -1d, 0);
                } else {
                    loc = loc.subtract(0, 1, 0);
                }
                loc.setYaw(yaw);
                ArmorStand ast = (ArmorStand) event.getClickedBlock().getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                ast.setMarker(true);
                ast.setInvulnerable(true);
                ast.setBasePlate(false);
                ast.setArms(true);
                ast.setInvisible(true);
                if (ItemStackUtils.isToolOrWeapon(event.getItem().getType())) {
                    ast.setRightArmPose(new EulerAngle(Math.toRadians(90), Math.toRadians(0), Math.toRadians(0)));
                } else {
                    //TODO: fix rotation for fishing rod, Spyglass, flint and steel
                    //HAHAHDH THAT'S A JOKE CAUSE FUCK OFF I'M A FUCKING AUTHORITARIAN REGIME
                    ast.setRightArmPose(new EulerAngle(Math.toRadians(250), Math.toRadians(0), Math.toRadians(45)));
                }
                if(!ast.getPersistentDataContainer().has(SunderiaUtils.key("dryer_time_left"), PersistentDataType.INTEGER)) {
                    ast.getPersistentDataContainer().set(SunderiaUtils.key("dryer_time_left"), PersistentDataType.INTEGER, recipeFor.get().getTime());
                }
                ast.getEquipment().setItemInMainHand(event.getItem());
                event.getItem().setAmount(0);
                activateNearbyObservers(event.getClickedBlock());
                astMap.put(event.getClickedBlock(), ast);
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(SunderiaUtils.getPlugin(), () -> {
                    ItemStack item = ast.getEquipment().getItemInMainHand();
                    PersistentDataContainer pdc = ast.getPersistentDataContainer();
                    int time = pdc.get(SunderiaUtils.key("dryer_time_left"), PersistentDataType.INTEGER);
                    //Get the current biome temperature
                    Biome biome = ast.getWorld().getBiome(ast.getLocation());
                    if(Arrays.stream(hotBiomes).anyMatch(biome1 -> biome1 == biome)) {
                        time -= 2;
                    } else {
                        time--;
                    }
                    if(time <= 0) {
                        ast.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, event.getClickedBlock().getLocation(), 1, 0, 0, 0);
                        ast.getWorld().playSound(event.getClickedBlock().getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1, 1);
                        astTaskMap.get(ast).cancel();
                        astTaskMap.remove(ast);
                        ItemStack stack = recipeFor.get().getOutput();
                        stack.setAmount(item.getAmount());
                        ast.getEquipment().setItemInMainHand(stack, true);
                        activateNearbyObservers(event.getClickedBlock());
                        if(event.getClickedBlock().getRelative(BlockFace.DOWN).getBlockData() instanceof Hopper hopperData) {
                            org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) event.getClickedBlock().getRelative(BlockFace.DOWN).getState();
                            if(hopperData.isEnabled()) {
                                hopper.getInventory().addItem(stack);
                                ast.getEquipment().setItemInMainHand(ItemStackUtils.EMPTY, true);
                            }
                        }
                    }
                    pdc.set(SunderiaUtils.key("dryer_time_left"), PersistentDataType.INTEGER, time);
                }, 0, 20);
                astTaskMap.put(ast, task);
            }
        }
    }

    private Location backward(Location loc, double distance, BlockFace facing) {
        Vector vec = new Vector();
        vec.setX(facing == BlockFace.EAST ? -distance : facing == BlockFace.WEST ? distance : 0);
        vec.setZ(facing == BlockFace.NORTH ? distance : facing == BlockFace.SOUTH ? -distance : 0);
        return loc.add(vec);
    }

    private void activateNearbyObservers(Block block) {
        for(BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}) {
            Block b = block.getRelative(face);
            if(b.getBlockData() instanceof CraftObserver observer) {
                BlockFace facing = observer.getFacing();
                if(b.getRelative(facing).getLocation().equals(block.getLocation())) {
                    observer.setPowered(true);
                    ((ObserverBlock) ((CraftBlock) b).getNMS().getBlock()).tick(((CraftBlockState) b.getState()).getHandle(), ((CraftWorld) b.getWorld()).getHandle(), toBlockPos(b.getLocation()), SunderiaUtils.getRandom());
                }
            }
        }
    }

    private BlockPos toBlockPos(Location loc) {
        return new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private float rotateLeft(float yaw, float add) {
        return FaceUtil.wrapAngle(yaw + add);
    }
}