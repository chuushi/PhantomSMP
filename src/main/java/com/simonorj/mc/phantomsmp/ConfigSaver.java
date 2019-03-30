package com.simonorj.mc.phantomsmp;

import org.bukkit.configuration.file.FileConfiguration;

class ConfigSaver {
    static final String REMOVE_TARGETING_RESTED_NODE = "remove-targeting-rested";
    static final String DISALLOW_SPAWNING_FOR_NODE = "disallow-targeting-for";
    static final String CONFIG_VERSION_NODE = "config-version";
    static final int version = 1;

    private static final String HEADER =
            "# PhantomSMP by Simon Chuu\n" +
            "\n" +
            "# For help, follow the plugin project link below:\n" +
            "# https://github.com/SimonOrJ/PhantomSMP/\n";

    private static final String REMOVE_TARGETING_RESTED =
            "# Remove phantoms that try to target player slept within three (Minecraft)\n" +
            "# days?\n" +
            "#   true = remove phantom targeting rested player\n" +
            "#   false = Keep phantoms (and make phantoms try to target another player)\n";

    private static final String DISALLOW_SPAWNING_FOR =
            "# How many ticks since player rested should phantoms ignore the player?\n" +
            "#   NOTE: Any value under 72000 (3 full Minecraft days) will essentially be\n" +
            "#   ignored for phantom spawning.  It will only have an effect on already\n" +
            "#   spawned phantoms only.\n";

    private static final String CONFIG_VERSION =
            "# Keeps track of configuration version -- do not change!";

    static String saveToString(FileConfiguration config) {
        boolean remove = config.getBoolean(REMOVE_TARGETING_RESTED_NODE, true);
        int disallow = config.getInt(DISALLOW_SPAWNING_FOR_NODE, 72000);

        return HEADER +
                "\n" +
                REMOVE_TARGETING_RESTED +
                REMOVE_TARGETING_RESTED_NODE +
                ": " + remove +
                "\n\n" +
                DISALLOW_SPAWNING_FOR +
                DISALLOW_SPAWNING_FOR_NODE +
                ": " + disallow +
                "\n\n" +
                CONFIG_VERSION +
                CONFIG_VERSION_NODE +
                ": " + version +
                "\n";
    }
}
