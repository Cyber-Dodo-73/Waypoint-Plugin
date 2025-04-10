package fr.cyberdodo.waystone.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WaystoneData {
    private int id;
    private String name;
    private Location location;
    private ItemStack bannerItem; // ex. RED_BANNER, BLUE_BANNER, etc.
    private int hologramId;       // ID de l'hologramme associé (0 si aucun)
}