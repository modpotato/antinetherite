package top.modpotato.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CraftListener implements Listener {
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        if (result.getType().name().contains("NETHERITE")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(Component.text("Crafting Netherite items is not allowed!").color(NamedTextColor.RED));
        }
    }
}
