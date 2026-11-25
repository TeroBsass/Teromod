package com.tero.teromod;

import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/**
 * Own implementation of auto-smelting for tools that carry the "Smelting Enchantment" datapack's
 * enchantment (smelting_enchantment:smelting).
 *
 * It works on the drops of every block break, no matter how the block was broken: a normal break,
 * a Veinminer vein (it breaks extra blocks as the player), or instant mining with huge Efficiency.
 * Only drops of ore blocks are smelted, using the normal smelting recipes (so raw iron becomes an
 * iron ingot, and any modded ore that has a smelting recipe works too).
 */
@EventBusSubscriber(modid = TeroMod.MODID)
public class SmeltingDrops {
    private static final ResourceKey<Enchantment> SMELTING = ResourceKey.create(
            Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("smelting_enchantment", "smelting"));
    private static final TagKey<Block> ORES = TagKey.create(
            Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));

    @SubscribeEvent
    public static void smeltOreDrops(BlockDropsEvent event) {
        ServerLevel level = event.getLevel();
        if (!hasSmelting(event.getTool()) || !isOre(event.getState())) {
            return;
        }
        List<ItemEntity> drops = event.getDrops();
        for (ItemEntity drop : drops) {
            ItemStack smelted = smelt(level, drop.getItem());
            if (!smelted.isEmpty()) {
                drop.setItem(smelted);
            }
        }
    }

    private static boolean hasSmelting(ItemStack tool) {
        ItemEnchantments enchantments = tool.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null) {
            return false;
        }
        for (var enchantment : enchantments.keySet()) {
            if (enchantment.is(SMELTING)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOre(BlockState state) {
        return state.is(ORES) || state.is(Blocks.ANCIENT_DEBRIS);
    }

    /** The smelting result for the whole stack, or an empty stack when the item cannot be smelted. */
    private static ItemStack smelt(ServerLevel level, ItemStack stack) {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        ItemStack result = level.recipeAccess()
                .getRecipeFor(RecipeType.SMELTING, input, level)
                .map(recipe -> recipe.value().assemble(input))
                .orElse(ItemStack.EMPTY);
        if (result.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack smelted = result.copy();
        smelted.setCount(Math.min(smelted.getMaxStackSize(), result.getCount() * stack.getCount()));
        return smelted;
    }
}
