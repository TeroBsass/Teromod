package com.tero.teromod;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Plain (non-block) items of the mod. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TeroMod.MODID);

    /** The equipment asset (assets/teromod/equipment/netherite_elytra.json) that draws the armor and the wings. */
    public static final ResourceKey<EquipmentAsset> NETHERITE_ELYTRA_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(TeroMod.MODID, "netherite_elytra"));

    /** Smithing template for the netherite chestplate + elytra upgrade. */
    public static final DeferredItem<Item> CREATION_TEMPLATE = ITEMS.registerItem("creation_template",
            props -> new Item(props
                    .rarity(Rarity.UNCOMMON)
                    .component(DataComponents.LORE, new ItemLore(List.of(
                            Component.translatable("item.teromod.creation_template.lore")
                                    .withStyle(ChatFormatting.GRAY))))));

    /**
     * A netherite chestplate that glides like an elytra. It is a normal chestplate in every way (armor,
     * toughness, knockback resistance, durability, enchantments) and has the glider component on top.
     */
    public static final DeferredItem<Item> ELYTRA_NETHERITE_CHESTPLATE = ITEMS.registerItem(
            "elytra_netherite_chestplate",
            props -> new Item(props
                    .humanoidArmor(ArmorMaterials.NETHERITE, ArmorType.CHESTPLATE)
                    .fireResistant()
                    .rarity(Rarity.EPIC)
                    .component(DataComponents.GLIDER, Unit.INSTANCE)
                    // Same slot and sound as the netherite chestplate, but with our own equipment asset
                    // that draws the netherite armor plus the elytra wings.
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
                            .setEquipSound(SoundEvents.ARMOR_EQUIP_NETHERITE)
                            .setAsset(NETHERITE_ELYTRA_ASSET)
                            .build())));

    private ModItems() {
    }
}
