package fr.cyberdodo.waystone;

import fr.cyberdodo.waystone.config.SQLiteDatabase;
import fr.cyberdodo.waystone.item.WaystoneItem;
import fr.cyberdodo.waystone.listener.*;
import fr.cyberdodo.waystone.manager.WaystoneParticleManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public class WaystonePlugin extends JavaPlugin {

    private static WaystonePlugin instance;
    private SQLiteDatabase database;

    @Override
    public void onEnable() {
        instance = this;

        // Initialiser la base de données
        database = new SQLiteDatabase(this, "waystones.db");
        try {
            database.connect();
            database.createTableIfNotExists();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Impossible de se connecter à la base de données!", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Enregistrer toutes les recettes de bannière (16 couleurs)
        WaystoneItem.registerWaystoneRecipe();

        // Enregistrer les listeners
        getServer().getPluginManager().registerEvents(new WaystonePlaceListener(), this);
        getServer().getPluginManager().registerEvents(new WaystoneChatListener(), this);
        getServer().getPluginManager().registerEvents(new WaystoneInteractListener(), this);
        getServer().getPluginManager().registerEvents(new WaystoneInventoryListener(), this);
        getServer().getPluginManager().registerEvents(new WaystoneBreakListener(), this);
        getServer().getPluginManager().registerEvents(new WaystoneExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new WaystoneTeleportListener(), this);
        getServer().getPluginManager().registerEvents(new WaystoneCraftListener(), this);
        getServer().getPluginManager().registerEvents(new WaystonePistonListener(), this);
        getServer().getPluginManager().registerEvents(new WaystonePhysicsListener(), this);

        WaystoneParticleManager.startParticleTask();

        getLogger().info("[WaystonePlugin] Plugin activé avec succès !");
    }

    @Override
    public void onDisable() {
        try {
            if (database != null) {
                database.close();
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Erreur lors de la fermeture de la base de données!", e);
        }
        getLogger().info("[WaystonePlugin] Plugin désactivé.");
    }

    public static WaystonePlugin getInstance() {
        return instance;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }
}