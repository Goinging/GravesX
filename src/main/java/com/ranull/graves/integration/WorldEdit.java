package com.ranull.graves.integration;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.BlockFaceUtil;
import com.ranull.graves.util.ResourceUtil;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.PasteBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides integration with WorldEdit for schematic operations.
 */
public final class WorldEdit {
    private final Graves plugin;
    private final Plugin worldEditPlugin;
    private final com.sk89q.worldedit.WorldEdit worldEdit;
    private final Map<String, Clipboard> stringClipboardMap;

    /**
     * Constructs a new WorldEdit integration instance with the specified plugin and WorldEdit plugin.
     *
     * @param plugin          The Graves plugin instance.
     * @param worldEditPlugin The WorldEdit plugin instance.
     */
    public WorldEdit(Graves plugin, Plugin worldEditPlugin) {
        this.plugin = plugin;
        this.worldEditPlugin = worldEditPlugin;
        this.worldEdit = com.sk89q.worldedit.WorldEdit.getInstance();
        this.stringClipboardMap = new HashMap<>();

        saveData();
        loadData();
    }

    /**
     * Saves WorldEdit schematics from the WorldEdit plugin's directory to the Graves plugin's data folder.
     */
    public void saveData() {
        if (plugin.getConfig().getBoolean("settings.integration.worldedit.write")) {
            ResourceUtil.copyResources("data/plugin/" + worldEditPlugin.getName().toLowerCase() + "/schematics",
                    plugin.getDataFolder() + "/schematics", plugin);
            plugin.debugMessage("Saving " + worldEditPlugin.getName() + " schematics.", 1);
        }
    }

