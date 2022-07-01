package fr.sunderia.sunderiasmp.recipes;

import fr.sunderia.sunderiautils.utils.ItemStackUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DryerRecipe {

    private static final List<DryerRecipe> recipes = new ArrayList<>();

    private final NamespacedKey key;
    private final ItemStack input;
    private final ItemStack output;
    private final int time;

    public DryerRecipe(NamespacedKey key, ItemStack input, ItemStack output, int timeInSeconds) {
        this.key = key;
        this.input = input;
        this.output = output;
        this.time = timeInSeconds;
    }

    public static Optional<DryerRecipe> getRecipe(NamespacedKey key) {
        return recipes.stream().filter(recipe -> recipe.key.equals(key)).findFirst();
    }

    public static Optional<DryerRecipe> getRecipeFor(ItemStack item) {
        if(ItemStackUtils.isAirOrNull(item)) return Optional.empty();
        return recipes.stream().filter(recipe -> recipe.input.getType() == item.getType()).findFirst();
    }

    public void addRecipe() {
        if(recipes.stream().anyMatch(recipe -> recipe.input.getType() == this.input.getType())) throw new IllegalArgumentException("Recipe with this input already exists.");
        recipes.add(this);
    }

    public NamespacedKey getKey() {
        return key;
    }

    public ItemStack getInput() {
        return input.clone();
    }

    public ItemStack getOutput() {
        return output.clone();
    }

    public int getTime() {
        return time;
    }
}
