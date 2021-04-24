package com.oheers.fish.competition.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;

public class Reward {

    RewardType type;
    String action;

    public Reward(String value) {
        String[] split = value.split(":");

        if (split.length < 2) {
            Bukkit.getLogger().log(Level.WARNING, value + " is not formatted correctly. It won't be given as a reward");
            this.type = RewardType.EMPTY;
        } else {
            try {
                // getting the value, imagine split being "COMMAND", "evenmorefish", "emf shop"
                this.type = RewardType.valueOf(split[0].toUpperCase());
                // joins the action by the removed ":" joiner
                this.action = StringUtils.join(split, ":", 1, split.length);
            // the value isn't one that can be used
            } catch (IllegalArgumentException e) {
                this.type = RewardType.EMPTY;
            }

        }
    }

    Plugin plugin = Bukkit.getPluginManager().getPlugin("EvenMoreFish");

    public void run(Player player) {
        switch (type) {
            case COMMAND:
                action = action.replace("{player}", player.getName());
                // running the command
                Bukkit.getScheduler().callSyncMethod( plugin, () ->
                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), action));
                break;
            case EFFECT:
                String[] parsedEffect = action.split(",");
                // Adds a potion effect in accordance to the config.yml "EFFECT:" value
                player.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(parsedEffect[0])), Integer.parseInt(parsedEffect[2])*20, Integer.parseInt(parsedEffect[1])));
                break;
            case HEALTH:
                // checking the player doesn't have a special effect thingy on
                if (!(player.getHealth() > 20)) {
                    double newhealth = player.getHealth() + Integer.parseInt(action);
                    // checking the new health won't go above 20
                    if (newhealth > 20) {
                        player.setHealth(20);
                    } else {
                        player.setHealth(newhealth);
                    }
                }
                break;
            case HUNGER:
                player.setFoodLevel(player.getFoodLevel() + Integer.parseInt(action));
                break;
            case ITEM:
                String[] parsedItem = action.split(",");
                FishUtils.giveItems(Collections.singletonList(new ItemStack(Material.getMaterial(parsedItem[0]), Integer.parseInt(parsedItem[1]))), player);
                break;
            case MESSAGE:
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', action));
                break;
            case MONEY:
                EvenMoreFish.econ.depositPlayer(player, Integer.parseInt(action));
                break;
            default:
                Bukkit.getLogger().log(Level.SEVERE, "Error in loading a reward.");
        }
    }
}
