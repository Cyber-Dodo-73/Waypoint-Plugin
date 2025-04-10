package fr.cyberdodo.waystone.inventory;

import fr.cyberdodo.waystone.dao.WaystoneDAO;
import fr.cyberdodo.waystone.data.WaystoneData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WaystoneInventory {

    public static Inventory buildWaystoneInventory() {
        Inventory inv = Bukkit.createInventory(null, 54, "Waystone Téléportation");
        List<WaystoneData> all = WaystoneDAO.getAllWaystones();
        int index = 0;
        for (WaystoneData wd : all) {
            if (index >= 53) break;

            // Cloner l'item de la bannière afin de ne pas altérer l'original
            ItemStack banner = wd.getBannerItem().clone();
            ItemMeta meta = banner.getItemMeta();

            // Définir le display name avec le nom de la waystone
            meta.setDisplayName("§e" + ChatColor.translateAlternateColorCodes('&', wd.getName()));

            // On efface le lore par défaut qui contient les infos de la bannière
            List<String> lore = new ArrayList<>();
            // On ajoute les coordonnées et le monde
            lore.add(ChatColor.GRAY + "X: " + wd.getLocation().getBlockX());
            lore.add(ChatColor.GRAY + "Y: " + wd.getLocation().getBlockY());
            lore.add(ChatColor.GRAY + "Z: " + wd.getLocation().getBlockZ());
            lore.add(ChatColor.GRAY + "Monde: " + wd.getLocation().getWorld().getName());
            meta.setLore(lore);

            // Ajout d'item flags pour masquer d'éventuels attributs ou informations par défaut
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_BANNER_PATTERNS);

            banner.setItemMeta(meta);
            inv.setItem(index++, banner);
        }

        // Ajout de l'anvil (enclume) pour le renommage en slot 53
        ItemStack anvil = new ItemStack(Material.ANVIL);
        ItemMeta anvilMeta = anvil.getItemMeta();
        anvilMeta.setDisplayName("§cRenommer cette Waystone");
        anvil.setItemMeta(anvilMeta);
        inv.setItem(53, anvil);

        return inv;
    }
}