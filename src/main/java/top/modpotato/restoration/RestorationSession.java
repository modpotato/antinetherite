package top.modpotato.restoration;

import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents an active restoration session for Ancient Debris
 */
public class RestorationSession {
    private final UUID sessionId;
    private final CommandSender initiator;
    private final UUID initiatorUUID;
    private final World worldFilter; // null for all worlds
    private final int totalLocations;
    private final int scheduledChunks;
    private final AtomicInteger completedLocations;
    private final long startTime;
    private final Set<UUID> subscribers; // UUIDs of players to notify
    private volatile boolean completed;
    
    /**
     * Creates a new restoration session
     * @param initiator The command sender who initiated the restoration
     * @param worldFilter The world to restore in, or null for all worlds
     * @param totalLocations The total number of locations to restore
     * @param scheduledChunks The number of chunks scheduled for restoration
     */
    public RestorationSession(CommandSender initiator, World worldFilter, int totalLocations, int scheduledChunks) {
        this.sessionId = UUID.randomUUID();
        this.initiator = initiator;
        this.initiatorUUID = getUUIDFromSender(initiator);
        this.worldFilter = worldFilter;
        this.totalLocations = totalLocations;
        this.scheduledChunks = scheduledChunks;
        this.completedLocations = new AtomicInteger(0);
        this.startTime = System.currentTimeMillis();
        this.subscribers = ConcurrentHashMap.newKeySet();
        this.completed = false;
        
        // Add initiator to subscribers if they have a UUID (i.e., a player)
        if (initiatorUUID != null) {
            subscribers.add(initiatorUUID);
        }
    }
    
    /**
     * Gets the UUID from a command sender if it's a player
     * @param sender The command sender
     * @return The UUID, or null if not a player
     */
    private UUID getUUIDFromSender(CommandSender sender) {
        if (sender instanceof org.bukkit.entity.Player) {
            return ((org.bukkit.entity.Player) sender).getUniqueId();
        }
        return null;
    }
    
    /**
     * Increments the completed locations counter
     * @return The new count
     */
    public int incrementCompleted() {
        return completedLocations.incrementAndGet();
    }
    
    /**
     * Gets the current completion percentage (0-100)
     * @return The completion percentage
     */
    public double getCompletionPercentage() {
        if (totalLocations == 0) {
            return 100.0;
        }
        return (completedLocations.get() * 100.0) / totalLocations;
    }
    
    /**
     * Gets the elapsed time in milliseconds
     * @return The elapsed time
     */
    public long getElapsedTimeMs() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Marks the session as completed
     */
    public void markCompleted() {
        this.completed = true;
    }
    
    // Getters
    public UUID getSessionId() {
        return sessionId;
    }
    
    public CommandSender getInitiator() {
        return initiator;
    }
    
    public UUID getInitiatorUUID() {
        return initiatorUUID;
    }
    
    public World getWorldFilter() {
        return worldFilter;
    }
    
    public int getTotalLocations() {
        return totalLocations;
    }
    
    public int getScheduledChunks() {
        return scheduledChunks;
    }
    
    public int getCompletedLocations() {
        return completedLocations.get();
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public Set<UUID> getSubscribers() {
        return subscribers;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    /**
     * Adds a subscriber to receive progress updates
     * @param playerUUID The player UUID to add
     */
    public void addSubscriber(UUID playerUUID) {
        subscribers.add(playerUUID);
    }
    
    /**
     * Removes a subscriber from receiving progress updates
     * @param playerUUID The player UUID to remove
     */
    public void removeSubscriber(UUID playerUUID) {
        subscribers.remove(playerUUID);
    }
}
