package fr.cyberdodo.waystone.config;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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