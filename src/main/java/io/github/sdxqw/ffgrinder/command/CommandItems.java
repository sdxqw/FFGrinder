package io.github.sdxqw.ffgrinder.command;

import io.github.sdxqw.ffgrinder.FFGrinder;
import io.github.sdxqw.ffgrinder.config.SpawnerConfig;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandItems {

    private final Map<String, Map<String, List<String>>> commandItems;

    private final FFGrinder plugin;
    private final SpawnerConfig spawnerConfig;

    public CommandItems(FFGrinder plugin, SpawnerConfig spawnerConfig) {
        this.plugin = plugin;
        this.spawnerConfig = spawnerConfig;
        this.commandItems = new HashMap<>();
    }

    public void loadCommand() {
        ConfigurationSection config = plugin.getConfig();
        for (String entityTypeName : config.getKeys(false)) {
            List<Map<?, ?>> commandWithChances = plugin.getConfig().getMapList(entityTypeName + ".commandWithChances");

            for (Map<?, ?> commandMap : commandWithChances) {
                String command = (String) commandMap.get("command");
                String displayName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', (String) commandMap.get("displayName")));
                List<String> lore = null;
                Object displayLoreObj = commandMap.get("displayLore");

                if (displayLoreObj instanceof List<?>) {
                    lore = ((List<?>) displayLoreObj).stream()
                            .map(line -> {
                                if (line instanceof String) {
                                    return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', (String) line));
                                } else {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                }

                Map<String, List<String>> displayLoreName = new HashMap<>();

                displayLoreName.put(displayName, lore);

                if (commandItems.containsKey(command)) {
                    commandItems.get(command).put(displayName, lore);
                } else {
                    commandItems.put(command, displayLoreName);
                }
            }
        }
    }

    public boolean isCommandItem(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return false;
        }

        String displayName = ChatColor.stripColor(itemMeta.getDisplayName());
        if (displayName == null) {
            return false;
        }

        for (Map.Entry<String, Map<String, List<String>>> entry : commandItems.entrySet()) {
            Map<String, List<String>> displayLoreMap = entry.getValue();

            if (displayLoreMap.containsKey(displayName)) {
                List<String> expectedLore = displayLoreMap.get(displayName);

                List<String> lore = itemMeta.getLore();
                if (lore == null) {
                    return false;
                }

                for (String loreLine : lore) {
                    String strippedLoreLine = ChatColor.stripColor(loreLine);
                    if (expectedLore.contains(strippedLoreLine)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void executeCommand(Player player, ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return;
        }

        String displayName = ChatColor.stripColor(itemMeta.getDisplayName());
        if (displayName == null) {
            return;
        }

        for (Map.Entry<String, Map<String, List<String>>> entry : commandItems.entrySet()) {
            String command = entry.getKey();
            Map<String, List<String>> displayLoreMap = entry.getValue();

            if (displayLoreMap.containsKey(displayName)) {
                String formattedCommand = command.replace("{player}", player.getName());
                player.performCommand(formattedCommand);
                String redeemMessage = spawnerConfig.getRedeemMessageFromDisplayName(displayName);

                if (redeemMessage != null && !redeemMessage.isEmpty()) {
                    player.sendMessage(redeemMessage);
                }
                if (itemStack.getAmount() > 1) {
                    itemStack.setAmount(itemStack.getAmount() - 1);
                } else {
                    player.setItemInHand(null);
                }
                break;
            }
        }
    }
}