package io.github.sdxqw.ffgrinder.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemDrops {
    public static ItemStack createItemStack(Map<?, ?> itemMap) {
        Material displayMaterial = Material.getMaterial((String) itemMap.get("displayMaterial"));
        ItemStack itemStack = new ItemStack(displayMaterial, (Integer) itemMap.get("amount"));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', (String) itemMap.get("displayName")));
        itemMeta.setLore(getLore(itemMap));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack getRandomItem(Map<?, ?> itemsWithChances) {
        Double chance = (Double) itemsWithChances.get("chance");
        int random = (int) (Math.random() * 100);
        return (chance > 0 && random <= chance) ? createItemStack(itemsWithChances) : null;
    }

    private static List<String> getLore(Map<?, ?> itemMap) {
        Object displayLoreObj = itemMap.get("displayLore");
        if (displayLoreObj instanceof List<?>) {
            return ((List<?>) displayLoreObj).stream().filter(line -> line instanceof String).map(line -> ChatColor.translateAlternateColorCodes('&', (String) line)).collect(Collectors.toList());
        }
        return null;
    }

    public static void applyEnchantments(ItemStack itemStack, List<Map<?, ?>> enchantments) {
        for (Map<?, ?> enchantmentMap : enchantments) {
            String enchantmentName = (String) enchantmentMap.get("enchant");
            Enchantment enchantment = Enchantment.getByName(enchantmentName);
            int enchantmentLevel = (Integer) enchantmentMap.get("level");
            itemStack.addUnsafeEnchantment(enchantment, enchantmentLevel);
        }
    }

}