    /**
     * Loads WorldEdit schematics from the Graves plugin's data folder into memory.
     */
    public void loadData() {
        stringClipboardMap.clear();

        File schematicsFile = new File(plugin.getDataFolder() + File.separator + "schematics");
        File[] listFiles = schematicsFile.listFiles();

        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isFile() && file.getName().contains(".schem")) {
                    String name = file.getName().toLowerCase().replace(".schematic", "")
                            .replace(".schem", "");
                    ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(file);

                    if (clipboardFormat != null) {
                        try (ClipboardReader clipboardReader = clipboardFormat.getReader(new FileInputStream(file))) {
                            stringClipboardMap.put(name, clipboardReader.read());
                            plugin.debugMessage("Loading schematic " + name, 1);
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    } else {
                        plugin.warningMessage("Unable to load schematic " + name);
                    }
                }
            }
        }
    }

    /**
     * Creates and places a schematic at the specified location based on the configuration for the given grave.
     *
     * @param location The location to place the schematic.
     * @param grave    The grave instance containing configuration details.
     */
    public void createSchematic(Location location, Grave grave) {
        if (location.getWorld() != null && plugin.getConfig("schematic.enabled", grave).getBoolean("schematic.enabled")) {
            String schematicName = plugin.getConfig("schematic.name", grave).getString("schematic.name");

            if (schematicName != null && !schematicName.equals("") && hasSchematic(schematicName)) {
                int offsetX = plugin.getConfig("schematic.offset.x", grave).getInt("schematic.offset.x");
                int offsetY = plugin.getConfig("schematic.offset.y", grave).getInt("schematic.offset.y");
                int offsetZ = plugin.getConfig("schematic.offset.z", grave).getInt("schematic.offset.z");

                pasteSchematic(location.clone().add(offsetX, offsetY, offsetZ), location.getYaw(), schematicName);
                plugin.debugMessage("Placing schematic for " + grave.getUUID() + " at "
                        + location.getWorld().getName() + ", " + (location.getBlockX() + 0.5) + "x, "
                        + (location.getBlockY() + 0.5) + "y, " + (location.getBlockZ() + 0.5) + "z", 1);
            } else {
                plugin.debugMessage("Can't find schematic " + schematicName, 1);
            }
        }
    }

    /**
     * Checks if a schematic can be built at the specified location based on the schematic's dimensions and block face.
     *
     * @param location   The location to check.
     * @param blockFace  The block face to check against.
     * @param name       The name of the schematic.
     * @return {@code true} if the schematic can be built, otherwise {@code false}.
     */
    public boolean canBuildSchematic(Location location, BlockFace blockFace, String name) {
        if (location.getWorld() != null) {
            if (stringClipboardMap.containsKey(name)) {
                Clipboard clipboard = stringClipboardMap.get(name);
                BlockVector3 offset = clipboard.getOrigin();
                Region region = clipboard.getRegion();
                int width = region.getWidth();
                int height = region.getHeight();
                int length = region.getLength();
                //Location center = new Location(location.getWorld(), region.getWidth() + offset.getBlockX(), region.getHeight() + offset.getBlockY(), region.getLength() + offset.getBlockZ());
                Location corner;
                try {
                    corner = location.clone().add(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
                } catch (NoSuchMethodError e) {
                    corner = location.clone().add(offset.x(), offset.y(), offset.z());
                }
                Location leftTopCorner = location;
                Location rightTopCorner = location;

                switch (blockFace) {
                    case NORTH:
                        try {
                            leftTopCorner = location.clone().add(offset.getBlockX() - region.getWidth(), offset.getBlockY() - region.getHeight(), 0);
                        } catch (NoSuchMethodError e) {
                            leftTopCorner = location.clone().add(offset.x() - region.getWidth(), offset.y() - region.getHeight(), 0);
                        }
                        break;
                    case WEST:
                        try {
                            leftTopCorner = location.clone().add(region.getWidth() - offset.getBlockX(), offset.getBlockY() - region.getHeight(), 0);
                        } catch (NoSuchMethodError e) {
                            leftTopCorner = location.clone().add(region.getWidth() - offset.x(), offset.y() - region.getHeight(), 0);
                        }
                        break;
                }

                corner.getBlock().setType(Material.BEDROCK);
                // plugin.getServer().broadcastMessage(leftTopCorner.toString());
                // leftTopCorner.getBlock().setType(Material.BEDROCK);
                // plugin.getServer().broadcastMessage(region.toString());
                // plugin.getServer().broadcastMessage(location.toString());
                // center.getBlock().setType(Material.BEDROCK);

            } else {
                plugin.debugMessage("Can't find schematic " + name, 1);
            }
        }

        return false;
    }

    /**
     * Checks if a schematic with the specified name exists.
     *
     * @param string The name of the schematic.
     * @return {@code true} if the schematic exists, otherwise {@code false}.
     */
    public boolean hasSchematic(String string) {
        return stringClipboardMap.containsKey(string.toLowerCase().replace(".schem", ""));
    }

    public void getAreaSchematic(Location location, float yaw, File file) {

    }


    /**
     * Pastes a schematic at the specified location.
     *
     * @param location The location to paste the schematic.
     * @param name     The name of the schematic.
     * @return The Clipboard of the pasted schematic, or {@code null} if the schematic could not be pasted.
     */
    public Clipboard pasteSchematic(Location location, String name) {
        return pasteSchematic(location, 0, name);
    }

    /**
     * Pastes a schematic at the specified location with rotation based on yaw.
     *
     * @param location The location to paste the schematic.
     * @param yaw      The yaw rotation for the schematic.
     * @param name     The name of the schematic.
     * @return The Clipboard of the pasted schematic, or {@code null} if the schematic could not be pasted.
     */
    public Clipboard pasteSchematic(Location location, float yaw, String name) {
        return pasteSchematic(location, yaw, name, true);
    }

    private Clipboard pasteSchematic(Location location, float yaw, String name, boolean ignoreAirBlocks) {
        if (location.getWorld() != null) {
            if (stringClipboardMap.containsKey(name)) {
                Clipboard clipboard = stringClipboardMap.get(name);

                try (EditSession editSession = getEditSession(location.getWorld())) {
                    ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);

                    clipboardHolder.setTransform(clipboardHolder.getTransform().combine(getYawTransform(yaw)));
                    Operations.complete(clipboardHolder.createPaste(editSession).to(locationToBlockVector3(location))
                            .ignoreAirBlocks(ignoreAirBlocks).build());

                    return clipboardHolder.getClipboard();
                } catch (WorldEditException exception) {
                    exception.printStackTrace();
                }
            } else {
                plugin.debugMessage("Can't find schematic " + name, 1);
            }
        }

        return null;
    }

    private PasteBuilder getSchematic(Location location, float yaw, String name) {
        return getSchematic(location, yaw, name, true);
    }


    /**
     * Retrieves a PasteBuilder for the specified schematic at the given location with rotation based on yaw.
     *
     * @param location The location to paste the schematic.
     * @param yaw      The yaw rotation for the schematic.
     * @param name     The name of the schematic.
     * @param ignoreAirBlocks Whether to ignore air blocks when pasting.
     * @return A PasteBuilder for the schematic, or {@code null} if the schematic could not be found.
     */
    private PasteBuilder getSchematic(Location location, float yaw, String name, boolean ignoreAirBlocks) {
        if (location.getWorld() != null) {
            if (stringClipboardMap.containsKey(name)) {
                Clipboard clipboard = stringClipboardMap.get(name);
                ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);

                clipboardHolder.setTransform(clipboardHolder.getTransform().combine(getYawTransform(yaw)));
                return clipboardHolder.createPaste(getEditSession(location.getWorld()))
                        .to(locationToBlockVector3(location)).ignoreAirBlocks(ignoreAirBlocks);
            } else {
                plugin.debugMessage("Can't find schematic " + name, 1);
            }
        }

        return null;
    }

    /**
     * Builds a schematic using the specified PasteBuilder.
     *
     * @param pasteBuilder The PasteBuilder to use for building the schematic.
     */
    public void buildSchematic(PasteBuilder pasteBuilder) {
        try {
            Operations.complete(pasteBuilder.build());
        } catch (WorldEditException exception) {
            exception.printStackTrace();
        }
    }

    private AffineTransform getYawTransform(float yaw) {
        AffineTransform affineTransform = new AffineTransform();

        switch (BlockFaceUtil.getSimpleBlockFace(BlockFaceUtil.getYawBlockFace(yaw))) {
            case SOUTH:
                return affineTransform.rotateY(180);
            case EAST:
                return affineTransform.rotateY(270);
            case WEST:
                return affineTransform.rotateY(90);
        }

        return affineTransform;
    }

    /**
     * Converts a Bukkit Location to a WorldEdit BlockVector3.
     *
     * @param location The Bukkit Location to convert.
     * @return The equivalent WorldEdit BlockVector3.
     */
    public BlockVector3 locationToBlockVector3(Location location) {
        return BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private Location blockVector3ToLocation(World world, BlockVector3 blockVector3) {
        Location Vector3ToLocation;
        try {
            Vector3ToLocation = new Location(world, blockVector3.getBlockX(), blockVector3.getBlockY(), blockVector3.getBlockZ());
        } catch (NoSuchMethodError e) {
            Vector3ToLocation = new Location(world, blockVector3.x(), blockVector3.y(), blockVector3.z());
        }
        return Vector3ToLocation;
    }

    private EditSession getEditSession(World world) {
        return worldEdit.newEditSession(BukkitAdapter.adapt(world));
    }
}