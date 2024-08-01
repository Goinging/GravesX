package com.ranull.graves.event;

import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The base class for all grave-related events.
 * <p>
 * This class provides common properties for grave events, such as the grave itself,
 * the location of the event, the entity involved, and additional information like
 * inventory views and blocks. This class is cancellable, allowing event listeners
 * to prevent the event from proceeding.
 * </p>
 */
public abstract class GraveEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Grave grave;
    private final Entity entity;
    private Location location;
    private final InventoryView inventoryView;
    private final LivingEntity livingEntity;
    private final LivingEntity targetEntity;
    private final BlockData.BlockType blockType;
    private final Block block;
    private final Player player;
    private boolean isCancelled;
    private boolean dropItems;

    /**
     * Constructs a new {@code GraveEvent}.
     *
     * @param grave           The grave associated with the event.
     * @param entity          The entity involved in the event, if any.
     * @param location        The location of the event.
     * @param inventoryView   The inventory view associated with the event, if any.
     * @param livingEntity    The living entity associated with the event, if any.
     * @param blockType       The type of block involved in the event, if any.
     * @param block           The block involved in the event, if any.
     * @param targetEntity    The entity targeted by the event, if any.
     * @param player          The player involved in the event, if any.
     */
    public GraveEvent(Grave grave, @Nullable Entity entity, @Nullable Location location, @Nullable InventoryView inventoryView, @Nullable LivingEntity livingEntity, @Nullable BlockData.BlockType blockType, @Nullable Block block, @Nullable LivingEntity targetEntity, @Nullable Player player) {
        this.grave = grave;
        this.entity = entity;
        this.location = location;
        this.inventoryView = inventoryView;
        this.livingEntity = livingEntity;
        this.blockType = blockType;
        this.block = block;
        this.targetEntity = targetEntity;
        this.player = player;
        this.isCancelled = false;
        this.dropItems = true;
    }

    /**
     * Gets the grave associated with the event.
     *
     * @return The grave associated with the event.
     */
    public Grave getGrave() {
        return grave;
    }

    /**
     * Gets the entity involved in the event.
     *
     * @return The entity involved in the event, or null if not applicable.
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the entity targeted by the event.
     *
     * @return The target entity, or null if not applicable.
     */
    public LivingEntity getTargetEntity() {
        return targetEntity;
    }

    /**
     * Gets the type of the target entity.
     *
     * @return The type of the target entity, or null if not applicable.
     */
    public EntityType getEntityType() {
        return targetEntity != null ? targetEntity.getType() : null;
    }

    /**
     * Gets the location of the event.
     *
     * @return The location of the event.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location of the event.
     *
     * @param location The new location of the event.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Gets the inventory view associated with the event.
     *
     * @return The inventory view, or null if not applicable.
     */
    @Nullable
    public InventoryView getInventoryView() {
        return inventoryView;
    }

    /**
     * Gets the living entity associated with the event.
     *
     * @return The living entity, or null if not applicable.
     */
    @Nullable
    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    /**
     * Gets the type of block involved in the event.
     *
     * @return The block type, or null if not applicable.
     */
    @Nullable
    public BlockData.BlockType getBlockType() {
        return blockType;
    }

    /**
     * Gets the block involved in the event.
     *
     * @return The block involved in the event, or null if not applicable.
     */
    @Nullable
    public Block getBlock() {
        return block;
    }

    /**
     * Gets the experience points associated with the grave.
     *
     * @return The experience points.
     */
    public int getBlockExp() {
        return grave.getExperience();
    }

    /**
     * Checks whether items should drop upon breaking the grave block.
     *
     * @return True if items should drop, false otherwise.
     */
    public boolean isDropItems() {
        return this.dropItems;
    }

    /**
     * Sets whether items should drop upon breaking the grave block.
     *
     * @param dropItems True if items should drop, false otherwise.
     */
    public void setDropItems(boolean dropItems) {
        this.dropItems = dropItems;
    }

    /**
     * Gets the player involved in the event.
     *
     * @return The player involved in the event, or null if not applicable.
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Checks whether the event is cancelled.
     *
     * @return True if the event is cancelled, false otherwise.
     */
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Sets whether the event is cancelled.
     *
     * @param cancel True to cancel the event, false otherwise.
     */
    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    /**
     * Gets the list of handlers for this event.
     *
     * @return The handler list for this event.
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the static list of handlers for this event.
     *
     * @return The static handler list for this event.
     */
    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }
}