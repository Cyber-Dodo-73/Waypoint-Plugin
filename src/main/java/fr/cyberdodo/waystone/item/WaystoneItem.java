package fr.cyberdodo.waystone.item;

import org.bukkit.*;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class WaystoneItem {

    public static void registerWaystoneRecipe() {
        // On crée un item placeholder
        ItemStack placeholder = new ItemStack(Material.WHITE_BANNER, 1);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName("Waystone (dummy)");
        placeholder.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(
                JavaPlugin.getProvidingPlugin(WaystoneItem.class),
                "waystone_recipe"
        );
        ShapedRecipe recipe = new ShapedRecipe(key, placeholder);

        recipe.shape("EDE", "EBE", "ESE");

        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('D', Material.DIAMOND);
        // Ici on veut toutes les bannières
        recipe.setIngredient('B', new RecipeChoice.MaterialChoice(Tag.ITEMS_BANNERS));
        recipe.setIngredient('S', Material.STICK);

        Bukkit.addRecipe(recipe);
    }

}