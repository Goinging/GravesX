package com.ranull.graves.manager;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import com.ranull.graves.Graves;
import com.ranull.graves.integration.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * The IntegrationManager class manages the integration of various plugins with the Graves plugin.
 */
public final class IntegrationManager {
    private final Graves plugin;
    private MultiPaper multiPaper;
    private Vault vault;
    private ProtocolLib protocolLib;
    private WorldEdit worldEdit;
    private WorldGuard worldGuard;
    private Towny towny;
    private GriefDefender griefDefender;
    private FurnitureLib furnitureLib;
    private FurnitureEngine furnitureEngine;
    private ProtectionLib protectionLib;
    private ItemsAdder itemsAdder;
    private Oraxen oraxen;
    private ChestSort chestSort;
    private MiniMessage miniMessage;
    private MineDown mineDown;
    private ItemBridge itemBridge;
    private PlayerNPC playerNPC;
    private CitizensNPC citizensNPC;
    private PlaceholderAPI placeholderAPI;
    private SkriptImpl skriptImpl;

    /**
     * Initializes a new instance of the IntegrationManager class.
     *
     * @param plugin The plugin instance.
     */
    public IntegrationManager(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads the integrations by unloading and then loading them.
     */
    public void reload() {
        unload();
        load();
    }

    /**
     * Loads all integrations.
     */
    public void load() {
        loadMultiPaper();
        loadVault();
        loadProtocolLib();
        loadWorldEdit();
        loadWorldGuard();
        loadTowny();
        //loadGriefDefender(); // TODO
        loadFurnitureLib();
        loadFurnitureEngine();
        loadProtectionLib();
        loadItemsAdder();
        loadOraxen();
        loadMiniMessage();
        loadMineDown();
        loadChestSort();
        loadPlayerNPC();
        loadCitizensNPC();
        loadItemBridge();
        loadPlaceholderAPI();
        loadSkript();
        loadCompatibilityWarnings();
    }

    /**
     * Unloads all integrations.
     */
    public void unload() {
        if (furnitureLib != null) {
            furnitureLib.unregisterListeners();
        }

        if (furnitureEngine != null) {
            furnitureEngine.unregisterListeners();
        }

        if (oraxen != null) {
            oraxen.unregisterListeners();
        }

        if (placeholderAPI != null) {
            placeholderAPI.unregister();
        }

        if (playerNPC != null) {
            playerNPC.unregisterListeners();
        }

        if (citizensNPC != null) {
            citizensNPC.unregisterListeners();
        }

        if (towny != null) {
            towny.unregisterListeners();
        }

        if (skriptImpl != null) {
            skriptImpl = null;
        }
    }

    public MultiPaper getMultiPaper() {
        return multiPaper;
    }

    public Vault getVault() {
        return vault;
    }

    public ProtocolLib getProtocolLib() {
        return protocolLib;
    }

    public WorldEdit getWorldEdit() {
        return worldEdit;
    }

    public WorldGuard getWorldGuard() {
        return worldGuard;
    }

    public Towny getTowny() {
        return towny;
    }

    public GriefDefender getGriefDefender() {
        return griefDefender;
    }

    public FurnitureLib getFurnitureLib() {
        return furnitureLib;
    }

    public FurnitureEngine getFurnitureEngine() {
        return furnitureEngine;
    }

    public ProtectionLib getProtectionLib() {
        return protectionLib;
    }

    public ItemsAdder getItemsAdder() {
        return itemsAdder;
    }

    public Oraxen getOraxen() {
        return oraxen;
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }

    public MineDown getMineDown() {
        return mineDown;
    }

    public ChestSort getChestSort() {
        return chestSort;
    }

    public PlayerNPC getPlayerNPC() {
        return playerNPC;
    }

    public SkriptAddon getSkript() {
        return skriptImpl.getSkriptAddon();
    }

    public CitizensNPC getCitizensNPC() {
        return citizensNPC;
    }

    public boolean hasMultiPaper() {
        return multiPaper != null;
    }

    public boolean hasVault() {
        return vault != null;
    }

    public boolean hasProtocolLib() {
        return protocolLib != null;
    }

    public boolean hasWorldEdit() {
        return worldEdit != null;
    }

    public boolean hasWorldGuard() {
        return worldGuard != null;
    }

    public boolean hasTowny() {
        return towny != null;
    }

    public boolean hasGriefDefender() {
        return griefDefender != null;
    }

    public boolean hasFurnitureLib() {
        return furnitureLib != null;
    }

    public boolean hasFurnitureEngine() {
        return furnitureEngine != null;
    }

    public boolean hasProtectionLib() {
        return protectionLib != null;
    }

    public boolean hasItemsAdder() {
        return itemsAdder != null;
    }

    public boolean hasOraxen() {
        return oraxen != null;
    }

    public boolean hasMiniMessage() {
        return miniMessage != null;
    }

    public boolean hasMineDown() {
        return mineDown != null;
    }

    public boolean hasChestSort() {
        return chestSort != null;
    }

    public boolean hasPlayerNPC() {
        return playerNPC != null;
    }

    public boolean hasCitizensNPC() {
        return citizensNPC != null;
    }

    public boolean hasPlaceholderAPI() {
        return placeholderAPI != null;
    }

    public boolean hasSkript() {
        return skriptImpl != null;
    }

    private void loadMultiPaper() {
        if (plugin.getConfig().getBoolean("settings.integration.multipaper.enabled", true)) {
            try {
                Class.forName("puregero.multipaper.MultiPaper", false, getClass().getClassLoader());

                multiPaper = new MultiPaper(plugin);

                plugin.infoMessage("MultiPaper detected, enabling MultiLib.");
            } catch (ClassNotFoundException ignored) {
            }
        } else {
            multiPaper = null;
        }
    }

    private void loadVault() {
        if (plugin.getConfig().getBoolean("settings.integration.vault.enabled", true)) {
            Plugin vaultPlugin = plugin.getServer().getPluginManager().getPlugin("Vault");

            if (vaultPlugin != null && vaultPlugin.isEnabled()) {
                RegisteredServiceProvider<Economy> registeredServiceProvider = plugin.getServer().getServicesManager()
                        .getRegistration(Economy.class);

                if (registeredServiceProvider != null) {
                    vault = new Vault(registeredServiceProvider.getProvider());

                    plugin.integrationMessage("Hooked into " + vaultPlugin.getName() + " "
                            + vaultPlugin.getDescription().getVersion() + ".");
                }
            }
        } else {
            vault = null;
        }
    }

    private void loadProtocolLib() {
        if (plugin.getConfig().getBoolean("settings.integration.protocollib.enabled", true)) {
            Plugin protocolLibPlugin = plugin.getServer().getPluginManager().getPlugin("ProtocolLib");

            if (protocolLibPlugin != null && protocolLibPlugin.isEnabled()) {
                protocolLib = new ProtocolLib(plugin);

                plugin.integrationMessage("Hooked into " + protocolLibPlugin.getName() + " "
                        + protocolLibPlugin.getDescription().getVersion() + ".");
            }
        } else {
            protocolLib = null;
        }
    }

    public void loadWorldGuard() {
        if (plugin.getConfig().getBoolean("settings.integration.worldguard.enabled", true)) {
            Plugin worldGuardPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");

            if (worldGuardPlugin != null) {
                try {
                    Class.forName("com.sk89q.worldguard.WorldGuard", false, getClass().getClassLoader());
                    Class.forName("com.sk89q.worldguard.protection.flags.registry.FlagConflictException",
                            false, getClass().getClassLoader());

                    worldGuard = new WorldGuard(plugin);

                    plugin.integrationMessage("Hooked into " + worldGuardPlugin.getName() + " "
                            + worldGuardPlugin.getDescription().getVersion() + ".");
                } catch (ClassNotFoundException ignored) {
                    plugin.integrationMessage(worldGuardPlugin.getName() + " "
                            + worldGuardPlugin.getDescription().getVersion()
                            + " detected, Only WorldGuard 6.2+ is supported. Disabling WorldGuard support.");
                }
            }
        } else {
            worldGuard = null;
        }
    }

    public void loadTowny() {
        if (plugin.getConfig().getBoolean("settings.integration.towny.enabled", true)) {
            Plugin townyPlugin = plugin.getServer().getPluginManager().getPlugin("Towny");

            if (townyPlugin != null) {
                towny = new Towny(plugin, townyPlugin);

                plugin.integrationMessage("Hooked into " + townyPlugin.getName() + " "
                        + townyPlugin.getDescription().getVersion() + ".");
            }
        } else {
            towny = null;
        }
    }

    private void loadWorldEdit() {
        if (plugin.getConfig().getBoolean("settings.integration.worldedit.enabled", true)) {
            Plugin worldEditPlugin = plugin.getServer().getPluginManager().getPlugin("WorldEdit");

            if (worldEditPlugin != null && worldEditPlugin.isEnabled()) {
                try {
                    Class.forName("com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats", false, getClass()
                            .getClassLoader());

                    worldEdit = new WorldEdit(plugin, worldEditPlugin);

                    plugin.integrationMessage("Hooked into " + worldEditPlugin.getName() + " "
                            + worldEditPlugin.getDescription().getVersion() + ".");
                } catch (ClassNotFoundException ignored) {
                    plugin.integrationMessage(worldEditPlugin.getName() + " "
                            + worldEditPlugin.getDescription().getVersion()
                            + " detected, Only WorldEdit 7+ is supported.Disabling WorldEdit support.");
                }
            }
        } else {
            worldEdit = null;
        }
    }

    private void loadGriefDefender() {
        if (plugin.getConfig().getBoolean("settings.integration.griefdefender.enabled", true)) {
            Plugin griefDefenderPlugin = plugin.getServer().getPluginManager().getPlugin("GriefDefender");

            if (griefDefenderPlugin != null && griefDefenderPlugin.isEnabled()) {
                griefDefender = new GriefDefender();

                //griefDefender.registerFlag();

                plugin.integrationMessage("Hooked into " + griefDefenderPlugin.getName() + " "
                        + griefDefenderPlugin.getDescription().getVersion() + ".");
            }
        } else {
            griefDefender = null;
        }
    }

    private void loadFurnitureLib() {
        if (plugin.getConfig().getBoolean("settings.integration.furniturelib.enabled", true)) {
            Plugin furnitureLibPlugin = plugin.getServer().getPluginManager().getPlugin("FurnitureLib");

            if (furnitureLibPlugin != null && furnitureLibPlugin.isEnabled()) {
                furnitureLib = new FurnitureLib(plugin);

                plugin.integrationMessage("Hooked into " + furnitureLibPlugin.getName() + " "
                        + furnitureLibPlugin.getDescription().getVersion() + ".");
            }
        } else {
            furnitureLib = null;
        }
    }

    private void loadFurnitureEngine() {
        if (plugin.getConfig().getBoolean("settings.integration.furnitureengine.enabled", true)) {
            Plugin furnitureEnginePlugin = plugin.getServer().getPluginManager().getPlugin("FurnitureEngine");

            if (furnitureEnginePlugin != null && furnitureEnginePlugin.isEnabled()) {
                try {
                    Class.forName("com.mira.furnitureengine.furniture.FurnitureManager", false, getClass().getClassLoader());

                    furnitureEngine = new FurnitureEngine(plugin);

                    plugin.integrationMessage("Hooked into " + furnitureEnginePlugin.getName() + " "
                            + furnitureEnginePlugin.getDescription().getVersion() + ".");
                } catch (ClassNotFoundException ignored) {
                    plugin.integrationMessage(furnitureEnginePlugin.getName() + " "
                            + furnitureEnginePlugin.getDescription().getVersion() + " detected, but FurnitureManager " +
                            "class not found, disabling integration.");
                }
            }
        } else {
            furnitureEngine = null;
        }
    }

    private void loadProtectionLib() {
        if (plugin.getConfig().getBoolean("settings.integration.protectionlib.enabled", true)) {
            Plugin protectionLibPlugin = plugin.getServer().getPluginManager().getPlugin("ProtectionLib");

            if (protectionLibPlugin != null && protectionLibPlugin.isEnabled()) {
                protectionLib = new ProtectionLib(plugin, protectionLibPlugin);

                plugin.integrationMessage("Hooked into " + protectionLibPlugin.getName() + " "
                        + protectionLibPlugin.getDescription().getVersion() + ".");
            }
        } else {
            protectionLib = null;
        }
    }

    private void loadItemsAdder() {
        if (plugin.getConfig().getBoolean("settings.integration.itemsadder.enabled", true)) {
            Plugin itemsAdderPlugin = plugin.getServer().getPluginManager().getPlugin("ItemsAdder");

            if (itemsAdderPlugin != null && itemsAdderPlugin.isEnabled()) {
                itemsAdder = new ItemsAdder(plugin, itemsAdderPlugin);

                plugin.integrationMessage("Hooked into " + itemsAdderPlugin.getName() + " "
                        + itemsAdderPlugin.getDescription().getVersion() + ".");
            }
        } else {
            itemsAdder = null;
        }
    }

    private void loadOraxen() {
        if (plugin.getConfig().getBoolean("settings.integration.oraxen.enabled", true)) {
            Plugin oraxenPlugin = plugin.getServer().getPluginManager().getPlugin("Oraxen");

            if (oraxenPlugin != null && oraxenPlugin.isEnabled()) {
                oraxen = new Oraxen(plugin, oraxenPlugin);

                plugin.integrationMessage("Hooked into " + oraxenPlugin.getName() + " "
                        + oraxenPlugin.getDescription().getVersion() + ".");
            }
        } else {
            oraxen = null;
        }
    }

    private void loadMiniMessage() {
        if (plugin.getConfig().getBoolean("settings.integration.minimessage.enabled", true)) {
            try {
                Class.forName("net.kyori.adventure.text.minimessage.MiniMessage", false,
                        getClass().getClassLoader());
                Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer", false,
                        getClass().getClassLoader());

                miniMessage = new MiniMessage();

                plugin.integrationMessage("Hooked into MiniMessage.");
            } catch (ClassNotFoundException ignored) {
            }
        } else {
            miniMessage = null;
        }
    }

