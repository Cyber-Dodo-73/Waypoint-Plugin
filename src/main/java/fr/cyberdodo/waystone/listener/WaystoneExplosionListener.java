package fr.cyberdodo.waystone.listener;

import fr.cyberdodo.waystone.dao.WaystoneDAO;
import fr.cyberdodo.waystone.data.WaystoneData;
import fr.cyberdodo.waystone.manager.WaystoneManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class WaystoneExplosionListener implements Listener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // Récupère la liste des blocs détruits par l’explosion
        List<Block> blocks = event.blockList();
        for (Block block : blocks) {
            // On vérifie si c’est un banner
            if (block.getType().name().endsWith("_BANNER")) {
                // On regarde si c’est une Waystone
                WaystoneData data = WaystoneDAO.getWaystoneByLocation(block.getLocation());
                if (data != null) {
                    WaystoneManager.deleteWaystone(data.getId());
                    // On ne peut pas forcément dire "event.getPlayer()" ici, car c’est une entité
                    // explosive, pas un joueur. Tu peux faire un log en console ou rien.
                }
            }
        }
    }
}