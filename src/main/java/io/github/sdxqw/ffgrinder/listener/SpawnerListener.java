package io.github.sdxqw.ffgrinder.listener;

import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.sdxqw.ffgrinder.config.SpawnerConfig;
import io.github.sdxqw.ffgrinder.item.ItemDrops;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class SpawnerListener implements Listener {

    private final SpawnerConfig spawnerConfig;
    private final List<ItemStack> eligibleItems = new ArrayList<>();

    /**
     * Constructor for SpawnerListener class.
     *
     * @param spawnerConfig The SpawnerConfig instance
     */
    public SpawnerListener(SpawnerConfig spawnerConfig) {
        this.spawnerConfig = spawnerConfig;
    }

    /**
     * Event handler for entity death events.
     *
     * @param event The entity death event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        String entityTypeName = event.getEntity().getType().name();
        if (spawnerConfig.isEntityTypeConfigured(entityTypeName)) {
            List<String> allowedWorlds = spawnerConfig.getAllowedWorlds(entityTypeName);
            if (allowedWorlds != null && allowedWorlds.contains(event.getEntity().getWorld().getName())) {
                processItems(entityTypeName);
                dropSelectedItem(entityTypeName, event);
            }
        }
    }

    /**
     * Processes items with chances and command items with chances for a specific entity type.
     *
     * @param entityTypeName The name of the entity type
     */
    private void processItems(String entityTypeName) {
        processItemsWithChances(entityTypeName);
        processCommandItemsWithChances(entityTypeName);
    }

    /**
     * Processes items with chances for a specific entity type and adds them to the eligible items list.
     *
     * @param entityTypeName The name of the entity type
     */
    private void processItemsWithChances(String entityTypeName) {
        List<Map<?, ?>> itemsWithChances = spawnerConfig.getItemsWithChances(entityTypeName);
        if (itemsWithChances != null) {
            itemsWithChances.stream().map(ItemDrops::getRandomItem).filter(Objects::nonNull).peek(itemStack -> ItemDrops.applyEnchantments(itemStack, spawnerConfig.getEnchants(entityTypeName))).forEach(eligibleItems::add);
        }
    }

    /**
     * Processes command items with chances for a specific entity type and add them to the eligible items list.
     *
     * @param entityTypeName The name of the entity type
     */
    private void processCommandItemsWithChances(String entityTypeName) {
        List<Map<?, ?>> commandItemsWithChances = spawnerConfig.getCommandItemsWithChances(entityTypeName);
        if (commandItemsWithChances != null) {
            commandItemsWithChances.stream().map(ItemDrops::getRandomItem).filter(Objects::nonNull).map(itemStack -> {
                NBTItem nbtItem = new NBTItem(itemStack);
                nbtItem.setString("CommandItem", getCommand(commandItemsWithChances));
                return nbtItem.getItem();
            }).forEach(eligibleItems::add);
        }
    }

    /**
     * Retrieves the command associated with a list of command items.
     *
     * @param commandItemsWithChances The list of command items
     * @return The command associated with the command items
     */
    private String getCommand(List<Map<?, ?>> commandItemsWithChances) {
        return commandItemsWithChances.stream().map(commandItem -> (String) commandItem.get("command")).collect(Collectors.toList()).toString();
    }

    /**
     * Drops a random number of random items from the eligible items list when an entity dies.
     *
     * @param entityTypeName The name of the entity type
     * @param event The entity death event
     */
    private void dropSelectedItem(String entityTypeName, EntityDeathEvent event) {
        if (!eligibleItems.isEmpty()) {
            if (spawnerConfig.isDefaultDrop(entityTypeName)) {
                event.getDrops().add(eligibleItems.get(new Random().nextInt(eligibleItems.size())));
            } else {
                event.getDrops().clear();
                event.getDrops().add(eligibleItems.get(new Random().nextInt(eligibleItems.size())));
            }

            eligibleItems.clear();
        }
    }

    /**
     * Event handler for player interaction events.
     * Cancels the event and removes the item from the player's inventory if the item is a command item.
     *
     * @param event The player interaction event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();
        if (itemStack != null && new NBTItem(itemStack).hasTag("CommandItem")) {
            event.setCancelled(true);
            if (event.getPlayer().getInventory().getItemInMainHand().getAmount() > 1) {
                event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
            } else event.getPlayer().getInventory().setItemInMainHand(null);
            event.getPlayer().sendMessage(spawnerConfig.getRedeemMessageFromDisplayName(ChatColor.stripColor(Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName())));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), new NBTItem(itemStack).getString("CommandItem").replace("{player}", event.getPlayer().getName()));
        }
    }
}