package fr.sunderia.sunderiasmp.objects;

import fr.sunderia.sunderiautils.utils.InventoryBuilder;
import fr.sunderia.sunderiautils.utils.ItemBuilder;
import fr.sunderia.sunderiautils.utils.ItemStackUtils;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;

public class Inventories {

    private Inventories() {}

    private static final Inventory CUSTOM_ITEMS = new InventoryBuilder("Custom Items", new InventoryBuilder.Shape("""
            █████████
            █       █
            █       █
            █       █
            ████❌████
            """, Map.of('█', new ItemStack(Material.GRAY_STAINED_GLASS_PANE), '❌', new ItemBuilder(Material.ARROW).setDisplayName("Page").build())))
            .onOpen(event -> {
                Arrays.stream(event.getInventory().getContents())
                        .filter(is -> ItemStackUtils.isNotAirNorNull(is) && ItemStackUtils.isCustomItem(is) && is.getType() != Material.ARROW)
                        .forEach(stack -> event.getInventory().setItem(event.getInventory().first(stack), ItemStackUtils.EMPTY));
                event.getInventory().addItem(Arrays.stream(CustomItems.values()).map(CustomItems::getStack).toArray(ItemStack[]::new));
            })
            .onClick(event -> {
                if(ItemStackUtils.isAirOrNull(event.getCurrentItem()) || !ItemStackUtils.isCustomItem(event.getCurrentItem()) || event.getCurrentItem().getType() == Material.ARROW) return;
                ItemBuilder ib = new ItemBuilder(event.getCurrentItem()).setAmount(1);
                event.getWhoClicked().getInventory().addItem(event.isShiftClick() ? ib.setAmount(64).build() : ib.build());
            })
            .setCancelled().build();

    public static Inventory getCustomItems() {
        return CUSTOM_ITEMS;
    }
}
