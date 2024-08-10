package com.ranull.graves.manager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;

/**
 * Manages version-specific functionality for the Graves plugin.
 */
public final class VersionManager {
    private final String version;
    private final boolean hasConfigContains;
    private final boolean hasAPIVersion;
    private final boolean hasBlockData;
    private final boolean hasPersistentData;
    private final boolean hasScoreboardTags;
    private final boolean hasHexColors;
    private final boolean hasCompassMeta;
    private final boolean hasSwingHand;
    private final boolean hasWorldHeight;
    private final boolean hasSecondHand;
    private final boolean hasEnchantmentCurse;
    private final boolean hasParticle;
    private boolean isBukkit;
    private boolean isMohist;

    /**
     * Initializes a new instance of the VersionManager class.
     */
    public VersionManager() {
        this.version = getVersion();
        this.hasConfigContains = !is_v1_7() && !is_v1_8() && !is_v1_9();
        this.hasAPIVersion = !is_v1_7() && !is_v1_8() && !is_v1_9() && !is_v1_10() && !is_v1_11() && !is_v1_12();
        this.hasBlockData = !is_v1_7() && !is_v1_8() && !is_v1_9() && !is_v1_10() && !is_v1_11() && !is_v1_12();
        this.hasPersistentData = !is_v1_7() && !is_v1_8() && !is_v1_9() && !is_v1_10() && !is_v1_11() && !is_v1_12()
                && !is_v1_13();
        this.hasScoreboardTags = !is_v1_7() && !is_v1_8() && !is_v1_9() && !is_v1_10();
        this.hasHexColors = !is_v1_7() && !is_v1_8() && !is_v1_9() && !is_v1_10() && !is_v1_11() && !is_v1_12()
                && !is_v1_13() && !is_v1_14() && !is_v1_15() && !isBukkit();
        this.hasCompassMeta = !is_v1_7() && !is_v1_8() && !is_v1_9() && !is_v1_10() && !is_v1_11()
                && !is_v1_12() && !is_v1_13() && !is_v1_14() && !is_v1_15()
                && !version.matches("(?i)v1_16_R1|");
        this.hasSwingHand = !is_v1_7() && !is_v1_8() && !is_v1_9() && !is_v1_10() && !is_v1_11() && !is_v1_12()
                && !is_v1_13() && !is_v1_14() && !is_v1_15();
        this.hasWorldHeight = !is_v1_7() && !is_v1_8() && !is_v1_9() && !is_v1_10() && !is_v1_11() && !is_v1_12()
                && !is_v1_13() && !is_v1_14() && !is_v1_15() && !is_v1_16();
        this.hasSecondHand = !is_v1_7() && !is_v1_8();
        this.hasEnchantmentCurse = !is_v1_7() && !is_v1_8() && !is_v1_9() && !is_v1_10();
        this.hasParticle = !is_v1_7() && !is_v1_8();

        try {
            Class.forName("org.spigotmc.SpigotConfig", false, getClass().getClassLoader());
            this.isBukkit = false;
        } catch (ClassNotFoundException ignored) {
            this.isBukkit = true;
        }

        try {
            Class.forName("com.mohistmc.config.MohistConfigUtil", false, getClass().getClassLoader());
            this.isMohist = true;
        } catch (ClassNotFoundException ignored) {
            this.isMohist = false;
        }
    }

