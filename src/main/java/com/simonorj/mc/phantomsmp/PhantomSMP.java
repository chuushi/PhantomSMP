package com.simonorj.mc.phantomsmp;

import com.google.common.base.Charsets;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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

        if (getConfig().getInt(ConfigTool.CONFIG_VERSION_NODE) != ConfigTool.version)
            saveConfig();

        this.removeTargetingRested = getConfig().getBoolean(ConfigTool.REMOVE_TARGETING_RESTED_NODE, true);
        this.removeWhenSleeping = getConfig().getBoolean(ConfigTool.REMOVE_WHEN_SLEEPING_NODE, false);
        this.disallowSpawningFor= getConfig().getInt(ConfigTool.DISALLOW_SPAWNING_FOR_NODE, 72000);

        this.listener = new PhantomListener();
        getServer().getPluginManager().registerEvents(listener, this);

        if (getConfig().getBoolean(ConfigTool.ENABLE_METRICS_NODE, true))
            setupMetrics();
    }

    private void setupMetrics() {
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("removeTargetingRested", () -> String.valueOf(removeTargetingRested)));
        metrics.addCustomChart(new Metrics.SimplePie("removeWhenSleeping", () -> String.valueOf(removeWhenSleeping)));
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
            String data = ConfigTool.saveToString(getConfig());

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
