package io.github.sdxqw.ffgrinder.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class is used to manage item drops in the game.
 */
public class ItemDrops {

    /**
     * Creates an ItemStack from a given item map.
     * @param itemMap The map containing item details.
     * @return An ItemStack created from the item map.
     */
    public static ItemStack createItemStack(Map<?, ?> itemMap) {
        Material displayMaterial = Material.getMaterial((String) itemMap.get("displayMaterial"));
        assert displayMaterial != null;
        ItemStack itemStack = new ItemStack(displayMaterial, (Integer) itemMap.get("amount"));
        ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', (String) itemMap.get("displayName")));
        itemMeta.setLore(getLore(itemMap));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * Selects a random item from a given map of items with chances.
     * @param itemsWithChances The map containing items with their respective chances.
     * @return A random ItemStack, or null if the chance condition is not met.
     */
    public static ItemStack getRandomItem(Map<?, ?> itemsWithChances) {
        Double chance = (Double) itemsWithChances.get("chance");
        int random = (int) (Math.random() * 100);
        return (chance > 0 && random <= chance) ? createItemStack(itemsWithChances) : null;
    }

    /**
     * Retrieves the lore of an item from a given item map.
     * @param itemMap The map containing item details.
     * @return A list of lore strings, or null if not found.
     */
    private static List<String> getLore(Map<?, ?> itemMap) {
        Object displayLoreObj = itemMap.get("displayLore");
        if (displayLoreObj instanceof List<?>) {
            return ((List<?>) displayLoreObj).stream().filter(line -> line instanceof String).map(line -> ChatColor.translateAlternateColorCodes('&', (String) line)).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Applies enchantments to a given ItemStack.
     * @param itemStack The ItemStack to apply enchantments to.
     * @param enchantments The list of enchantments to apply.
     */
    public static void applyEnchantments(ItemStack itemStack, List<Map<?, ?>> enchantments) {
        for (Map<?, ?> enchantmentMap : enchantments) {
            try {
                Object enchantObj = enchantmentMap.get("enchant");
                if (enchantObj == null) {
                    System.out.println("Enchantment is null");
                    continue;
                }
                NamespacedKey enchantmentKey = NamespacedKey.minecraft(enchantObj.toString().toLowerCase());
                Enchantment enchantment = Enchantment.getByKey(enchantmentKey);
                int enchantmentLevel = (Integer) enchantmentMap.get("level");
                itemStack.addUnsafeEnchantment(Objects.requireNonNull(enchantment), enchantmentLevel);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid enchantment: " + enchantmentMap.get("enchant"));
            }
        }
    }
}