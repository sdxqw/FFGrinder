package io.github.sdxqw.ffgrinder.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemDrops {
    public static ItemStack createItemStack(Map<?, ?> itemMap) {
        Integer amount = (Integer) itemMap.get("amount");
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required.");
        }

        String displayName = (String) itemMap.get("displayName");
        if (displayName == null) {
            throw new IllegalArgumentException("displayName is required.");
        }

        Material displayMaterial = Material.getMaterial((String) itemMap.get("displayMaterial"));
        if (displayMaterial == null) {
            throw new IllegalArgumentException("Invalid displayMaterial.");
        }

        List<String> lore = null;
        Object displayLoreObj = itemMap.get("displayLore");

        if (displayLoreObj instanceof List<?>) {
            lore = ((List<?>) displayLoreObj).stream()
                    .map(line -> {
                        if (line instanceof String) {
                            return ChatColor.translateAlternateColorCodes('&', (String) line);
                        } else {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        ItemStack itemStack = new ItemStack(displayMaterial, amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        if (lore != null) {
            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }


    public static ItemStack getRandomItem(Map<?, ?> itemsWithChances) {
        Double chance = (Double) itemsWithChances.get("chance");
        if (chance == null) {
            throw new IllegalArgumentException("chance is required.");
        }

        if (chance <= 0) {
            return null;
        }

        if (chance >= 100) {
            return createItemStack(itemsWithChances);
        }

        int random = (int) (Math.random() * 100);
        if (random <= chance) {
            return createItemStack(itemsWithChances);
        }

        return null;
    }

    public static void applyEnchantments(ItemStack itemStack, List<Map<?, ?>> enchantments) {
        if (itemStack == null || enchantments == null) {
            return;
        }

        for (Map<?, ?> enchantmentMap : enchantments) {
            String enchantmentName = (String) enchantmentMap.get("enchant");
            Enchantment enchantment = Enchantment.getByName(enchantmentName);

            if (enchantment != null) {
                int enchantmentLevel = (Integer) enchantmentMap.get("level");
                itemStack.addUnsafeEnchantment(enchantment, enchantmentLevel);
            }
        }
    }

}
