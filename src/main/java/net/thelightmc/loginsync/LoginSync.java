package net.thelightmc.loginsync;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;
import net.thelightmc.loginsync.listeners.PlayerListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class LoginSync extends Plugin {
    private Configuration configuration;
    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, new PlayerListener(this));
    }

    public Configuration getConfig() {
        if (configuration == null) loadConfiguration();
        return configuration;
    }

    private void loadConfiguration() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try {
                Files.copy(getResourceAsStream("config.yml"), file.toPath());
                configuration = YamlConfiguration.getProvider(YamlConfiguration.class).load(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
