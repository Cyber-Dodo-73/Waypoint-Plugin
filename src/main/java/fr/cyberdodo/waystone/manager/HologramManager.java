package fr.cyberdodo.waystone.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class HologramManager {

    // Map associant l'ID de la waystone (int) à l'ArmorStand de l'hologramme
    private static final Map<Integer, ArmorStand> holograms = new HashMap<>();

    /**
     * Crée et stocke l'hologramme associé à une waystone.
     *
     * @param waystoneId   l'identifiant de la waystone
     * @param world        le monde sur lequel créer l'hologramme
     * @param location     la position de la waystone (souvent la bannière)
     * @param waystoneName le nom affiché par l'hologramme
     * @return l'ArmorStand créé
     */
    public static ArmorStand createWaystoneHologram(int waystoneId, World world, Location location, String waystoneName) {
        // Détecter si c'est une bannière murale ou au sol
        String blockType = location.getBlock().getType().name();
        double yOffset;

        if (blockType.contains("_WALL_BANNER")) {
            // Bannière murale : hauteur plus basse
            yOffset = 1.2;
        } else {
            // Bannière au sol : hauteur plus haute
            yOffset = 2.0;
        }

        // Positionner l'hologramme au-dessus de la bannière
        Location holoLocation = location.clone().add(0.5, yOffset, 0.5);
        ArmorStand hologram = (ArmorStand) world.spawnEntity(holoLocation, EntityType.ARMOR_STAND);
        hologram.setVisible(false);              // rendre l'ArmorStand invisible
        hologram.setGravity(false);              // désactiver la gravité pour qu'il ne bouge pas
        hologram.setMarker(true);                // supprimer le hitbox
        hologram.setCustomName(ChatColor.translateAlternateColorCodes('&', waystoneName));
        hologram.setCustomNameVisible(true);     // afficher le nom

        holograms.put(waystoneId, hologram);
        return hologram;
    }

    /**
     * Renvoie l'hologramme associé à une waystone.
     *
     * @param waystoneId l'identifiant de la waystone
     * @return l'ArmorStand associé ou null si introuvable dans la map
     */
    public static ArmorStand getHologram(int waystoneId) {
        return holograms.get(waystoneId);
    }

    /**
     * Recherche dans le monde un ArmorStand dont l'ID d'entité correspond.
     *
     * @param entityId l'ID de l'entité (ArmorStand) recherché
     * @return l'ArmorStand trouvé ou null si introuvable
     */
    public static ArmorStand findHologramInWorld(int entityId) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getEntityId() == entityId && entity instanceof ArmorStand) {
                    return (ArmorStand) entity;
                }
            }
        }
        return null;
    }

    /**
     * Supprime l'hologramme associé à la waystone à partir de son ID de waystone.
     * Si non présent dans la map (cas reload), recherche l'ArmorStand dans le monde.
     *
     * @param waystoneId       l'identifiant de la waystone
     * @param hologramEntityId l'ID de l'entité hologramme stocké en DB
     */
    public static void deleteWaystoneHologram(int waystoneId, int hologramEntityId) {
        ArmorStand hologram = holograms.remove(waystoneId);
        if (hologram == null) {
            // En cas de reload, la map est vide : on recherche l'ArmorStand par son entityId
            hologram = findHologramInWorld(hologramEntityId);
        }
        if (hologram != null && !hologram.isDead()) {
            hologram.remove();
        }
    }
}