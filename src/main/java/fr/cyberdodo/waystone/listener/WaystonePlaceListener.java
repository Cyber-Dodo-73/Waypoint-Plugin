package fr.cyberdodo.waystone.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WaystonePlaceListener implements Listener {

    public static class PendingPlace {
        private final Block block;
        private final BlockData blockData;
        private final ItemStack item;  // <-- on mémorise l'item placé

        public PendingPlace(Block block, BlockData blockData, ItemStack item) {
            this.block = block;
            this.blockData = blockData;
            this.item = item;
        }

        public Block getBlock() {
            return block;
        }

        public BlockData getBlockData() {
            return blockData;
        }

        public ItemStack getItem() {
            return item;
        }
    }

    public static final Map<Player, PendingPlace> pendingWaystonePlacements = new HashMap<>();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Player player = event.getPlayer();

        if (item != null && item.hasItemMeta()) {
            String display = item.getItemMeta().getDisplayName();
            if (display != null && display.contains("Waystone")
                    && item.getType().name().endsWith("_BANNER")) {

                // On ne l’annule pas => Minecraft calcule l’orientation
                Block placedBlock = event.getBlockPlaced();
                BlockData data = placedBlock.getBlockData();

                // Juste après la pose, on enlève le bloc pour "forcer" la phase de naming
                placedBlock.setType(Material.AIR);

                // On stocke block + blockData + item
                pendingWaystonePlacements.put(
                        player,
                        new PendingPlace(placedBlock, data, item.clone())
                );

                player.sendMessage("§aWaystone posée.");
                player.sendMessage("§7Tapez un nom dans le chat ou 'annuler' pour annuler.");
            }
        }
    }
}