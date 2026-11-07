package com.tero.teromod;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public class BookEnchantmentTransferRecipe extends CustomRecipe {
    public static final BookEnchantmentTransferRecipe INSTANCE = new BookEnchantmentTransferRecipe();
    public static final MapCodec<BookEnchantmentTransferRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, BookEnchantmentTransferRecipe> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<BookEnchantmentTransferRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private BookEnchantmentTransferRecipe() {
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !getEnchantedItem(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack source = getEnchantedItem(input);
        if (source.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(source);
        enchantedBook.set(DataComponents.STORED_ENCHANTMENTS, enchantments);
        return enchantedBook;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = CraftingRecipe.defaultCraftingReminder(input);
        ItemStack source = getEnchantedItem(input);
        if (source.isEmpty()) {
            return remainingItems;
        }

        for (int slot = 0; slot < input.size(); slot++) {
            if (input.getItem(slot) == source) {
                ItemStack disenchantedItem = source.copyWithCount(1);
                disenchantedItem.remove(DataComponents.ENCHANTMENTS);
                disenchantedItem.remove(DataComponents.STORED_ENCHANTMENTS);
                remainingItems.set(slot, disenchantedItem);
                break;
            }
        }
        return remainingItems;
    }

    private static ItemStack getEnchantedItem(CraftingInput input) {
        if (input.ingredientCount() != 2) {
            return ItemStack.EMPTY;
        }

        boolean hasBook = false;
        ItemStack enchantedItem = ItemStack.EMPTY;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(Items.BOOK)) {
                if (hasBook) {
                    return ItemStack.EMPTY;
                }
                hasBook = true;
            } else if (!stack.is(Items.ENCHANTED_BOOK)
                    && EnchantmentHelper.hasAnyEnchantments(stack)
                    && enchantedItem.isEmpty()) {
                enchantedItem = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }

        return hasBook ? enchantedItem : ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<BookEnchantmentTransferRecipe> getSerializer() {
        return SERIALIZER;
    }
}
