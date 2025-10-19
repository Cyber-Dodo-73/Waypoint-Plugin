package fr.cyberdodo.waystone.listener;

import fr.cyberdodo.waystone.WaystonePlugin;
import fr.cyberdodo.waystone.dao.WaystoneDAO;
import fr.cyberdodo.waystone.data.WaystoneData;
import fr.cyberdodo.waystone.manager.WaystoneManager;
import fr.cyberdodo.waystone.manager.WaystoneTeleportManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WaystoneInventoryListener implements Listener {

    // Map pour la phase de renommage (nouveau nom)
    private static final Map<Player, WaystoneData> renameInProgress = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!event.getView().getTitle().equals("Waystone Téléportation")) {
            return;
        }
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot < 0 || slot > 53) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        // ENCLUME => Renommer
        if (slot == 53 && clicked.getType() == Material.ANVIL) {
            // Récupérer la Waystone qu’on a cliquée via PlayerInteractEvent
            WaystoneData last = WaystoneInteractListener.lastClickedWaystone.get(player);
            if (last == null) {
                player.closeInventory();
                player.sendMessage("§cAucune Waystone à renommer...");
                return;
            }

            // On met en "renameInProgress"
            renameInProgress.put(player, last);
            player.closeInventory();
            player.sendMessage("§6Entrez le nouveau nom pour la Waystone : "
                    + ChatColor.translateAlternateColorCodes('&', last.getName()));
            return;
        }

        // Sinon, c’est une bannière => on initie la téléportation
        if (clicked.getType().name().endsWith("_BANNER")) {
            // On retire les codes de couleur pour comparer le nom "pur"
            String displayName = ChatColor.stripColor(Objects.requireNonNull(clicked.getItemMeta()).getDisplayName());

            WaystonePlugin.getInstance().getLogger().info("Display name: " + displayName);

            // Retrouver la Waystone correspondante
            List<WaystoneData> all = WaystoneDAO.getAllWaystones();
            WaystoneData target = null;
            for (WaystoneData wd : all) {
                // Convertir les codes couleur '&' en '§', puis retirer les couleurs pour obtenir le nom pur
                String dbName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', wd.getName()));
                WaystonePlugin.getInstance().getLogger().info("Waystone (DB): " + dbName);
                if (dbName.equalsIgnoreCase(displayName)) {
                    target = wd;
                    break;
                }
            }
            if (target != null) {
                WaystonePlugin.getInstance().getLogger().info("Target: " + target);
            } else {
                WaystonePlugin.getInstance().getLogger().info("Target not found");
            }
            if (target != null) {
                // On ferme l’inventaire
                player.closeInventory();
                // On déclenche la procédure de TP (5s, etc.)
                WaystoneTeleportManager.startTeleport(player, target);
            }
        }
    }

    // Gère le chat pour le nouveau nom
    @EventHandler
    public void onChatRename(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!renameInProgress.containsKey(player)) {
            return; // pas en phase rename
        }

        event.setCancelled(true);

        WaystoneData data = renameInProgress.remove(player);
        String newName = event.getMessage();

        // Mise à jour DB
        WaystoneManager.renameWaystone(data.getId(), newName, player);
    }
}