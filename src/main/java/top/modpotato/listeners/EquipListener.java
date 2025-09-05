package top.modpotato.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import top.modpotato.config.Config;
import top.modpotato.util.NetheriteDetector;

/**
 * Prevents equipping Netherite armor through any standard method
 * (cursor placement, shift-click, number key swap, drag, right-click, dispenser)
 */
public class EquipListener implements Listener {
    private final NetheriteDetector netheriteDetector;
    private final Config config;

    public EquipListener(NetheriteDetector netheriteDetector, Config config) {
        this.netheriteDetector = netheriteDetector;
        this.config = config;
    }

    /**
     * Handles inventory clicks: direct armor slot placement, shift-click, number key swaps.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (shouldIgnore(player)) {
            return;
        }

        // Direct placement into armor slot (cursor -> slot) OR interacting with existing armor
        if (event.getSlotType() == SlotType.ARMOR) {
            ItemStack cursor = event.getCursor();
            if (isNetheriteArmor(cursor)) {
                cancelClick(event, player, "Equipping Netherite armor is not allowed!");
                return;
            }
            // Prevent picking up / swapping a netherite armor piece once there (optional policy)
            ItemStack current = event.getCurrentItem();
            if (isNetheriteArmor(current)) {
                cancelClick(event, player, "Netherite armor cannot be interacted with!");
                return;
            }
        }

        // Shift-click auto-equip from inventory
        if (event.isShiftClick()) {
            ItemStack current = event.getCurrentItem();
            if (isNetheriteArmor(current)) {
                cancelClick(event, player, "Equipping Netherite armor is not allowed!");
                return;
            }
        }

        // Number key swap into armor slot
        if (event.getClick() == ClickType.NUMBER_KEY && event.getSlotType() == SlotType.ARMOR) {
            int hotbar = event.getHotbarButton();
            if (hotbar >= 0) {
                ItemStack hotbarItem = player.getInventory().getItem(hotbar);
                if (isNetheriteArmor(hotbarItem)) {
                    cancelClick(event, player, "Equipping Netherite armor is not allowed!");
                }
            }
        }
    }

    /**
     * Blocks dragging a Netherite armor piece that could land in an armor slot.
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (shouldIgnore(player)) {
            return;
        }

        ItemStack dragged = event.getOldCursor();
        if (isNetheriteArmor(dragged)) {
            event.setCancelled(true);
            notifyPlayer(player, "Equipping Netherite armor is not allowed!");
        }
    }

    /**
     * Blocks right-click auto-equip from hand.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        if (shouldIgnore(player)) {
            return;
        }
        ItemStack item = event.getItem();
        if (isNetheriteArmor(item)) {
            event.setCancelled(true);
            notifyPlayer(player, "Equipping Netherite armor is not allowed!");
        }
    }

    /**
     * Blocks dispenser attempts to equip Netherite armor onto players.
     */
    @EventHandler
    public void onBlockDispenseArmor(BlockDispenseArmorEvent event) {
        if (!isNetheriteArmor(event.getItem())) {
            return;
        }
        if (!(event.getTargetEntity() instanceof Player player)) {
            return;
        }
        if (shouldIgnore(player)) {
            return;
        }
        event.setCancelled(true);
        notifyPlayer(player, "Dispensed Netherite armor is blocked!");
    }

    private boolean isNetheriteArmor(ItemStack item) {
        if (item == null) return false;
        if (!netheriteDetector.isNetheriteItem(item)) return false;
        String n = item.getType().name();
        return n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE") || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS");
    }

    private boolean shouldIgnore(Player player) {
        return config.isIgnoreCreativeSpectator() &&
                (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR);
    }

    private void cancelClick(InventoryClickEvent event, Player player, String message) {
        event.setCancelled(true);
        notifyPlayer(player, message);
    }

    private void notifyPlayer(Player player, String message) {
        if (config.isNotifyPlayers()) {
            player.sendMessage(Component.text(message).color(NamedTextColor.RED));
        }
    }
}