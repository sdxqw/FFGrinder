package io.github.sdxqw.ffgrinder.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

/**
 * This class is used to manage the configuration of spawners.
 */
public class SpawnerConfig {
    private final FileConfiguration config;

    /**
     * Constructor for SpawnerConfig.
     * @param config The configuration file to be managed.
     */
    public SpawnerConfig(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Checks if a specific entity type is configured.
     * @param entityTypeName The name of the entity type.
     * @return True if the entity type is configured, false otherwise.
     */
    public boolean isEntityTypeConfigured(String entityTypeName) {
        return config.contains(entityTypeName);
    }

    /**
     * Retrieves the list of allowed worlds for a specific entity type.
     * @param entityTypeName The name of the entity type.
     * @return A list of allowed worlds, or null if not configured.
     */
    public List<String> getAllowedWorlds(String entityTypeName) {
        ConfigurationSection section = config.getConfigurationSection(entityTypeName);
        if (section != null && section.contains("allowedWorlds")) {
            return section.getStringList("allowedWorlds");
        }
        return null;
    }

    /**
     * Retrieves the list of items with their respective chances for a specific entity type.
     * @param entityTypeName The name of the entity type.
     * @return A list of maps containing items and their chances, or null if not configured.
     */
    public List<Map<?, ?>> getItemsWithChances(String entityTypeName) {
        ConfigurationSection section = config.getConfigurationSection(entityTypeName);
        if (section != null && section.contains("itemsWithChances")) {
            return section.getMapList("itemsWithChances");
        }
        return null;
    }

    /**
     * Retrieves the list of command items with their respective chances for a specific entity type.
     * @param entityTypeName The name of the entity type.
     * @return A list of maps containing command items and their chances, or null if not configured.
     */
    public List<Map<?, ?>> getCommandItemsWithChances(String entityTypeName) {
        ConfigurationSection section = config.getConfigurationSection(entityTypeName);
        if (section != null && section.contains("commandWithChances")) {
            return section.getMapList("commandWithChances");
        }
        return null;
    }

    /**
     * Retrieves the redeem message for a specific display name.
     * @param displayName The display name.
     * @return The redeem message, or null if not found.
     */
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

    /**
     * Checks if a specific entity type is set as a default drop.
     * @param entityTypeName The name of the entity type.
     * @return True if the entity type is a default drop, false otherwise.
     */
    public boolean isDefaultDrop(String entityTypeName) {
        ConfigurationSection section = config.getConfigurationSection(entityTypeName);

        if (section != null && section.contains("defaultDrop")) {
            return section.getBoolean("defaultDrop");
        }

        return false;
    }
}
