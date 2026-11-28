package com.tero.teromod;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Plain (non-block) items of the mod. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TeroMod.MODID);

    /** Smithing template for the chestplate + elytra upgrade. */
    public static final DeferredItem<Item> CREATION_TEMPLATE = ITEMS.registerItem("creation_template",
            props -> new Item(props
                    .rarity(Rarity.UNCOMMON)
                    .component(DataComponents.LORE, new ItemLore(List.of(
                            Component.translatable("item.teromod.creation_template.lore")
                                    .withStyle(ChatFormatting.GRAY))))));

    private ModItems() {
    }
}