    private void loadMineDown() {
        if (plugin.getConfig().getBoolean("settings.integration.minedown.enabled", true)) {
            Plugin mineDownPlugin = plugin.getServer().getPluginManager().getPlugin("MineDownPlugin");

            if (mineDownPlugin != null && mineDownPlugin.isEnabled()) {
                mineDown = new MineDown();

                plugin.integrationMessage("Hooked into " + mineDownPlugin.getName() + " "
                        + mineDownPlugin.getDescription().getVersion() + ".");
            }
        } else {
            mineDown = null;
        }
    }

    private void loadChestSort() {
        if (plugin.getConfig().getBoolean("settings.integration.chestsort.enabled", true)) {
            Plugin chestSortPlugin = plugin.getServer().getPluginManager().getPlugin("ChestSort");

            if (chestSortPlugin != null && chestSortPlugin.isEnabled()) {
                chestSort = new ChestSort();

                plugin.integrationMessage("Hooked into " + chestSortPlugin.getName() + " "
                        + chestSortPlugin.getDescription().getVersion() + ".");
            }
        } else {
            chestSort = null;
        }
    }

    private void loadPlayerNPC() {
        if (plugin.getConfig().getBoolean("settings.integration.playernpc.enabled", true)) {
            Plugin playerNPCPlugin = plugin.getServer().getPluginManager().getPlugin("PlayerNPC");

            if (playerNPCPlugin != null && playerNPCPlugin.isEnabled()) {
                playerNPC = new PlayerNPC(plugin);

                plugin.integrationMessage("Hooked into " + playerNPCPlugin.getName() + " "
                        + playerNPCPlugin.getDescription().getVersion() + ".");
            }
        } else {
            playerNPC = null;
        }
    }

