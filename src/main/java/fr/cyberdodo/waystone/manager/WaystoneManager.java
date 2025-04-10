package fr.cyberdodo.waystone.manager;

import fr.cyberdodo.waystone.dao.WaystoneDAO;
import fr.cyberdodo.waystone.data.WaystoneData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WaystoneManager {

    /**
     * Crée une waystone :
     * - Insère la waystone dans la base de données avec un hologram_id initialisé à 0.
     * - Récupère la waystone insérée afin d'obtenir son ID.
     * - Crée l'hologramme associé et met à jour la DB avec l'ID de l'hologramme (obtention via ArmorStand#getEntityId()).
     *
     * @param name      Le nom de la waystone
     * @param itemStack L'ItemStack complet de la bannière (avec motifs, etc.)
     * @param location  La position où la waystone est posée
     * @param player    Le joueur qui pose la waystone (pour message)
     */
    public static void createWaystone(String name, ItemStack itemStack, Location location, Player player) {
        // Insertion en DB avec hologram_id = 0 (valeur par défaut)
        itemStack.setAmount(1); // On s'assure que l'item est bien un seul exemplaire
        WaystoneDAO.insertWaystone(name, itemStack, location, 0);

        // Récupération de la waystone insérée pour obtenir son ID
        WaystoneData data = WaystoneDAO.getWaystoneByLocation(location);
        if (data != null) {
            World world = location.getWorld();
            // Création de l'hologramme au-dessus de la bannière
            ArmorStand hologram = HologramManager.createWaystoneHologram(data.getId(), world, location, name);
            int hologramId = hologram.getEntityId();
            // Mise à jour en DB avec l'ID réel de l'hologramme
            WaystoneDAO.updateWaystoneHologramId(data.getId(), hologramId);
        }
        player.sendMessage("§aWaystone " + ChatColor.translateAlternateColorCodes('&', name) + "§r§a créée !");
    }

    /**
     * Supprime une waystone :
     * - Supprime l'hologramme associé.
     * - Supprime la waystone de la base de données.
     *
     * @param id L'identifiant de la waystone à supprimer.
     */
    public static void deleteWaystone(int id) {
        // Récupérer la waystone depuis la DB pour avoir accès à l'hologram id
        WaystoneData data = WaystoneDAO.getWaystoneById(id);
        if(data == null) return;

        int hologramEntityId = data.getHologramId();
        // Suppression de l'hologramme associé en passant la waystoneId et l'entityId
        HologramManager.deleteWaystoneHologram(id, hologramEntityId);
        // Suppression de la waystone dans la DB
        WaystoneDAO.deleteWaystone(id);
    }

    /**
     * Renomme une waystone :
     * - Met à jour le nom de la waystone dans la base de données.
     * - Met à jour le nom affiché par l'hologramme associé, s'il existe.
     *
     * @param id      L'identifiant de la waystone à renommer.
     * @param newName Le nouveau nom de la waystone.
     * @param player  Le joueur qui effectue l'opération (pour message)
     */
    public static void renameWaystone(int id, String newName, Player player) {
        // Mise à jour du nom dans la DB
        WaystoneDAO.updateWaystoneName(id, newName);
        // Mise à jour de l'hologramme (si existant)
        ArmorStand hologram = HologramManager.getHologram(id);
        if (hologram == null) {
            // En cas de reload, recherche dans le monde
            WaystoneData data = WaystoneDAO.getWaystoneById(id);
            if (data != null) {
                hologram = HologramManager.findHologramInWorld(data.getHologramId());
            }
        }
        if (hologram != null && !hologram.isDead()) {
            hologram.setCustomName(ChatColor.translateAlternateColorCodes('&', newName));
        }
        player.sendMessage("§aWaystone renommée en " + ChatColor.translateAlternateColorCodes('&', newName) + "§r§a !");
    }
}