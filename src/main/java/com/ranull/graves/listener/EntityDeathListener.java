package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.event.GraveBlockPlaceEvent;
import com.ranull.graves.event.GraveCreateEvent;
import com.ranull.graves.type.Grave;
import com.ranull.graves.type.Graveyard;
import com.ranull.graves.util.*;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Listener for handling entity death events and creating graves.
 */
public class EntityDeathListener implements Listener {
    private final Graves plugin;

    /**
     * Constructs an EntityDeathListener with the specified Graves plugin.
     *
     * @param plugin The Graves plugin instance.
     */
    public EntityDeathListener(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the EntityDeathEvent to create a grave based on various conditions.
     *
     * @param event The EntityDeathEvent to handle.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) throws InvocationTargetException {
        LivingEntity livingEntity = event.getEntity();
        String entityName = plugin.getEntityManager().getEntityName(livingEntity);
        Location location = LocationUtil.roundLocation(livingEntity.getLocation());
        List<String> permissionList = livingEntity instanceof Player ? plugin.getPermissionList(livingEntity) : null;
        List<String> worldList = plugin.getConfig("world", livingEntity, permissionList).getStringList("world");
        List<ItemStack> removedItemStackList = getRemovedItemStacks(livingEntity);

        if (isInvalidMohistDeath(event) || isInvalidGraveZombie(event, livingEntity, entityName)) return;

        if (livingEntity instanceof Player) {
            if (handlePlayerDeath((Player) livingEntity, entityName)) return;
        }

        if (!isEnabledGrave(livingEntity, permissionList, entityName)) return;

        if (isKeepInventory((PlayerDeathEvent) event, entityName)) return;

        if (event.getDrops().isEmpty()) {
            plugin.debugMessage("Grave not created for " + entityName + " because they had an empty inventory", 2);
            return;
        }

        if (isInvalidCreatureSpawn(livingEntity, permissionList, entityName)) return;

        if (!isValidWorld(worldList, livingEntity, entityName)) return;

        if (plugin.getGraveManager().shouldIgnoreBlock(location.getBlock(), livingEntity, permissionList)) {
            plugin.getEntityManager().sendMessage("message.ignore", livingEntity, StringUtil.format(location.getBlock().getType().name()), location, permissionList);
            return;
        }

        if (!canCreateGraveInProtectedRegion(location, livingEntity, entityName, permissionList)) return;

        if (!isValidDamageCause(livingEntity, permissionList, entityName)) return;

        if (plugin.getGraveManager().getGraveList(livingEntity).size() >= plugin.getConfig("grave.max", livingEntity, permissionList).getInt("grave.max")) {
            plugin.getEntityManager().sendMessage("message.max", livingEntity, livingEntity.getLocation(), permissionList);
            plugin.debugMessage("Grave not created for " + entityName + " because they reached maximum graves", 2);
            return;
        }

        if (!hasValidToken(livingEntity, permissionList, entityName, event.getDrops())) return;

        List<ItemStack> graveItemStackList = getGraveItemStackList(event, livingEntity, permissionList);

        if (!graveItemStackList.isEmpty()) {
            createGrave(event, livingEntity, entityName, permissionList, removedItemStackList, graveItemStackList, location);
        } else {
            plugin.debugMessage("Grave not created for " + entityName + " because they had no drops", 2);
        }
    }

    /**
     * Retrieves the list of removed item stacks for the specified entity.
     *
     * @param livingEntity The entity whose removed item stacks are to be retrieved.
     * @return The list of removed item stacks.
     */
    private List<ItemStack> getRemovedItemStacks(LivingEntity livingEntity) {
        List<ItemStack> removedItemStackList = new ArrayList<>();
        if (plugin.getCacheManager().getRemovedItemStackMap().containsKey(livingEntity.getUniqueId())) {
            removedItemStackList.addAll(plugin.getCacheManager().getRemovedItemStackMap().get(livingEntity.getUniqueId()));
            plugin.getCacheManager().getRemovedItemStackMap().remove(livingEntity.getUniqueId());
        }
        return removedItemStackList;
    }

