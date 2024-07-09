package top.modpotato;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import top.modpotato.listeners.CraftListener;


public class Main extends JavaPlugin {
    private int delay;
    private int multiplier;
    private boolean clear_netherite = false;
    private boolean cancel_craft = false;

    private BukkitRunnable removeNetheriteTask;
    private CraftListener craftListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        delay = getConfig().getInt("anti-netherite.delay", 1);
        multiplier = getConfig().getInt("anti-netherite.multiplier", 20);
        clear_netherite = getConfig().getBoolean("anti-netherite.clear", true);
        cancel_craft = getConfig().getBoolean("anti-netherite.cancel-craft", true);

        if (clear_netherite) {
            scheduleRemoveNetherite();
        }
        
        if (cancel_craft) {
            craftListener = new CraftListener();
            getServer().getPluginManager().registerEvents(craftListener, this);
        }
        
    }

    @Override
    public void onDisable() {
        if (removeNetheriteTask != null) {
            removeNetheriteTask.cancel();
        }
        
        if (craftListener != null) {
            HandlerList.unregisterAll(craftListener);
        }
    }

    private void scheduleRemoveNetherite() {
        removeNetheriteTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType().name().contains("NETHERITE")) {
                            player.getInventory().remove(item);
                        }
                    }
                }
            }
        };
        removeNetheriteTask.runTaskTimer(this, 0L, delay*multiplier);
    }
}