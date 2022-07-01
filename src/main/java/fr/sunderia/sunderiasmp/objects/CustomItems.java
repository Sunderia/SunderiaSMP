package fr.sunderia.sunderiasmp.objects;

import fr.sunderia.sunderiautils.SunderiaUtils;
import fr.sunderia.sunderiautils.customblock.CustomBlock;
import fr.sunderia.sunderiautils.utils.ItemBuilder;
import fr.sunderia.sunderiautils.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public enum CustomItems {

    RUBY_ORE(new CustomBlock(SunderiaUtils.key("ruby_ore"), 1, 100, new ItemBuilder(Material.EMERALD).setDisplayName(ChatColor.RED + "Ruby").build())),
    ;

    private final CustomBlock customBlock;
    private final ItemStack stack;

    CustomItems(CustomBlock customBlock) {
        this.customBlock = customBlock;
        this.stack = new ItemBuilder(customBlock.getMat())
                .setDisplayName(StringUtils.capitalizeWord(customBlock.getName().getKey().replace("_", " ").toLowerCase()))
                .setCustomModelData(customBlock.getCustomModelData())
                .build();
    }

    CustomItems(ItemStack stack) {
        this.stack = stack;
        this.customBlock = null;
    }

    public static List<ItemStack> getAllBlockItems() {
        return Arrays.stream(values()).filter(CustomItems::isCustomBlock).map(CustomItems::getStack).toList();
    }

    public boolean isCustomBlock() {
        return customBlock != null;
    }

    public NamespacedKey getKey() {
        return !isCustomBlock() ? null : customBlock.getName();
    }

    public CustomBlock getCustomBlock() {
        return customBlock.clone();
    }

    public ItemStack getStack() {
        return stack;
    }
}
