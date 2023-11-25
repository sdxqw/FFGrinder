package io.github.sdxqw.ffgrinder.config;

import org.bukkit.ChatColor;
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

    public String getRedeemMessageFromDisplayName(String displayName) {
        ConfigurationSection configurationSection = config;

        for (String entityTypeName : configurationSection.getKeys(false)) {
            ConfigurationSection section = configurationSection.getConfigurationSection(entityTypeName);

            if (section != null && section.contains("commandWithChances")) {
                List<Map<?, ?>> commandWithChances = section.getMapList("commandWithChances");
                for (Map<?, ?> commandMap : commandWithChances) {
                    String commandSection = (String) commandMap.get("redeemMessage");

                    if (commandSection == null) {
                        continue;
                    }

                    if (displayName.equals(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', (String) commandMap.get("displayName"))))) {
                        return ChatColor.translateAlternateColorCodes('&', commandSection);
                    }
                }
            }
        }

        return null;
    }

    public boolean isDefaultDrop(String entityTypeName) {
        ConfigurationSection section = config.getConfigurationSection(entityTypeName);

        if (section != null && section.contains("defaultDrop")) {
            return section.getBoolean("defaultDrop");
        }

        return false;
    }


}
