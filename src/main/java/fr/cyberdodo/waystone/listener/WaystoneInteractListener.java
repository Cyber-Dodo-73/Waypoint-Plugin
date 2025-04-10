package fr.cyberdodo.waystone.listener;

import fr.cyberdodo.waystone.dao.WaystoneDAO;
import fr.cyberdodo.waystone.data.WaystoneData;
import fr.cyberdodo.waystone.inventory.WaystoneInventory;
import fr.cyberdodo.waystone.manager.WaystoneTeleportManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class WaystoneInteractListener implements Listener {

    // Mémorise la dernière Waystone cliquée par chaque joueur
    public static Map<Player, WaystoneData> lastClickedWaystone = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {

            Player player = event.getPlayer();

            // Vérifie si en TP
            if (WaystoneTeleportManager.isTeleporting(player)) {
                event.setCancelled(true);
                player.sendMessage("§cVous êtes en train de vous téléporter, attendez la fin !");
                return;
            }

            Block block = event.getClickedBlock();
            if (block.getType().name().endsWith("_BANNER")) {
                // Vérifier si c’est un Waystone
                WaystoneData data = WaystoneDAO.getWaystoneByLocation(block.getLocation());
                if (data == null) {
                    return; // pas dans la DB
                }
                event.setCancelled(true);
                // On mémorise que ce joueur a cliqué sur "data"
                lastClickedWaystone.put(player, data);

                // Ouvrir l’inventaire custom
                player.openInventory(WaystoneInventory.buildWaystoneInventory());
            }
        }
    }
}