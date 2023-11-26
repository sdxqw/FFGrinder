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

/**
 * This class is responsible for handling command items in the FFGrinder plugin.
 */
public class CommandItems {

    private final Map<String, Map<String, List<String>>> commandItems = new HashMap<>();
    private final FFGrinder plugin;
    private final SpawnerConfig spawnerConfig;

    /**
     * Constructor for CommandItems class.
     *
     * @param plugin        The FFGrinder plugin instance
     * @param spawnerConfig The SpawnerConfig instance
     */
    public CommandItems(FFGrinder plugin, SpawnerConfig spawnerConfig) {
        this.plugin = plugin;
        this.spawnerConfig = spawnerConfig;
    }

    /**
     * Loads the commands from the plugin's configuration.
     */
    public void loadCommand() {
        plugin.getConfig().getKeys(false).forEach(entityTypeName -> {
            List<Map<?, ?>> commandWithChances = plugin.getConfig().getMapList(entityTypeName + ".commandWithChances");
            commandWithChances.forEach(commandMap -> processCommandMap((String) commandMap.get("command"), commandMap));
        });
    }

    /**
     * Processes a command map and stores it in the commandItems map.
     *
     * @param command    The command to process
     * @param commandMap The map containing command details
     */
    private void processCommandMap(String command, Map<?, ?> commandMap) {
        String displayName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', (String) commandMap.get("displayName")));
        List<String> lore = getLore(commandMap);
        Map<String, List<String>> displayLoreName = new HashMap<>();
        displayLoreName.put(displayName, lore);
        commandItems.putIfAbsent(command, displayLoreName);
        commandItems.get(command).put(displayName, lore);
    }

    /**
     * Retrieves the lore from a command map.
     *
     * @param commandMap The map containing command details
     * @return A list of lore strings
     */
    private List<String> getLore(Map<?, ?> commandMap) {
        Object displayLoreObj = commandMap.get("displayLore");
        return displayLoreObj instanceof List<?> ? ((List<?>) displayLoreObj).stream().filter(line -> line instanceof String).map(line -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', (String) line))).collect(Collectors.toList()) : null;
    }

    /**
     * Checks if an ItemStack is a command item.
     *
     * @param itemStack The ItemStack to check
     * @return true if the ItemStack is a command item, false otherwise
     */
    public boolean isCommandItem(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return false;
        ItemMeta itemMeta = itemStack.getItemMeta();
        String displayName = itemMeta != null ? ChatColor.stripColor(itemMeta.getDisplayName()) : null;
        return displayName != null && commandItems.values().stream().anyMatch(displayLoreMap -> displayLoreMap.containsKey(displayName) && isLoreMatch(displayName, itemMeta.getLore()));
    }

    /**
     * Checks if a lore matches with a command item's lore.
     *
     * @param displayName The display name of the command item
     * @param lore        The lore to check
     * @return true if the lore matches, false otherwise
     */
    private boolean isLoreMatch(String displayName, List<String> lore) {
        return lore != null && lore.stream().map(ChatColor::stripColor).anyMatch(strippedLoreLine -> commandItems.get(displayName).containsKey(strippedLoreLine));
    }

    /**
     * Executes a command associated with an ItemStack.
     *
     * @param player    The player executing the command
     * @param itemStack The ItemStack associated with the command
     */
    public void executeCommand(Player player, ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        String displayName = itemMeta != null ? ChatColor.stripColor(itemMeta.getDisplayName()) : null;
        if (displayName == null) return;
        commandItems.entrySet().stream().filter(entry -> entry.getValue().containsKey(displayName)).findFirst().ifPresent(entry -> processCommand(player, itemStack, displayName, entry.getKey()));
    }

    /**
     * Processes a command associated with an ItemStack.
     *
     * @param player      The player executing the command
     * @param itemStack   The ItemStack associated with the command
     * @param displayName The display name of the command item
     * @param command     The command to execute
     */
    private void processCommand(Player player, ItemStack itemStack, String displayName, String command) {
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("{player}", player.getName()));
        String redeemMessage = spawnerConfig.getRedeemMessageFromDisplayName(displayName);
        if (redeemMessage != null && !redeemMessage.isEmpty()) player.sendMessage(redeemMessage);
        itemStack.setAmount(itemStack.getAmount() > 1 ? itemStack.getAmount() - 1 : 0);
    }
}
