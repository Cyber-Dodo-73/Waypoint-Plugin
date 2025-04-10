package fr.cyberdodo.waystone.listener;

import fr.cyberdodo.waystone.manager.WaystoneTeleportManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class WaystoneTeleportListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        if (victim instanceof Player) {
            Player player = (Player) victim;
            // Si le joueur est en cours de TP, on l’annule
            WaystoneTeleportManager.cancelTeleport(player);
        }
    }
}