package com.oheers.fish;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.logging.Level;

public class FishUtils {

    /* checks for the "emf-fish-length" nbt tag, to determine if this itemstack is a fish or not.
     * we only need to check for the length since they're all added in a batch if it's an EMF fish */
    public static boolean isFish(ItemStack i) {
        NamespacedKey nbtlength = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-length");

        if (i != null) {
            if (i.hasItemMeta()) {
                return i.getItemMeta().getPersistentDataContainer().has(nbtlength, PersistentDataType.FLOAT);
            }
        }

        return false;
    }

    public static Fish getFish(ItemStack i) {
        NamespacedKey nbtrarity = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-rarity");
        NamespacedKey nbtname = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-name");
        NamespacedKey nbtlength = new NamespacedKey(Bukkit.getPluginManager().getPlugin("EvenMoreFish"), "emf-fish-length");

        // all appropriate null checks can be safely assumed to have passed to get to a point where we're running this method.
        PersistentDataContainer container = i.getItemMeta().getPersistentDataContainer();
        String nameString = container.get(nbtname, PersistentDataType.STRING);
        String rarityString = container.get(nbtrarity, PersistentDataType.STRING);
        Float lengthFloat = container.get(nbtlength, PersistentDataType.FLOAT);

        // Generating an empty rarity
        Rarity rarity = new Rarity(null, null, 0, false);
        // Hunting through the fish collection and creating a rarity that matches the fish's nbt
        for (Rarity r : EvenMoreFish.fishCollection.keySet()) {
            if (r.getValue().equals(rarityString)) {
                rarity = new Rarity(r.getValue(), r.getColour(), r.getWeight(), r.getAnnounce());
            }
        }

        // setting the correct length so it's an exact replica.
        Fish fish = new Fish(rarity, nameString);
        fish.setLength(lengthFloat);

        return fish;
    }

    public static void giveItems(List<ItemStack> items, Player player) {
        int slots = 0;

        for (ItemStack is : player.getInventory().getStorageContents()) {
            if (is == null) {
                slots++;
            }
        }

        for (ItemStack item : items) {
            if (slots > 0) {
                player.getInventory().addItem(item);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.5f);
            } else {
                player.getLocation().getWorld().dropItem(player.getLocation(), item);
            }
        }
    }

    public static boolean checkRegion(Location l) {
        // if there's any region plugin installed
        if (EvenMoreFish.guardPL != null) {
            // if the user has defined a region whitelist
            if (EvenMoreFish.mainConfig.regionWhitelist()) {

                // Gets a list of user defined regions
                List<String> whitelistedRegions = EvenMoreFish.mainConfig.getAllowedRegions();

                if (EvenMoreFish.guardPL.equals("worldguard")) {

                    // Creates a query for whether the player is stood in a protectedregion defined by the user
                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionQuery query = container.createQuery();
                    ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(l));

                    // runs the query
                    for (ProtectedRegion pr : set) {
                        if (whitelistedRegions.contains(pr.getId())) return true;
                    }
                    return false;
                } else if (EvenMoreFish.guardPL.equals("redprotect")) {
                    Region r = RedProtect.get().getAPI().getRegion(l);
                    // if the hook is in any redprotect region
                    if (r != null) {
                        // if the hook is in a whitelisted region
                        return whitelistedRegions.contains(r.getName());
                    }
                    return false;
                } else {
                    // the user has defined a region whitelist but doesn't have a region plugin.
                    Bukkit.getLogger().log(Level.WARNING, "Please install WorldGuard or RedProtect to enable region-specific fishing.");
                    return true;
                }
            }
            return true;
        }
        return true;
    }

}
