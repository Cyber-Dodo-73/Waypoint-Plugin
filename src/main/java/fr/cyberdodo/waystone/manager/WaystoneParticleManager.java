package fr.cyberdodo.waystone.manager;

import fr.cyberdodo.waystone.WaystonePlugin;
import fr.cyberdodo.waystone.dao.WaystoneDAO;
import fr.cyberdodo.waystone.data.WaystoneData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class WaystoneParticleManager {

    /**
     * Démarre une tâche répétée qui affiche des particules de type table d'enchantement
     * au-dessus de chaque waystone.
     */
    public static void startParticleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Récupère toutes les waystones depuis la DB
                List<WaystoneData> waystones = WaystoneDAO.getAllWaystones();
                for (WaystoneData data : waystones) {
                    // Calculer la position de la particule : par exemple, 1,5 blocs au-dessus du bloc de la waystone
                    Location loc = data.getLocation().clone().add(0.5, 1.5, 0.5);
                    // Affiche 10 particules avec un léger offset aléatoire et une vitesse d'animation
                    loc.getWorld().spawnParticle(Particle.ENCHANT, loc, 10, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }.runTaskTimer(WaystonePlugin.getInstance(), 0L, 20L); // actualisation toutes les 20 ticks (1 seconde)
    }
}