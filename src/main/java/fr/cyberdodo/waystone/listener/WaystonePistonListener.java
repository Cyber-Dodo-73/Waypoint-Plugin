package fr.cyberdodo.waystone.listener;

import fr.cyberdodo.waystone.WaystonePlugin;
import fr.cyberdodo.waystone.dao.WaystoneDAO;
import fr.cyberdodo.waystone.data.WaystoneData;
import fr.cyberdodo.waystone.manager.WaystoneManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.ArrayList;
import java.util.List;

public class WaystonePistonListener implements Listener {

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        checkBlocksAffected(event.getBlocks());
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        checkBlocksAffected(event.getBlocks());
    }

    private void checkBlocksAffected(List<Block> movedBlocks) {
        for (Block block : movedBlocks) {
            // Vérifie les bannières affectées par le mouvement
            for (Block affectedBanner : getBannièresAutour(block)) {
                WaystoneData data = WaystoneDAO.getWaystoneByLocation(affectedBanner.getLocation());
                if (data != null) {
                    Bukkit.getScheduler().runTask(WaystonePlugin.getInstance(), () -> {
                        //drops the banner
                        affectedBanner.breakNaturally();
                    });
                    WaystoneManager.deleteWaystone(data.getId());
                }
            }
        }
    }

    private List<Block> getBannièresAutour(Block block) {
        List<Block> bannieres = new ArrayList<>();
        Block up = block.getRelative(0, 1, 0);
        if (up.getType().name().endsWith("_BANNER")) {
            bannieres.add(up);
        }

        for (BlockFace face : List.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
            Block side = block.getRelative(face);
            if (side.getType().name().endsWith("_WALL_BANNER")) {
                bannieres.add(side);
            }
        }

        return bannieres;
    }
}