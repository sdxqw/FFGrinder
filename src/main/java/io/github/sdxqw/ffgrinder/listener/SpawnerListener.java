package io.github.sdxqw.ffgrinder.listener;

import io.github.sdxqw.ffgrinder.command.CommandItems;
import io.github.sdxqw.ffgrinder.config.SpawnerConfig;
import io.github.sdxqw.ffgrinder.item.ItemDrops;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class listens to and handles events related to spawners.
 */
public class SpawnerListener implements Listener {

    private final SpawnerConfig spawnerConfig;
    private final CommandItems commandItems;
    private final List<ItemStack> eligibleItems = new ArrayList<>();

    /**
     * Constructor for SpawnerListener.
     * @param commandItems The command items to be managed.
     * @param spawnerConfig The configuration of spawners to be managed.
     */
    public SpawnerListener(CommandItems commandItems, SpawnerConfig spawnerConfig) {
        this.spawnerConfig = spawnerConfig;
        this.commandItems = commandItems;
    }

    /**
     * Event handler for entity death events.
     * @param event The entity death event.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        String entityTypeName = event.getEntity().getType().name();

        if (spawnerConfig.isEntityTypeConfigured(entityTypeName)) {
            List<String> allowedWorlds = spawnerConfig.getAllowedWorlds(entityTypeName);
            if (allowedWorlds != null && allowedWorlds.contains(event.getEntity().getWorld().getName())) {
                processItemsWithChances(entityTypeName);
                processCommandItemsWithChances(entityTypeName);
                dropSelectedItem(entityTypeName, event);
            }
        }
    }

    /**
     * Processes items with chances for a specific entity type.
     * @param entityTypeName The name of the entity type.
     */
    private void processItemsWithChances(String entityTypeName) {
        List<Map<?, ?>> itemsWithChances = spawnerConfig.getItemsWithChances(entityTypeName);
        if (itemsWithChances != null) {
            for (Map<?, ?> itemMap : itemsWithChances) {
                ItemStack itemStack = ItemDrops.getRandomItem(itemMap);
                if (itemStack != null) {
                    applyEnchantmentsIfAny(itemMap, itemStack);
                    eligibleItems.add(itemStack);
                }
            }
        }
    }

    /**
     * Applies enchantments to an item stack if any are present in the item map.
     * @param itemMap The map containing item details.
     * @param itemStack The item stack to apply enchantments to.
     */
    private void applyEnchantmentsIfAny(Map<?, ?> itemMap, ItemStack itemStack) {
        List<Map<?, ?>> enchantments = new ArrayList<>();
        Object enchantmentsObj = itemMap.get("enchantments");

        if (enchantmentsObj instanceof List<?>) {
            for (Object obj : (List<?>) enchantmentsObj) {
                if (obj instanceof Map<?, ?>) {
                    enchantments.add((Map<?, ?>) obj);
                }
            }
        }

        if (!enchantments.isEmpty()) {
            ItemDrops.applyEnchantments(itemStack, enchantments);
        }
    }

    /**
     * Processes command items with chances for a specific entity type.
     * @param entityTypeName The name of the entity type.
     */
    private void processCommandItemsWithChances(String entityTypeName) {
        List<Map<?, ?>> commandItemsWithChances = spawnerConfig.getCommandItemsWithChances(entityTypeName);

        if (commandItemsWithChances != null) {
            for (Map<?, ?> commandItemMap : commandItemsWithChances) {
                ItemStack commandItemStack = ItemDrops.getRandomItem(commandItemMap);
                if (commandItemStack != null) {
                    eligibleItems.add(commandItemStack);
                }
            }
        }
    }

    /**
     * Drops a selected item when an entity dies.
     * @param entityTypeName The name of the entity type.
     * @param event The entity death event.
     */
    private void dropSelectedItem(String entityTypeName, EntityDeathEvent event) {
        if (!eligibleItems.isEmpty()) {
            ItemStack selectedItem = eligibleItems.get(new Random().nextInt(eligibleItems.size()));
            if (spawnerConfig.isDefaultDrop(entityTypeName)) {
                event.getDrops().add(selectedItem);
            } else {
                event.getDrops().clear();
                event.getDrops().add(selectedItem);
            }
        }
    }

    /**
     * Event handler for player interaction events.
     * @param event The player interaction event.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();

        if (commandItems.isCommandItem(itemStack)) {
            commandItems.executeCommand(event.getPlayer(), itemStack);
        }
    }
}