package fr.cyberdodo.waystone.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

public class WaystoneCraftListener implements Listener {

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        // Vérifier si c'est notre recette
        CraftingInventory inv = event.getInventory();
        Recipe recipe = event.getRecipe();
        if (recipe == null) {
            return;
        }
        ItemStack result = recipe.getResult();
        if (result == null) {
            return;
        }

        // Vérifie si c’est bien la recette "waystone_recipe"
        if (!(recipe instanceof ShapedRecipe shaped)) {
            return;
        }
        if (!shaped.getKey().getKey().equalsIgnoreCase("waystone_recipe")) {
            return;
        }

        // Récupère la matrice du craft (3x3)
        ItemStack[] matrix = inv.getMatrix();

        // On va chercher l'item BANNIÈRE dans la matrice
        ItemStack bannerItem = null;
        for (ItemStack stack : matrix) {
            if (stack != null && stack.getType().name().endsWith("_BANNER")) {
                bannerItem = stack;
                break;
            }
        }
        if (bannerItem == null) {
            return;
        }

        // Récupère le montant prévu par la recette
        int resultAmount = result.getAmount();

        // Crée le nouvel ItemStack en clonant la bannière d'entrée
        ItemStack newResult = bannerItem.clone();
        newResult.setAmount(resultAmount);  // Définir le montant correct

        // Modifier le nom pour indiquer que c'est une Waystone
        ItemMeta newMeta = newResult.getItemMeta();
        newMeta.setDisplayName("§aWaystone");
        newResult.setItemMeta(newMeta);

        // On set le nouveau résultat dans l'inventaire de craft
        inv.setResult(newResult);
    }
}