    private void loadCitizensNPC() {
        if (plugin.getConfig().getBoolean("settings.integration.citizens.enabled", true)) {
            Plugin citizensPlugin = plugin.getServer().getPluginManager().getPlugin("Citizens");

            if (citizensPlugin != null && citizensPlugin.isEnabled()) {
                citizensNPC = new CitizensNPC(plugin);

                plugin.integrationMessage("Hooked into " + citizensPlugin.getName() + " "
                        + citizensPlugin.getDescription().getVersion() + ".");
            }
        } else {
            citizensNPC = null;
        }
    }

    private void loadItemBridge() {
        if (plugin.getConfig().getBoolean("settings.integration.itembridge.enabled", true)) {
            Plugin itemBridgePlugin = plugin.getServer().getPluginManager().getPlugin("ItemBridge");

            if (itemBridgePlugin != null && itemBridgePlugin.isEnabled()) {
                if (itemBridge == null) {
                    itemBridge = new ItemBridge(plugin);
                }

                plugin.integrationMessage("Hooked into " + itemBridgePlugin.getName() + " "
                        + itemBridgePlugin.getDescription().getVersion() + ".");
            }
        } else {
            itemBridge = null;
        }
    }

    private void loadPlaceholderAPI() {
        if (placeholderAPI != null) {
            placeholderAPI.unregister();
        }

        if (plugin.getConfig().getBoolean("settings.integration.placeholderapi.enabled")) {
            Plugin placeholderAPIPlugin = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");

            if (placeholderAPIPlugin != null && placeholderAPIPlugin.isEnabled()) {
                placeholderAPI = new PlaceholderAPI(plugin);

                placeholderAPI.register();

                plugin.integrationMessage("Hooked into " + placeholderAPIPlugin.getName() + " "
                        + placeholderAPIPlugin.getDescription().getVersion() + ".");
            }
        } else {
            placeholderAPI = null;
        }
    }

