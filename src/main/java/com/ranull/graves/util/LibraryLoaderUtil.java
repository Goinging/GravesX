package com.ranull.graves.util;

import com.ranull.graves.Graves;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import net.byteflux.libby.logging.LogLevel;

/**
 * Utility class for loading external libraries dynamically using BukkitLibraryManager.
 * <p>
 * This class provides methods to load libraries from Maven repositories, with support for
 * relocation and isolation of the loaded libraries.
 * </p>
 */
public class LibraryLoaderUtil {
    private final Graves plugin;

    /**
     * Constructs a new LibraryLoaderUtil instance.
     *
     * @param plugin The plugin instance to associate with the library manager.
     */
    public LibraryLoaderUtil(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads a library with the specified group ID, artifact ID, version, relocation patterns, and isolation setting.
     * <p>
     * Uses default settings for ID.
     * </p>
     *
     * @param groupID                   The group ID of the library.
     * @param artifactID                The artifact ID of the library.
     * @param version                   The version of the library.
     * @param relocatePattern           The package pattern to relocate.
     * @param relocateRelocatedPattern  The relocated package pattern.
     * @param isIsolated                Whether to load the library in an isolated class loader.
     */
    public void loadLibrary(String groupID, String artifactID, String version, String relocatePattern, String relocateRelocatedPattern, boolean isIsolated) {
        loadLibrary(groupID, artifactID, version, null, relocatePattern, relocateRelocatedPattern, isIsolated, null);
    }

    /**
     * Loads a library with the specified group ID, artifact ID, version, relocation patterns, and isolation setting.
     * <p>
     * Uses default settings for ID.
     * </p>
     *
     * @param groupID                   The group ID of the library.
     * @param artifactID                The artifact ID of the library.
     * @param version                   The version of the library.
     * @param relocatePattern           The package pattern to relocate.
     * @param relocateRelocatedPattern  The relocated package pattern.
     * @param isIsolated                Whether to load the library in an isolated class loader.
     * @param libraryURL                Points to an external library URL to a repository.
     */
    public void loadLibrary(String groupID, String artifactID, String version, String relocatePattern, String relocateRelocatedPattern, boolean isIsolated, String libraryURL) {
        loadLibrary(groupID, artifactID, version, null, relocatePattern, relocateRelocatedPattern, isIsolated, libraryURL);
    }

    /**
     * Loads a library with the specified group ID, artifact ID, version, ID, relocation patterns, and isolation setting.
     * <p>
     * Configures the library with optional ID and relocation settings, and loads it using the BukkitLibraryManager.
     * </p>
     *
     * @param groupID                   The group ID of the library.
     * @param artifactID                The artifact ID of the library.
     * @param version                   The version of the library.
     * @param ID                        Optional ID for the library.
     * @param relocatePattern           Optional package pattern to relocate.
     * @param relocateRelocatedPattern  Optional relocated package pattern.
     * @param isIsolated                Whether to load the library in an isolated class loader.
     * @param libraryURL                Points to an external library URL to a repository.
     */
    public void loadLibrary(String groupID, String artifactID, String version, String ID, String relocatePattern, String relocateRelocatedPattern, boolean isIsolated, String libraryURL) {
        try {
            LibraryManager libraryManager = new BukkitLibraryManager(plugin);
            if (libraryURL != null) {
                libraryManager.addRepository(libraryURL);
            } else {
                libraryManager.addMavenCentral();
                libraryManager.addSonatype();
                libraryManager.addJCenter();
                libraryManager.addJitPack();
            }
            libraryManager.setLogLevel(LogLevel.DEBUG);
            Library lib;
            if (ID != null) {
                if (relocatePattern != null) {
                    lib = Library.builder()
                            .groupId(groupID)
                            .artifactId(artifactID)
                            .version(version)
                            .id(ID)
                            .relocate(relocatePattern, relocateRelocatedPattern)
                            .isolatedLoad(isIsolated)
                            .build();
                } else {
                    lib = Library.builder()
                            .groupId(groupID)
                            .artifactId(artifactID)
                            .version(version)
                            .id(ID)
                            .isolatedLoad(isIsolated)
                            .build();
                }
            } else {
                if (relocatePattern != null) {
                    lib = Library.builder()
                            .groupId(groupID)
                            .artifactId(artifactID)
                            .version(version)
                            .relocate(relocatePattern, relocateRelocatedPattern)
                            .isolatedLoad(isIsolated)
                            .build();
                } else {
                    lib = Library.builder()
                            .groupId(groupID)
                            .artifactId(artifactID)
                            .version(version)
                            .isolatedLoad(isIsolated)
                            .build();
                }
            }
            libraryManager.loadLibrary(lib);
            plugin.getLogger().info("Loaded library " + groupID.replace("{}", ".") + "." + artifactID + " version " + version + " successfully.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to download or load library " + groupID.replace("{}", ".") + "." + artifactID + " version " + version + ". Cause: " + e.getCause());
            plugin.logStackTrace(e);
        }
    }
}