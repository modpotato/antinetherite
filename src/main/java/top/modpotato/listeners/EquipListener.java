package top.modpotato.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EquipListener implements Listener {
    @EventHandler
    public void onEquip(InventoryClickEvent event) {
        if (event.getSlotType() != SlotType.ARMOR) {
            return;
        }

        ItemStack item = event.getCursor();
        if (item != null && item.getType().name().contains("NETHERITE")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.sendMessage(Component.text("Equipping Netherite armor is not allowed!").color(NamedTextColor.RED));
        }
    }
}