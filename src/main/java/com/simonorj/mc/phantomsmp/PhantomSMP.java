package com.simonorj.mc.phantomsmp;

import com.google.common.base.Charsets;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Level;

public class PhantomSMP extends JavaPlugin {
    private static PhantomSMP instance = null;
    private PhantomListener listener = null;
    boolean removeTargetingRested;
    boolean removeWhenSleeping;
    int disallowSpawningFor;

    @Override
    public void onEnable() {
        PhantomSMP.instance = this;
        saveDefaultConfig();

        if (getConfig().getInt(ConfigSaver.CONFIG_VERSION_NODE) != ConfigSaver.version)
            saveConfig();

        this.removeTargetingRested = getConfig().getBoolean(ConfigSaver.REMOVE_TARGETING_RESTED_NODE, true);
        this.removeWhenSleeping = getConfig().getBoolean(ConfigSaver.REMOVE_WHEN_SLEEPING_NODE, false);
        this.disallowSpawningFor= getConfig().getInt(ConfigSaver.DISALLOW_SPAWNING_FOR_NODE, 72000);

        this.listener = new PhantomListener();
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        listener = null;
        PhantomSMP.instance = null;
    }

    @Override
    public void saveConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        try {
            //noinspection ResultOfMethodCallIgnored
            configFile.mkdirs();
            String data = ConfigSaver.saveToString(getConfig());

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), Charsets.UTF_8)) {
                writer.write(data);
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }

    static PhantomSMP getInstance() {
        return instance;
    }
}
