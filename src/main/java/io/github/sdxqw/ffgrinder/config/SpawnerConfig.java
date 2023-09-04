package io.github.sdxqw.ffgrinder.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class SpawnerConfig {
    private final FileConfiguration config;

    public SpawnerConfig(FileConfiguration config) {
        this.config = config;
    }

    public boolean isEntityTypeConfigured(String entityTypeName) {
        return config.contains(entityTypeName);
    }

    public List<String> getAllowedWorlds(String entityTypeName) {
        ConfigurationSection section = config.getConfigurationSection(entityTypeName);
        if (section != null && section.contains("allowedWorlds")) {
            return section.getStringList("allowedWorlds");
        }
        return null;
    }

    public List<Map<?, ?>> getItemsWithChances(String entityTypeName) {
        ConfigurationSection section = config.getConfigurationSection(entityTypeName);
        if (section != null && section.contains("itemsWithChances")) {
            return section.getMapList("itemsWithChances");
        }
        return null;
    }

    public List<Map<?, ?>> getCommandItemsWithChances(String entityTypeName) {
        ConfigurationSection section = config.getConfigurationSection(entityTypeName);
        if (section != null && section.contains("commandWithChances")) {
            return section.getMapList("commandWithChances");
        }
        return null;
    }
}
