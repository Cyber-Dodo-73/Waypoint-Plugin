package fr.cyberdodo.waystone.config;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;

public class SQLiteDatabase {

    private final Plugin plugin;
    private final String fileName;
    private Connection connection;

    public SQLiteDatabase(Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    public void connect() throws SQLException {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File dbFile = new File(plugin.getDataFolder(), fileName);
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);
    }

    public void createTableIfNotExists() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Créer la table si elle n'existe pas
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS waystones (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "name TEXT NOT NULL, " +
                            "world TEXT NOT NULL, " +
                            "x DOUBLE NOT NULL, " +
                            "y DOUBLE NOT NULL, " +
                            "z DOUBLE NOT NULL, " +
                            "item_base64 TEXT NOT NULL, " +
                            "hologram_id INTEGER NOT NULL DEFAULT 0" +
                            ");"
            );

            // Migration : Ajouter la colonne hologram_id si elle n'existe pas déjà
            // Cette requête échouera silencieusement si la colonne existe déjà
            try {
                statement.executeUpdate("ALTER TABLE waystones ADD COLUMN hologram_id INTEGER NOT NULL DEFAULT 0");
                plugin.getLogger().info("Colonne hologram_id ajoutée à la base de données existante.");
            } catch (SQLException e) {
                // La colonne existe déjà, on ignore l'erreur
                if (!e.getMessage().contains("duplicate column name")) {
                    throw e;
                }
            }

            // Migration : Supprimer la colonne playerUUID si elle existe (ancienne version)
            // SQLite ne supporte pas DROP COLUMN directement, on doit recréer la table
            try {
                // Vérifier si la colonne playerUUID existe
                boolean hasPlayerUUID = false;
                try (ResultSet rs = connection.getMetaData().getColumns(null, null, "waystones", "playerUUID")) {
                    if (rs.next()) {
                        hasPlayerUUID = true;
                    }
                }

                if (hasPlayerUUID) {
                    plugin.getLogger().info("Migration détectée : suppression de la colonne playerUUID...");

                    // Vérifier si la colonne hologram_id existe dans l'ancienne table
                    boolean hasHologramId = false;
                    try (ResultSet rs = connection.getMetaData().getColumns(null, null, "waystones", "hologram_id")) {
                        if (rs.next()) {
                            hasHologramId = true;
                        }
                    }

                    // Créer une table temporaire sans la colonne playerUUID
                    statement.executeUpdate(
                            "CREATE TABLE waystones_new (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    "name TEXT NOT NULL, " +
                                    "world TEXT NOT NULL, " +
                                    "x DOUBLE NOT NULL, " +
                                    "y DOUBLE NOT NULL, " +
                                    "z DOUBLE NOT NULL, " +
                                    "item_base64 TEXT NOT NULL, " +
                                    "hologram_id INTEGER NOT NULL DEFAULT 0" +
                                    ");"
                    );

                    // Copier les données (en excluant playerUUID)
                    if (hasHologramId) {
                        statement.executeUpdate(
                                "INSERT INTO waystones_new (id, name, world, x, y, z, item_base64, hologram_id) " +
                                        "SELECT id, name, world, x, y, z, item_base64, hologram_id FROM waystones;"
                        );
                    } else {
                        // Si hologram_id n'existe pas, on utilise la valeur par défaut 0
                        statement.executeUpdate(
                                "INSERT INTO waystones_new (id, name, world, x, y, z, item_base64, hologram_id) " +
                                        "SELECT id, name, world, x, y, z, item_base64, 0 FROM waystones;"
                        );
                    }

                    // Supprimer l'ancienne table
                    statement.executeUpdate("DROP TABLE waystones;");

                    // Renommer la nouvelle table
                    statement.executeUpdate("ALTER TABLE waystones_new RENAME TO waystones;");

                    plugin.getLogger().info("Migration terminée : colonne playerUUID supprimée.");
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Erreur lors de la migration de la base de données : " + e.getMessage());
                throw e;
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}