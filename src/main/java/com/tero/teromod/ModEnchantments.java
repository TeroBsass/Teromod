package com.tero.teromod;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * The mod's enchantments are data-driven: their definitions are JSON files in
 * data/teromod/enchantment, and the game logic lives in the handler classes next to this one.
 */
public final class ModEnchantments {
    private ModEnchantments() {
    }

    public static final ResourceKey<Enchantment> EFFECT_PROTECTION = key("effect_protection");
    public static final ResourceKey<Enchantment> MAGNET = key("magnet");
    public static final ResourceKey<Enchantment> IMPERCEPTION = key("imperception");
    public static final ResourceKey<Enchantment> ABSORPTION = key("absorption");

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(TeroMod.MODID, name));
    }

    /** The level of the given enchantment on the stack, or 0 when it is missing. */
    public static int level(ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) {
            return 0;
        }
        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null) {
            return 0;
        }
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            if (enchantment.is(key)) {
                return enchantments.getLevel(enchantment);
            }
        }
        return 0;
    }
}
