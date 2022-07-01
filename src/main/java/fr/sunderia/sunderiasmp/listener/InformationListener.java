package fr.sunderia.sunderiasmp.listener;

import fr.sunderia.sunderiautils.SunderiaUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.Map;

public class InformationListener implements Listener {

    private static final Map<Player, BukkitTask> tasks = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        tasks.put(event.getPlayer(), Bukkit.getScheduler().runTaskTimer(SunderiaUtils.getPlugin(), () -> {
            Location location = event.getPlayer().getEyeLocation();
            RayTraceResult result = event.getPlayer().getWorld().rayTraceBlocks(location, location.getDirection(), 10d, FluidCollisionMode.NEVER);
            if(result == null || result.getHitBlock() == null) return;
            if(DryerListener.astMap.containsKey(result.getHitBlock())) {
                int timeLeft = DryerListener.astMap.get(result.getHitBlock()).getPersistentDataContainer().get(SunderiaUtils.key("dryer_time_left"), PersistentDataType.INTEGER);
                if(timeLeft == 0) {
                    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§aDryer is ready!"));
                } else {
                    event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§7Dryer : §e" + timeLeft + "s"));
                }
            }
        }, 0, 5));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        tasks.get(event.getPlayer()).cancel();
        tasks.remove(event.getPlayer());
    }

}
