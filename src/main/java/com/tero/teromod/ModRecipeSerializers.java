package com.tero.teromod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, TeroMod.MODID);

    public static final Supplier<RecipeSerializer<BookEnchantmentTransferRecipe>> BOOK_ENCHANTMENT_TRANSFER =
            RECIPE_SERIALIZERS.register("book_enchantment_transfer",
                    () -> BookEnchantmentTransferRecipe.SERIALIZER);

    private ModRecipeSerializers() {
    }
}
