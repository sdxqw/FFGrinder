package io.github.sdxqw.ffgrinder.command;

import io.github.sdxqw.ffgrinder.FFGrinder;
import io.github.sdxqw.ffgrinder.config.SpawnerConfig;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandItems {

    private final Map<String, Map<String, List<String>>> commandItems = new HashMap<>();
    private final FFGrinder plugin;
    private final SpawnerConfig spawnerConfig;

    public CommandItems(FFGrinder plugin, SpawnerConfig spawnerConfig) {
        this.plugin = plugin;
        this.spawnerConfig = spawnerConfig;
    }

    public void loadCommand() {
        plugin.getConfig().getKeys(false).forEach(entityTypeName -> {
            List<Map<?, ?>> commandWithChances = plugin.getConfig().getMapList(entityTypeName + ".commandWithChances");
            commandWithChances.forEach(commandMap -> processCommandMap((String) commandMap.get("command"), commandMap));
        });
    }

    private void processCommandMap(String command, Map<?, ?> commandMap) {
        String displayName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', (String) commandMap.get("displayName")));
        List<String> lore = getLore(commandMap);
        Map<String, List<String>> displayLoreName = new HashMap<>();
        displayLoreName.put(displayName, lore);
        commandItems.putIfAbsent(command, displayLoreName);
        commandItems.get(command).put(displayName, lore);
    }

    private List<String> getLore(Map<?, ?> commandMap) {
        Object displayLoreObj = commandMap.get("displayLore");
        return displayLoreObj instanceof List<?> ? ((List<?>) displayLoreObj).stream().filter(line -> line instanceof String).map(line -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', (String) line))).collect(Collectors.toList()) : null;
    }

    public boolean isCommandItem(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return false;
        ItemMeta itemMeta = itemStack.getItemMeta();
        String displayName = itemMeta != null ? ChatColor.stripColor(itemMeta.getDisplayName()) : null;
        return displayName != null && commandItems.values().stream().anyMatch(displayLoreMap -> displayLoreMap.containsKey(displayName) && isLoreMatch(displayName, itemMeta.getLore()));
    }

    private boolean isLoreMatch(String displayName, List<String> lore) {
        return lore != null && lore.stream().map(ChatColor::stripColor).anyMatch(strippedLoreLine -> commandItems.get(displayName).containsKey(strippedLoreLine));
    }

    public void executeCommand(Player player, ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        String displayName = itemMeta != null ? ChatColor.stripColor(itemMeta.getDisplayName()) : null;
        if (displayName == null) return;
        commandItems.entrySet().stream().filter(entry -> entry.getValue().containsKey(displayName)).findFirst().ifPresent(entry -> processCommand(player, itemStack, displayName, entry.getKey()));
    }

    private void processCommand(Player player, ItemStack itemStack, String displayName, String command) {
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("{player}", player.getName()));
        String redeemMessage = spawnerConfig.getRedeemMessageFromDisplayName(displayName);
        if (redeemMessage != null && !redeemMessage.isEmpty()) player.sendMessage(redeemMessage);
        itemStack.setAmount(itemStack.getAmount() > 1 ? itemStack.getAmount() - 1 : 0);
    }
}