    private void loadSkript() {
        if (plugin.getConfig().getBoolean("settings.integration.skript.enabled")) {
            Plugin skriptPlugin = plugin.getServer().getPluginManager().getPlugin("Skript");
            if (skriptPlugin != null && skriptPlugin.isEnabled()) {
                skriptImpl = new SkriptImpl(plugin);
                plugin.integrationMessage("Hooked into " + skriptPlugin.getName() + " " + skriptPlugin.getDescription().getVersion() + ".");
            }
        } else {
            skriptImpl = null;
        }
    }

    @SuppressWarnings("deprecation")
    private void loadCompatibilityWarnings() {
        if (plugin.getConfig().getBoolean("settings.compatibility.warning")) {
            for (World world : plugin.getServer().getWorlds()) {
                if (world.getGameRuleValue("keepInventory").equals("true")) {
                    plugin.compatibilityMessage("World \"" + world.getName()
                            + "\" has keepInventory set to true, Graves will not be created here.");
                }
            }

            Plugin essentialsPlugin = plugin.getServer().getPluginManager().getPlugin("Essentials");

            if (essentialsPlugin != null && essentialsPlugin.isEnabled()) {
                plugin.compatibilityMessage(essentialsPlugin.getName()
                        + " Detected, make sure you don't have the essentials.keepinv or essentials.keepxp permissions.");
            }

            Plugin deluxeCombatPlugin = plugin.getServer().getPluginManager().getPlugin("DeluxeCombat");

            if (deluxeCombatPlugin != null && deluxeCombatPlugin.isEnabled()) {
                plugin.compatibilityMessage(deluxeCombatPlugin.getName()
                        + " Detected, in order to work with graves you need to set disable-drop-handling to true in " +
                        deluxeCombatPlugin.getName() + "'s data.yml file.");
            }

            similarPluginWarning("DeadChest");
            similarPluginWarning("DeathChest");
            similarPluginWarning("DeathChestPro");
            similarPluginWarning("SavageDeathChest");
            similarPluginWarning("AngelChest");
        }
    }

    private void similarPluginWarning(String string) {
        Plugin similarPlugin = plugin.getServer().getPluginManager().getPlugin(string);

        if (similarPlugin != null && similarPlugin.isEnabled()) {
            plugin.compatibilityMessage(string + " Detected, Graves listens to the death event after " + string +
                    ", and " + string + " clears the drop list. This means Graves will never be created for players " +
                    "if " + string + " is enabled, only non-player entities will create Graves if configured to do so.");
        }
    }
}