    /**
     * Checks if the entity death event is an invalid Mohist death.
     *
     * @param event The entity death event to check.
     * @return True if the event is an invalid Mohist death, false otherwise.
     */
    private boolean isInvalidMohistDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.PLAYER && !(event instanceof PlayerDeathEvent)) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            return true;
        }
        return false;
    }

    /**
     * Checks if the entity is an invalid grave zombie.
     *
     * @param event       The entity death event.
     * @param livingEntity The entity to check.
     * @param entityName   The name of the entity.
     * @return True if the entity is an invalid grave zombie, false otherwise.
     */
    private boolean isInvalidGraveZombie(EntityDeathEvent event, LivingEntity livingEntity, String entityName) {
        if (plugin.getEntityManager().hasDataByte(livingEntity, "graveZombie")) {
            EntityType zombieGraveEntityType = plugin.getEntityManager().hasDataString(livingEntity, "graveEntityType") ? EntityType.valueOf(plugin.getEntityManager().getDataString(livingEntity, "graveEntityType")) : EntityType.PLAYER;
            List<String> zombieGravePermissionList = plugin.getEntityManager().hasDataString(livingEntity, "gravePermissionList") ? Arrays.asList(plugin.getEntityManager().getDataString(livingEntity, "gravePermissionList").split("\\|")) : null;
            if (!plugin.getConfig("zombie.drop", zombieGraveEntityType, zombieGravePermissionList).getBoolean("zombie.drop")) {
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
            return true;
        }
        return false;
    }

    /**
     * Handles player death and checks if a grave should be created.
     *
     * @param player      The player who died.
     * @param entityName  The name of the player.
     * @return True if a grave should not be created, false otherwise.
     */
    private boolean handlePlayerDeath(Player player, String entityName) throws InvocationTargetException {
        if (plugin.getGraveyardManager().isModifyingGraveyard(player)) {
            plugin.getGraveyardManager().stopModifyingGraveyard(player);
        }
        if (!player.hasPermission("graves.place") || !player.hasPermission("graves.place") && !player.isOp()) {
            plugin.debugMessage("Grave not created for " + entityName + " because they don't have permission to place graves", 2);
            return true;
        } else if (player.hasPermission("essentials.keepinv") || player.hasPermission("essentials.keepinv")) {
            plugin.debugMessage(entityName + " has essentials.keepinv", 2);
        }
        return false;
    }

    /**
     * Checks if graves are enabled for the specified entity.
     *
     * @param livingEntity The entity to check.
     * @param permissionList The list of permissions.
     * @param entityName    The name of the entity.
     * @return True if graves are enabled, false otherwise.
     */
    private boolean isEnabledGrave(LivingEntity livingEntity, List<String> permissionList, String entityName) {
        if (!plugin.getConfig("grave.enabled", livingEntity, permissionList).getBoolean("grave.enabled")) {
            if (livingEntity instanceof Player) {
                plugin.debugMessage("Grave not created for " + entityName + " because they have graves disabled", 2);
            }
            return false;
        }
        return true;
    }

    /**
     * Checks if the player has keep inventory enabled.
     *
     * @param event      The player death event.
     * @param entityName The name of the player.
     * @return True if the player has keep inventory enabled, false otherwise.
     */
    private boolean isKeepInventory(PlayerDeathEvent event, String entityName) {
        try {
            if (event.getKeepInventory()) {
                plugin.debugMessage("Grave not created for " + entityName + " because they had keep inventory", 2);
                return true;
            }
        } catch (NoSuchMethodError ignored) {
        }
        return false;
    }

    /**
     * Checks if the creature spawn reason is valid.
     *
     * @param livingEntity  The creature entity.
     * @param permissionList The list of permissions.
     * @param entityName    The name of the entity.
     * @return True if the spawn reason is invalid, false otherwise.
     */
    private boolean isInvalidCreatureSpawn(LivingEntity livingEntity, List<String> permissionList, String entityName) {
        if (livingEntity instanceof Creature) {
            List<String> spawnReasonList = plugin.getConfig("spawn.reason", livingEntity, permissionList).getStringList("spawn.reason");
            if (plugin.getEntityManager().hasDataString(livingEntity, "spawnReason") && (!spawnReasonList.contains("ALL") && !spawnReasonList.contains(plugin.getEntityManager().getDataString(livingEntity, "spawnReason")))) {
                plugin.debugMessage("Grave not created for " + entityName + " because they had an invalid spawn reason", 2);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the entity is in a valid world.
     *
     * @param worldList    The list of valid worlds.
     * @param livingEntity The entity to check.
     * @param entityName   The name of the entity.
     * @return True if the entity is in a valid world, false otherwise.
     */
    private boolean isValidWorld(List<String> worldList, LivingEntity livingEntity, String entityName) {
        if (!worldList.contains("ALL") && !worldList.contains(livingEntity.getWorld().getName())) {
            plugin.debugMessage("Grave not created for " + entityName + " because they are not in a valid world", 2);
            return false;
        }
        return true;
    }

    /**
     * Checks if a grave can be created in the specified WorldGuard region.
     *
     * @param location       The location to check.
     * @param livingEntity   The entity to check.
     * @param entityName     The name of the entity.
     * @param permissionList The list of permissions.
     * @return True if a grave can be created, false otherwise.
     */
    private boolean canCreateGraveInProtectedRegion(Location location, LivingEntity livingEntity, String entityName, List<String> permissionList) {
        if (plugin.getIntegrationManager().hasWorldGuard()) {
            boolean hasCreateGrave = plugin.getIntegrationManager().getWorldGuard().hasCreateGrave(location);
            if (hasCreateGrave) {
                if (livingEntity instanceof Player) {
                    Player player = (Player) livingEntity;
                    if (!plugin.getIntegrationManager().getWorldGuard().canCreateGrave(player, location)) {
                        plugin.getEntityManager().sendMessage("message.region-create-deny", player, location, permissionList);
                        plugin.debugMessage("Grave not created for " + entityName + " because they are in a region with graves-create set to deny", 2);
                        return false;
                    }
                } else if (!plugin.getIntegrationManager().getWorldGuard().canCreateGrave(location)) {
                    plugin.debugMessage("Grave not created for " + entityName + " because they are in a region with graves-create set to deny", 2);
                    return false;
                }
            } else if (!plugin.getLocationManager().canBuild(livingEntity, location, permissionList)) {
                plugin.getEntityManager().sendMessage("message.build-denied", livingEntity, location, permissionList);
                plugin.debugMessage("Grave not created for " + entityName + " because they don't have permission to build where they died", 2);
                return false;
            }
        } else if (!plugin.getLocationManager().canBuild(livingEntity, location, permissionList)) {
            plugin.getEntityManager().sendMessage("message.build-denied", livingEntity, location, permissionList);
            plugin.debugMessage("Grave not created for " + entityName + " because they don't have permission to build where they died", 2);
            return false;
        }

        if (plugin.getIntegrationManager().hasTowny()) {
            Player player = (livingEntity instanceof Player) ? (Player) livingEntity : null;
            if (player != null && !plugin.getIntegrationManager().getTowny().isResident(String.valueOf(location), player)) {
                plugin.getEntityManager().sendMessage("message.region-create-deny", player, location, permissionList);
                plugin.debugMessage("Grave not created for " + entityName + " because they are not a resident in the town", 2);
                return false;
            }
        }
        return true;
    }


    /**
     * Checks if the damage cause is valid for creating a grave.
     *
     * @param livingEntity  The entity that was damaged.
     * @param permissionList The list of permissions.
     * @param entityName    The name of the entity.
     * @return True if the damage cause is valid, false otherwise.
     */private boolean isValidDamageCause(LivingEntity livingEntity, List<String> permissionList, String entityName) {
        if (livingEntity.getLastDamageCause() != null) {
            EntityDamageEvent.DamageCause damageCause = livingEntity.getLastDamageCause().getCause();
            List<String> damageCauseList = plugin.getConfig("death.reason", livingEntity, permissionList).getStringList("death.reason");
            if (!damageCauseList.contains("ALL") && !damageCauseList.contains(damageCause.name()) && (damageCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK && ((livingEntity.getKiller() != null && !plugin.getConfig("death.player", livingEntity, permissionList).getBoolean("death.player")) || (livingEntity.getKiller() == null && !plugin.getConfig("death.entity", livingEntity, permissionList).getBoolean("death.entity"))) || (damageCause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && !plugin.getConfig("death.environmental", livingEntity, permissionList).getBoolean("death.environmental")))) {
                plugin.debugMessage("Grave not created for " + entityName + " because they died to an invalid damage cause", 2);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the entity has a valid grave token.
     *
     * @param livingEntity  The entity to check.
     * @param permissionList The list of permissions.
     * @param entityName    The name of the entity.
     * @param drops         The list of item drops.
     * @return True if the entity has a valid grave token, false otherwise.
     */
    private boolean hasValidToken(LivingEntity livingEntity, List<String> permissionList, String entityName, List<ItemStack> drops) {
        if (plugin.getVersionManager().hasPersistentData() && plugin.getConfig("token.enabled", livingEntity, permissionList).getBoolean("token.enabled")) {
            String name = plugin.getConfig("token.name", livingEntity).getString("token.name", "basic");
            if (plugin.getConfig().isConfigurationSection("settings.token." + name)) {
                ItemStack itemStack = plugin.getRecipeManager().getGraveTokenFromPlayer(name, drops);
                if (itemStack != null) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                } else {
                    plugin.getEntityManager().sendMessage("message.no-token", livingEntity, livingEntity.getLocation(), permissionList);
                    plugin.debugMessage("Grave not created for " + entityName + " because they did not have a grave token", 2);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Retrieves the list of item stacks for the grave.
     *
     * @param event         The entity death event.
     * @param livingEntity  The entity that died.
     * @param permissionList The list of permissions.
     * @return The list of item stacks for the grave.
     */
    private List<ItemStack> getGraveItemStackList(EntityDeathEvent event, LivingEntity livingEntity, List<String> permissionList) {
        List<ItemStack> graveItemStackList = new ArrayList<>();
        List<ItemStack> eventItemStackList = new ArrayList<>(event.getDrops());
        List<ItemStack> dropItemStackList = new ArrayList<>(eventItemStackList);
        Iterator<ItemStack> dropItemStackListIterator = dropItemStackList.iterator();
        while (dropItemStackListIterator.hasNext()) {
            ItemStack itemStack = dropItemStackListIterator.next();
            if (itemStack != null) {
                if (plugin.getEntityManager().getGraveUUIDFromItemStack(itemStack) != null) {
                    if (plugin.getConfig("compass.destroy", livingEntity, permissionList).getBoolean("compass.destroy")) {
                        dropItemStackListIterator.remove();
                        event.getDrops().remove(itemStack);
                        continue;
                    } else if (plugin.getConfig("compass.ignore", livingEntity, permissionList).getBoolean("compass.ignore")) {
                        continue;
                    }
                }
                if (!plugin.getGraveManager().shouldIgnoreItemStack(itemStack, livingEntity, permissionList)) {
                    graveItemStackList.add(itemStack);
                    dropItemStackListIterator.remove();
                }
            }
        }
        return graveItemStackList;
    }

    /**
     * Creates a grave for the specified entity.
     *
     * @param event              The entity death event.
     * @param livingEntity       The entity that died.
     * @param entityName         The name of the entity.
     * @param permissionList     The list of permissions.
     * @param removedItemStackList The list of removed item stacks.
     * @param graveItemStackList The list of item stacks for the grave.
     * @param location           The location of the grave.
     */
    private void createGrave(EntityDeathEvent event, LivingEntity livingEntity, String entityName, List<String> permissionList, List<ItemStack> removedItemStackList, List<ItemStack> graveItemStackList, Location location) {
        Grave grave = new Grave(UUID.randomUUID());
        setupGrave(grave, livingEntity, entityName, permissionList);
        setGraveExperience(grave, event, livingEntity);
        setupGraveKiller(grave, livingEntity);
        setupGraveProtection(grave);
        GraveCreateEvent graveCreateEvent = new GraveCreateEvent(livingEntity, grave);
        plugin.getServer().getPluginManager().callEvent(graveCreateEvent);
        if (!graveCreateEvent.isCancelled()) {
            placeGrave(event, grave, graveCreateEvent, graveItemStackList, removedItemStackList, location, livingEntity, permissionList);
        }
    }

    /**
     * Sets up the basic properties of the grave.
     *
     * @param grave         The grave to set up.
     * @param livingEntity  The entity that died.
     * @param entityName    The name of the entity.
     * @param permissionList The list of permissions.
     */
    private void setupGrave(Grave grave, LivingEntity livingEntity, String entityName, List<String> permissionList) {
        grave.setOwnerType(livingEntity.getType());
        grave.setOwnerName(entityName);
        grave.setOwnerNameDisplay(livingEntity instanceof Player ? ((Player) livingEntity).getDisplayName() : grave.getOwnerName());
        grave.setOwnerUUID(livingEntity.getUniqueId());
        grave.setPermissionList(permissionList);
        grave.setYaw(livingEntity.getLocation().getYaw());
        grave.setPitch(livingEntity.getLocation().getPitch());
        grave.setTimeAlive(plugin.getConfig("grave.time", grave).getInt("grave.time") * 1000L);
        if (!plugin.getVersionManager().is_v1_7()) {
            grave.setOwnerTexture(SkinUtil.getTexture(livingEntity));
            grave.setOwnerTextureSignature(SkinUtil.getSignature(livingEntity));
        }
    }

    /**
     * Sets the experience for the grave.
     *
     * @param grave        The grave to set the experience for.
     * @param event        The entity death event.
     * @param livingEntity The entity that died.
     */
    private void setGraveExperience(Grave grave, EntityDeathEvent event, LivingEntity livingEntity) {
        float experiencePercent = (float) plugin.getConfig("experience.store", grave).getDouble("experience.store");
        if (experiencePercent >= 0) {
            if (livingEntity instanceof Player) {
                Player player = (Player) livingEntity;
                if (player.hasPermission("graves.experience")) {
                    grave.setExperience(ExperienceUtil.getDropPercent(ExperienceUtil.getPlayerExperience(player), experiencePercent));
                } else {
                    grave.setExperience(event.getDroppedExp());
                }
                if (event instanceof PlayerDeathEvent) {
                    ((PlayerDeathEvent) event).setKeepLevel(false);
                }
            } else {
                grave.setExperience(ExperienceUtil.getDropPercent(event.getDroppedExp(), experiencePercent));
            }
        } else {
            grave.setExperience(event.getDroppedExp());
        }
    }

    /**
     * Sets up the killer details for the grave.
     *
     * @param grave        The grave to set up.
     * @param livingEntity The entity that died.
     */
    private void setupGraveKiller(Grave grave, LivingEntity livingEntity) {
        if (livingEntity.getKiller() != null) {
            grave.setKillerType(EntityType.PLAYER);
            grave.setKillerName(livingEntity.getKiller().getName());
            grave.setKillerNameDisplay(livingEntity.getKiller().getDisplayName());
            grave.setKillerUUID(livingEntity.getKiller().getUniqueId());
        } else if (livingEntity.getLastDamageCause() != null) {
            EntityDamageEvent entityDamageEvent = livingEntity.getLastDamageCause();
            if (entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && entityDamageEvent instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
                grave.setKillerUUID(entityDamageByEntityEvent.getDamager().getUniqueId());
                grave.setKillerType(entityDamageByEntityEvent.getDamager().getType());
                grave.setKillerName(plugin.getEntityManager().getEntityName(entityDamageByEntityEvent.getDamager()));
            } else {
                grave.setKillerUUID(null);
                grave.setKillerType(null);
                grave.setKillerName(plugin.getGraveManager().getDamageReason(entityDamageEvent.getCause(), grave));
            }
            grave.setKillerNameDisplay(grave.getKillerName());
        }
    }

    /**
     * Sets up the protection details for the grave.
     *
     * @param grave The grave to set up.
     */
    private void setupGraveProtection(Grave grave) {
        if (plugin.getConfig("protection.enabled", grave).getBoolean("protection.enabled")) {
            grave.setProtection(true);
            grave.setTimeProtection(plugin.getConfig("protection.time", grave).getInt("protection.time") * 1000L);
        }
    }

    /**
     * Places the grave at the specified location.
     *
     * @param event               The entity death event.
     * @param grave               The grave to place.
     * @param graveCreateEvent    The grave create event.
     * @param graveItemStackList  The list of item stacks for the grave.
     * @param removedItemStackList The list of removed item stacks.
     * @param location            The location to place the grave.
     * @param livingEntity        The entity that died.
     * @param permissionList      The list of permissions.
     */
    private void placeGrave(EntityDeathEvent event, Grave grave, GraveCreateEvent graveCreateEvent, List<ItemStack> graveItemStackList, List<ItemStack> removedItemStackList, Location location, LivingEntity livingEntity, List<String> permissionList) {
        Map<Location, BlockData.BlockType> locationMap = new HashMap<>();
        Location safeLocation = plugin.getLocationManager().getSafeGraveLocation(livingEntity, location, grave);
        event.getDrops().clear();
        event.getDrops().addAll(event.getDrops());
        event.setDroppedExp(0);
        grave.setLocationDeath(safeLocation != null ? safeLocation : location);
        grave.getLocationDeath().setYaw(grave.getYaw());
        grave.getLocationDeath().setPitch(grave.getPitch());

        if (plugin.getConfig("graveyard.enabled", grave).getBoolean("graveyard.enabled")) {
            Graveyard graveyard = plugin.getGraveyardManager().getClosestGraveyard(grave.getLocationDeath(), livingEntity);
            if (graveyard != null) {
                Map<Location, BlockFace> graveyardFreeSpaces = plugin.getGraveyardManager().getGraveyardFreeSpaces(graveyard);
                if (!graveyardFreeSpaces.isEmpty()) {
                    for (Map.Entry<Location, BlockFace> entry : graveyardFreeSpaces.entrySet()) {
                        Location graveyardLocation = entry.getKey();
                        if (!plugin.getDataManager().hasGraveAtLocation(graveyardLocation)) {
                            graveyardLocation.setYaw(plugin.getConfig().getBoolean("settings.graveyard.facing") ? BlockFaceUtil.getBlockFaceYaw(entry.getValue()) : grave.getYaw());
                            graveyardLocation.setPitch(grave.getPitch());
                            locationMap.put(graveyardLocation, BlockData.BlockType.GRAVEYARD);
                            break;
                        }
                    }
                }
            }
        }

        if (locationMap.isEmpty()) {
            locationMap.put(grave.getLocationDeath(), BlockData.BlockType.DEATH);
        }

        setupObituary(grave, graveItemStackList);
        setupSkull(grave, graveItemStackList);
        grave.setInventory(plugin.getGraveManager().getGraveInventory(grave, livingEntity, graveItemStackList, removedItemStackList, permissionList));
        grave.setEquipmentMap(!plugin.getVersionManager().is_v1_7() ? plugin.getEntityManager().getEquipmentMap(livingEntity, grave) : new HashMap<>());

        if (!locationMap.isEmpty()) {
            notifyGraveCreation(event, grave, locationMap, livingEntity, permissionList);
        } else {
            handleFailedGravePlacement(event, grave, location, livingEntity);
        }
    }

    /**
     * Sets up the obituary item for the grave.
     *
     * @param grave               The grave to set up.
     * @param graveItemStackList  The list of item stacks for the grave.
     */
    private void setupObituary(Grave grave, List<ItemStack> graveItemStackList) {
        if (plugin.getConfig("obituary.enabled", grave).getBoolean("obituary.enabled")) {
            graveItemStackList.add(plugin.getItemStackManager().getGraveObituary(grave));
        }
    }

    /**
     * Sets up the skull item for the grave.
     *
     * @param grave               The grave to set up.
     * @param graveItemStackList  The list of item stacks for the grave.
     */
    private void setupSkull(Grave grave, List<ItemStack> graveItemStackList) {
        if (plugin.getConfig("head.enabled", grave).getBoolean("head.enabled") && Math.random() < plugin.getConfig("head.percent", grave).getDouble("head.percent") && grave.getOwnerTexture() != null && grave.getOwnerTextureSignature() != null) {
            graveItemStackList.add(plugin.getItemStackManager().getGraveHead(grave));
        }
    }

    /**
     * Notifies the creation of the grave and places the grave blocks.
     *
     * @param event              The entity death event.
     * @param grave              The grave that was created.
     * @param locationMap        The map of locations for the grave.
     * @param livingEntity       The entity that died.
     * @param permissionList     The list of permissions.
     */
    private void notifyGraveCreation(EntityDeathEvent event, Grave grave, Map<Location, BlockData.BlockType> locationMap, LivingEntity livingEntity, List<String> permissionList) {
        plugin.getEntityManager().sendMessage("message.death", livingEntity, grave.getLocationDeath(), grave);
        plugin.getEntityManager().runCommands("event.command.create", livingEntity, grave.getLocationDeath(), grave);
        plugin.getDataManager().addGrave(grave);
        if (plugin.getIntegrationManager().hasMultiPaper()) {
            plugin.getIntegrationManager().getMultiPaper().notifyGraveCreation(grave);
        }
        placeGraveBlocks(event, grave, locationMap, livingEntity);
    }

    /**
     * Places the grave blocks at the specified locations.
     *
     * @param event              The entity death event.
     * @param grave              The grave to place.
     * @param locationMap        The map of locations for the grave.
     * @param livingEntity       The entity that died.
     */
    private void placeGraveBlocks(EntityDeathEvent event, Grave grave, Map<Location, BlockData.BlockType> locationMap, LivingEntity livingEntity) {
        for (Map.Entry<Location, BlockData.BlockType> entry : locationMap.entrySet()) {
            Location location = entry.getKey().clone();
            int offsetX = 0;
            int offsetY = 0;
            int offsetZ = 0;
            switch (entry.getValue()) {
                case DEATH:
                    break;
                case NORMAL:
                    offsetX = plugin.getConfig("placement.offset.x", grave).getInt("placement.offset.x");
                    offsetY = plugin.getConfig("placement.offset.y", grave).getInt("placement.offset.y");
                    offsetZ = plugin.getConfig("placement.offset.z", grave).getInt("placement.offset.z");
                    break;
                case GRAVEYARD:
                    offsetX = plugin.getConfig().getInt("settings.graveyard.offset.x");
                    offsetY = plugin.getConfig().getInt("settings.graveyard.offset.y");
                    offsetZ = plugin.getConfig().getInt("settings.graveyard.offset.z");
                    break;
            }
            location.add(offsetX, offsetY, offsetZ);
            GraveBlockPlaceEvent graveBlockPlaceEvent = new GraveBlockPlaceEvent(grave, location, entry.getValue());
            plugin.getServer().getPluginManager().callEvent(graveBlockPlaceEvent);
            if (!graveBlockPlaceEvent.isCancelled()) {
                plugin.getGraveManager().placeGrave(graveBlockPlaceEvent.getLocation(), grave);
                plugin.getEntityManager().sendMessage("message.block", livingEntity, location, grave);
                plugin.getEntityManager().runCommands("event.command.block", livingEntity, graveBlockPlaceEvent.getLocation(), grave);
            }
        }
    }

    /**
     * Handles failed grave placement.
     *
     * @param event         The entity death event.
     * @param grave         The grave that failed to be placed.
     * @param location      The location where the grave was to be placed.
     * @param livingEntity  The entity that died.
     */
    private void handleFailedGravePlacement(EntityDeathEvent event, Grave grave, Location location, LivingEntity livingEntity) {
        if (event instanceof PlayerDeathEvent && plugin.getConfig("placement.failure-keep-inventory", grave).getBoolean("placement.failure-keep-inventory")) {
            PlayerDeathEvent playerDeathEvent = (PlayerDeathEvent) event;
            try {
                playerDeathEvent.setKeepLevel(true);
                playerDeathEvent.setKeepInventory(true);
                plugin.getEntityManager().sendMessage("message.failure-keep-inventory", livingEntity, location, grave);
            } catch (NoSuchMethodError ignored) {
            }
        } else {
            event.getDrops().addAll(event.getDrops());
            event.setDroppedExp(event.getDroppedExp());
            plugin.getEntityManager().sendMessage("message.failure", livingEntity, location, grave);
        }
    }

    private Location findGraveLocation(Grave grave, LivingEntity livingEntity, Location location) {
        if (plugin.getConfig("graveyard.enabled", grave).getBoolean("graveyard.enabled")) {
            Graveyard graveyard = plugin.getGraveyardManager().getClosestGraveyard(location, livingEntity);
            if (graveyard != null) {
                Map<Location, BlockFace> graveyardFreeSpaces = plugin.getGraveyardManager().getGraveyardFreeSpaces(graveyard);
                for (Map.Entry<Location, BlockFace> entry : graveyardFreeSpaces.entrySet()) {
                    Location graveyardLocation = entry.getKey();
                    if (!plugin.getDataManager().hasGraveAtLocation(graveyardLocation)) {
                        graveyardLocation.setYaw(plugin.getConfig().getBoolean("settings.graveyard.facing") ? BlockFaceUtil.getBlockFaceYaw(entry.getValue()) : livingEntity.getLocation().getYaw());
                        graveyardLocation.setPitch(livingEntity.getLocation().getPitch());
                        return graveyardLocation;
                    }
                }
            }
        }
        return plugin.getLocationManager().getSafeGraveLocation(livingEntity, location, grave);
    }
}