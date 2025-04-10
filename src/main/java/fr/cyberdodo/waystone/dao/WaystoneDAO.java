package fr.cyberdodo.waystone.dao;

import fr.cyberdodo.waystone.WaystonePlugin;
import fr.cyberdodo.waystone.data.WaystoneData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class WaystoneDAO {

    /**
     * Insère une Waystone avec l'ItemStack complet sérialisé en Base64 et l'ID de l'hologramme.
     * L'ID hologramme est passé en paramètre (0 si aucun hologramme n'est associé).
     */
    public static void insertWaystone(String name, ItemStack itemStack, Location loc, int hologramId) {
        String sql = "INSERT INTO waystones(name, world, x, y, z, item_base64, hologram_id) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = WaystonePlugin.getInstance().getDatabase().getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, loc.getWorld().getName());
            ps.setDouble(3, loc.getX());
            ps.setDouble(4, loc.getY());
            ps.setDouble(5, loc.getZ());

            // Sérialisation de l'ItemStack
            String base64 = itemStackToBase64(itemStack);
            ps.setString(6, base64);
            ps.setInt(7, hologramId);

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère une Waystone par son ID.
     */
    public static WaystoneData getWaystoneById(int id) {
        String sql = "SELECT * FROM waystones WHERE id = ?";
        try (PreparedStatement ps = WaystonePlugin.getInstance().getDatabase().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String world = rs.getString("world");
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");
                    String base64 = rs.getString("item_base64");
                    int hologramId = rs.getInt("hologram_id");
                    Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                    ItemStack item = itemStackFromBase64(base64);
                    return new WaystoneData(id, name, loc, item, hologramId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Récupère toutes les Waystones depuis la DB.
     * On charge l'ItemStack complet (bannerItem) depuis le champ item_base64.
     */
    public static List<WaystoneData> getAllWaystones() {
        List<WaystoneData> list = new ArrayList<>();
        String sql = "SELECT * FROM waystones";

        try (PreparedStatement ps = WaystonePlugin.getInstance().getDatabase().getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String world = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                String base64 = rs.getString("item_base64");
                int hologramId = rs.getInt("hologram_id");

                Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                ItemStack item = itemStackFromBase64(base64);

                // Création de l'objet WaystoneData avec le hologramId.
                WaystoneData data = new WaystoneData(id, name, loc, item, hologramId);
                list.add(data);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Récupère une Waystone par coordonnées (si elle existe).
     */
    public static WaystoneData getWaystoneByLocation(Location location) {
        String sql = "SELECT * FROM waystones WHERE world = ? AND x = ? AND y = ? AND z = ?";
        try (PreparedStatement ps = WaystonePlugin.getInstance().getDatabase().getConnection().prepareStatement(sql)) {
            ps.setString(1, location.getWorld().getName());
            ps.setDouble(2, location.getX());
            ps.setDouble(3, location.getY());
            ps.setDouble(4, location.getZ());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String base64 = rs.getString("item_base64");
                    int hologramId = rs.getInt("hologram_id");

                    ItemStack item = itemStackFromBase64(base64);
                    return new WaystoneData(id, name, location, item, hologramId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Met à jour le nom de la Waystone (le champ 'name') en DB.
     */
    public static void updateWaystoneName(int id, String newName) {
        String sql = "UPDATE waystones SET name = ? WHERE id = ?";
        try (PreparedStatement ps = WaystonePlugin.getInstance().getDatabase().getConnection().prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Met à jour l'ID de l'hologramme (champ 'hologram_id') pour une Waystone.
     */
    public static void updateWaystoneHologramId(int id, int hologramId) {
        String sql = "UPDATE waystones SET hologram_id = ? WHERE id = ?";
        try (PreparedStatement ps = WaystonePlugin.getInstance().getDatabase().getConnection().prepareStatement(sql)) {
            ps.setInt(1, hologramId);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Supprime une Waystone par son ID.
     */
    public static void deleteWaystone(int id) {
        String sql = "DELETE FROM waystones WHERE id = ?";
        try (PreparedStatement ps = WaystonePlugin.getInstance().getDatabase().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convertit un ItemStack -> String Base64.
     */
    private static String itemStackToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(item);
            dataOutput.close();

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convertit une String Base64 -> ItemStack.
     */
    private static ItemStack itemStackFromBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}