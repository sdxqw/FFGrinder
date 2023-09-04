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

public class SpawnerListener implements Listener {

    private final SpawnerConfig spawnerConfig;
    private final CommandItems commandItems;

    private final List<ItemStack> eligibleItems = new ArrayList<>();

    public SpawnerListener(CommandItems commandItems, SpawnerConfig spawnerConfig) {
        this.spawnerConfig = spawnerConfig;
        this.commandItems = commandItems;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        String entityTypeName = event.getEntity().getType().name();

        if (spawnerConfig.isEntityTypeConfigured(entityTypeName)) {
            List<String> allowedWorlds = spawnerConfig.getAllowedWorlds(entityTypeName);
            if (allowedWorlds != null && allowedWorlds.contains(event.getEntity().getWorld().getName())) {
                List<Map<?, ?>> itemsWithChances = spawnerConfig.getItemsWithChances(entityTypeName);
                if (itemsWithChances != null) {
                    for (Map<?, ?> itemMap : itemsWithChances) {
                        ItemStack itemStack = ItemDrops.getRandomItem(itemMap);
                        if (itemStack != null) {
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

                            eligibleItems.add(itemStack);
                        }
                    }
                }

                List<Map<?, ?>> commandItemsWithChances = spawnerConfig.getCommandItemsWithChances(entityTypeName);

                if (commandItemsWithChances != null) {
                    for (Map<?, ?> commandItemMap : commandItemsWithChances) {
                        ItemStack commandItemStack = ItemDrops.getRandomItem(commandItemMap);
                        if (commandItemStack != null) {
                            eligibleItems.add(commandItemStack);
                        }
                    }
                }

                if (!eligibleItems.isEmpty()) {
                    ItemStack selectedItem = eligibleItems.get(new Random().nextInt(eligibleItems.size()));
                    event.getDrops().clear();
                    event.getDrops().add(selectedItem);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();

        if (commandItems.isCommandItem(itemStack)) {
            commandItems.executeCommand(event.getPlayer(), itemStack);
        }
    }
}
