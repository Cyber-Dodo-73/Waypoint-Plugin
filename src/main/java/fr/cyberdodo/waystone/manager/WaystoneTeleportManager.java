package fr.cyberdodo.waystone.manager;

import fr.cyberdodo.waystone.WaystonePlugin;
import fr.cyberdodo.waystone.data.WaystoneData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class WaystoneTeleportManager {

    private static final Map<Player, TeleportData> teleportingPlayers = new HashMap<>();

    /**
     * Contient la destination et l'ID de la tâche en cours
     */
    private static class TeleportData {
        final WaystoneData target;
        final int taskId;

        private TeleportData(WaystoneData target, int taskId) {
            this.target = target;
            this.taskId = taskId;
        }
    }

    /**
     * Lance la téléportation avec animation/effets sur 5 secondes
     */
    public static void startTeleport(Player player, WaystoneData target) {
        // Vérifie si déjà en TP
        if (teleportingPlayers.containsKey(player)) {
            // Optionnel : message d’erreur
            player.sendMessage("§cVous êtes déjà en train de vous téléporter !");
            return;
        }

        // Vérifie si le joueur a assez de niveaux d'XP
        if (player.getLevel() < 1) {
            player.sendMessage("§cVous devez avoir au moins 1 niveau d'XP pour vous téléporter !");
            return;
        }
        // On retire 1 niveau d'XP
        player.setLevel(player.getLevel() - 1);

        // Appliquer Blindness + Nausea 6s (le temps + marge)
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 6 * 20, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 6 * 20, 0, false, false));

        // On crée une tâche qui va durer 5s
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(WaystonePlugin.getInstance(),
                new TeleportTicker(player, target), 0L, 1L);

        // On stocke la destination + l'ID de la tâche pour l'annuler/terminer plus tard
        teleportingPlayers.put(player, new TeleportData(target, taskId));
    }

    /**
     * Annule la téléportation du joueur (ex: s'il prend des dégâts)
     */
    public static void cancelTeleport(Player player) {
        TeleportData data = teleportingPlayers.remove(player);
        if (data != null) {
            // On arrête la tâche
            Bukkit.getScheduler().cancelTask(data.taskId);
            player.sendMessage("§cTéléportation annulée (vous avez subi des dégâts ou autre).");

            // Retirer les effets si tu veux
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.NAUSEA);
        }
    }

    /**
     * Finit la téléportation (au bout des 5s)
     */
    public static void finishTeleport(Player player) {
        TeleportData data = teleportingPlayers.remove(player);
        if (data == null) {
            return; // déjà annulé
        }
        // On arrête la tâche
        Bukkit.getScheduler().cancelTask(data.taskId);

        // On téléporte
        WaystoneData target = data.target;
        Location dest = target.getLocation();
        dest.add(0.5, 0, 0.5); // On centre la téléportation sur le bloc
        if (dest != null && dest.getWorld() != null) {
            player.teleport(dest);

            // On veut un GROS effet, donc on peut le faire immédiatement, ou avec un léger delay
            // Pour un effet plus “percutant”, on le fait immédiatement par exemple :
            Location effectLoc = player.getLocation().add(0, 1, 0);
            World w = effectLoc.getWorld();

            // ------------------- EXEMPLE 1 : Explosion Large -------------------
            // "EXPLOSION_LARGE" apparaît comme une grosse explosion.
            // count = 10, offset = 1 => un nuage plus large
            w.spawnParticle(Particle.LARGE_SMOKE,
                    effectLoc.getX(), effectLoc.getY(), effectLoc.getZ(),
                    10,    // count
                    1.0,   // offsetX
                    1.0,   // offsetY
                    1.0,   // offsetZ
                    0      // extra speed, souvent 0
            );

            // ------------------- EXEMPLE 2 : Nuage de fumée (SMOKE_LARGE) -------------------
            // On spawn un gros nuage de fumée au même endroit
            w.spawnParticle(Particle.CLOUD,
                    effectLoc.getX(), effectLoc.getY(), effectLoc.getZ(),
                    50,    // count: bcp de fumée
                    1.2,   // offset
                    1.2,
                    1.2,
                    0.01   // ‘extra’ peut donner un petit mouvement
            );

            // ------------------- EXEMPLE 3 : Particules de flammes tout autour -------------------
            w.spawnParticle(Particle.FLAME,
                    effectLoc.getX(), effectLoc.getY(), effectLoc.getZ(),
                    100,   // beaucoup de flammes
                    2.0,   // offsets
                    1.5,
                    2.0,
                    0.02   // vitesse
            );

            // ------------------- SON DE TÉLÉPORTATION -------------------
            w.playSound(effectLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }

        // On peut retirer les effets, s'il en reste
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.NAUSEA);
    }


    /**
     * Vérifie si un joueur est en train de se téléporter
     */
    public static boolean isTeleporting(Player player) {
        return teleportingPlayers.containsKey(player);
    }

    /**
     * Tâche répétitive qui gère les 5s d’animation/sons
     */
    private static class TeleportTicker implements Runnable {
        private final Player player;
        private final WaystoneData target;

        private int tick = 0;
        private final int totalTicks = 100; // 5 secondes = 5 * 20 ticks

        public TeleportTicker(Player player, WaystoneData target) {
            this.player = player;
            this.target = target;
        }

        @Override
        public void run() {
            // Vérifie si le joueur est toujours dans la map
            if (!isTeleporting(player)) {
                // plus de tp => on arrête
                return;
            }

            // Particules autour du joueur (un peu plus haut)
            Location loc = player.getLocation().add(0, 1, 0);
            loc.getWorld().spawnParticle(
                    Particle.PORTAL,
                    loc.getX(), loc.getY(), loc.getZ(),
                    30, // count
                    0.3, 0.5, 0.3, // offsets
                    0
            );

            // Jouer un son sur le joueur (bloc plus haut)
            if (tick % 20 == 0) {
                // toutes les secondes, par exemple
                loc.getWorld().playSound(loc,
                        Sound.ENTITY_ILLUSIONER_MIRROR_MOVE,
                        1.0f,
                        1.0f
                );
            }
            // Si on veut accélérer la fréquence du son, on peut faire un algo plus poussé

            // Au bout de 5s, on termine
            if (tick >= totalTicks) {
                WaystoneTeleportManager.finishTeleport(player);
            }
            tick++;
        }
    }
}