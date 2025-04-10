package fr.cyberdodo.waystone.listener;

import fr.cyberdodo.waystone.dao.WaystoneDAO;
import fr.cyberdodo.waystone.data.WaystoneData;
import fr.cyberdodo.waystone.manager.WaystoneManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class WaystoneBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType().name().endsWith("_BANNER")) {
            WaystoneData data = WaystoneDAO.getWaystoneByLocation(block.getLocation());
            if (data != null) {
                WaystoneManager.deleteWaystone(data.getId());
                event.getPlayer().sendMessage("§cWaystone supprimée: " + ChatColor.translateAlternateColorCodes('&', data.getName()));
            }
        }
    }
}