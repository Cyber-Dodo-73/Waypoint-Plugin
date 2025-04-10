package fr.cyberdodo.waystone.listener;

import fr.cyberdodo.waystone.WaystonePlugin;
import fr.cyberdodo.waystone.dao.WaystoneDAO;
import fr.cyberdodo.waystone.data.WaystoneData;
import fr.cyberdodo.waystone.manager.WaystoneManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class WaystonePhysicsListener implements Listener {

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();

        // Vérifie si le bloc est une bannière (normale ou murale)
        if (block.getType().name().endsWith("_BANNER") || block.getType().name().endsWith("_WALL_BANNER")) {
            // On retarde la vérification d'un tick pour laisser le temps à la physique de remplacer la bannière par de l'air
            Bukkit.getScheduler().runTaskLater(WaystonePlugin.getInstance(), () -> {
                if (block.getType() == Material.AIR) {
                    WaystoneData data = WaystoneDAO.getWaystoneByLocation(block.getLocation());
                    if (data != null) {
                        WaystoneManager.deleteWaystone(data.getId());
                    }
                }
            }, 1L);
        }
    }
}