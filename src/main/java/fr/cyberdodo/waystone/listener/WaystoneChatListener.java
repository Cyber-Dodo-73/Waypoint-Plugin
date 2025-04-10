package fr.cyberdodo.waystone.listener;

import fr.cyberdodo.waystone.WaystonePlugin;
import fr.cyberdodo.waystone.dao.WaystoneDAO;
import fr.cyberdodo.waystone.manager.WaystoneManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.block.Banner;

public class WaystoneChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();

        if (!WaystonePlaceListener.pendingWaystonePlacements.containsKey(player)) {
            return;
        }
        event.setCancelled(true);

        String input = event.getMessage();
        if ("annuler".equalsIgnoreCase(input)) {
            WaystonePlaceListener.pendingWaystonePlacements.remove(player);
            player.sendMessage("§cPose de la Waystone annulée.");
            return;
        }

        final WaystonePlaceListener.PendingPlace pending =
                WaystonePlaceListener.pendingWaystonePlacements.remove(player);

        final Block block = pending.getBlock();
        final BlockData originalData = pending.getBlockData();
        final ItemStack bannerItem = pending.getItem(); // L'item complet (avec motifs)

        // Sur le thread principal
        Bukkit.getScheduler().runTask(WaystonePlugin.getInstance(), () -> {
            // 1) On remet la bannière avec la bonne orientation
            block.setBlockData(originalData);

            // 2) On récupère le BlockState (TileEntity)
            BlockState state = block.getState();
            if (state instanceof Banner bannerState) {
                // 3) On prend le BannerMeta de l’item (motifs, base color…)
                if (bannerItem.getItemMeta() instanceof BannerMeta itemMeta) {
                    // 4) On applique les mêmes motifs à la bannière posée
                    bannerState.setPatterns(itemMeta.getPatterns());
                    bannerState.update(); // 5) On valide
                }
            }

            // Enfin, on insère en DB (sérialisation de l'item complet)
            WaystoneManager.createWaystone(input, bannerItem, block.getLocation(), player);
        });
    }
}