    /**
     * Retrieves the server version.
     *
     * @return The server version string.
     */
    public String getVersion() {
        try {
            return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (Exception e) {
            return Bukkit.getServer().getVersion();
        }
    }

    /**
     * Checks if the server is running on Bukkit.
     *
     * @return True if the server is running on Bukkit, otherwise false.
     */
    public boolean isBukkit() {
        return isBukkit;
    }

    /**
     * Checks if the server is running on Mohist.
     *
     * @return True if the server is running on Mohist, otherwise false.
     */
    public boolean isMohist() {
        return isMohist;
    }

    /**
     * Checks if the server version has the config contains method.
     *
     * @return True if the server version has the config contains method, otherwise false.
     */
    public boolean hasConfigContains() {
        return hasConfigContains;
    }

    /**
     * Checks if the server version has the API version.
     *
     * @return True if the server version has the API version, otherwise false.
     */
    public boolean hasAPIVersion() {
        return hasAPIVersion;
    }

    /**
     * Checks if the server version has block data support.
     *
     * @return True if the server version has block data support, otherwise false.
     */
    public boolean hasBlockData() {
        return hasBlockData;
    }

    /**
     * Checks if the server version has persistent data support.
     *
     * @return True if the server version has persistent data support, otherwise false.
     */
    public boolean hasPersistentData() {
        return hasPersistentData;
    }

    /**
     * Checks if the server version has scoreboard tags support.
     *
     * @return True if the server version has scoreboard tags support, otherwise false.
     */
    public boolean hasScoreboardTags() {
        return hasScoreboardTags;
    }

    /**
     * Checks if the server version has hex color support.
     *
     * @return True if the server version has hex color support, otherwise false.
     */
    public boolean hasHexColors() {
        return hasHexColors;
    }

    /**
     * Checks if the server version has compass meta support.
     *
     * @return True if the server version has compass meta support, otherwise false.
     */
    public boolean hasCompassMeta() {
        return hasCompassMeta;
    }

    /**
     * Checks if the server version has swing hand support.
     *
     * @return True if the server version has swing hand support, otherwise false.
     */
    public boolean hasSwingHand() {
        return hasSwingHand;
    }

    /**
     * Checks if the server version has min height support.
     *
     * @return True if the server version has min height support, otherwise false.
     */
    public boolean hasMinHeight() {
        return hasWorldHeight;
    }

    /**
     * Checks if the server version has second hand support.
     *
     * @return True if the server version has second hand support, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasSecondHand() {
        return hasSecondHand;
    }

    /**
     * Checks if the server version has enchantment curse support.
     *
     * @return True if the server version has enchantment curse support, otherwise false.
     */
    public boolean hasEnchantmentCurse() {
        return hasEnchantmentCurse;
    }

    /**
     * Checks if the server version has particle support.
     *
     * @return True if the server version has particle support, otherwise false.
     */
    public boolean hasParticle() {
        return hasParticle;
    }

    /**
     * Checks if the server version is 1.7.
     *
     * @return True if the server version is 1.7, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_7() {
        return version.matches("(?i)v1_7_R1|v1_7_R2|v1_7_R3|v1_7_R4");
    }

    /**
     * Checks if the server version is 1.8.
     *
     * @return True if the server version is 1.8, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_8() {
        return version.matches("(?i)v1_8_R1|v1_8_R2|v1_8_R3");
    }

    /**
     * Checks if the server version is 1.9.
     *
     * @return True if the server version is 1.9, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_9() {
        return version.matches("(?i)v1_9_R1|v1_9_R2");
    }

    /**
     * Checks if the server version is 1.10.
     *
     * @return True if the server version is 1.10, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_10() {
        return version.matches("(?i)v1_10_R1");
    }

    /**
     * Checks if the server version is 1.11.
     *
     * @return True if the server version is 1.11, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_11() {
        return version.matches("(?i)v1_11_R1");
    }

    /**
     * Checks if the server version is 1.12.
     *
     * @return True if the server version is 1.12, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_12() {
        return version.matches("(?i)v1_12_R1");
    }

    /**
     * Checks if the server version is 1.13.
     *
     * @return True if the server version is 1.13, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_13() {
        return version.matches("(?i)v1_13_R1|v1_13_R2");
    }

    /**
     * Checks if the server version is 1.14.
     *
     * @return True if the server version is 1.14, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_14() {
        return version.matches("(?i)v1_14_R1");
    }

    /**
     * Checks if the server version is 1.15.
     *
     * @return True if the server version is 1.15, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_15() {
        return version.matches("(?i)v1_15_R1");
    }

    /**
     * Checks if the server version is 1.16.
     *
     * @return True if the server version is 1.16, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_16() {
        return version.matches("(?i)v1_16_R1|v1_16_R2|v1_16_R3");
    }

    /**
     * Checks if the server version is 1.17.
     *
     * @return True if the server version is 1.17, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_17() {
        return version.matches("(?i)v1_17_R1");
    }

    /**
     * Checks if the server version is 1.18.
     *
     * @return True if the server version is 1.18, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean is_v1_18() {
        return version.matches("(?i)v1_18_R1|v1_18_R2");
    }

    /**
     * Retrieves the appropriate particle type for the given version.
     *
     * @param particle The particle name.
     * @return The Particle enum corresponding to the given particle name.
     */
    public Particle getParticleForVersion(String particle) {
        Particle toReturn = null;
        switch (particle) {
            case "REDSTONE":
                try {
                    toReturn = Particle.valueOf("REDSTONE");
                    if (toReturn == null) {
                        toReturn = Particle.valueOf("DUST"); // Assume server is running on 1.20.5 or newer
                    }
                } catch (NullPointerException | IllegalArgumentException e) {
                    toReturn = Particle.valueOf("DUST"); // Assume server is running on 1.20.5 or newer
                }
                break;
            // Add additional cases for other particles if needed
            default:
                throw new IllegalArgumentException("Unsupported particle type: " + particle);
        }
        return toReturn;
    }

    /**
     * Retrieves the appropriate enchantment type for the given version.
     *
     * @param enchantment The enchantment name.
     * @return The Enchantment enum corresponding to the given enchantment name.
     */
    public Enchantment getEnchantmentForVersion(String enchantment) {
        Enchantment toReturn = null;
        switch (enchantment) {
            case "DURABILITY":
                try {
                    toReturn = Enchantment.getByName("DURABILITY");
                    if (toReturn == null) {
                        toReturn = Enchantment.getByName("UNBREAKING"); // Assume server is running on 1.20.5 or newer
                    }
                } catch (NullPointerException | IllegalArgumentException e) {
                    toReturn = Enchantment.getByName("UNBREAKING"); // Assume server is running on 1.20.5 or newer
                }
                break;
            // Add other cases for different enchantments here
        }

        if (toReturn == null) {
            throw new IllegalArgumentException("Enchantment can't be null. This is a bug.");
        }

        return toReturn;
    }

    /**
     * Returns the appropriate Material for the given block depending on the Minecraft version.
     * This method ensures compatibility between different versions, particularly between
     * Minecraft 1.8 and newer versions where certain block names have changed.
     *
     * @param material The original Material to be checked and potentially converted.
     * @return The correct Material for the current Minecraft version.
     *         For example, if the input is Material.SOIL and the version is 1.9 or newer,
     *         it will return Material.FARMLAND.
     * @throws IllegalArgumentException If the provided material is not valid for the current Minecraft version.
     */
    public Material getBlockForVersion(String material) {
        Material toReturn = null;
        switch (material) {
            case "SOIL":
                try {
                    toReturn = Material.valueOf("SOIL");
                } catch (NullPointerException | IllegalArgumentException e) {
                    toReturn = Material.valueOf("FARMLAND"); // Assume server is running on 1.9 or newer
                }
                break;
            // Add other cases for different Materials here
        }
        return toReturn;
    }
}