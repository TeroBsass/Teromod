package com.tero.teromod;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleSmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Smithing table: Creation Template + netherite chestplate + elytra = netherite chestplate with elytra.
 *
 * The result is the chestplate itself (all its components are kept: damage, name, trim, enchantments),
 * turned into the elytra chestplate. The enchantments of the elytra are merged in wherever they can be
 * applied to the result:
 * <ul>
 *   <li>an enchantment the result does not support (or that conflicts with one already there) is dropped;</li>
 *   <li>the same enchantment on both items gives the higher level, or one level more when the levels are
 *       equal (capped at the maximum level), like in an anvil.</li>
 * </ul>
 */
public class ElytraChestplateSmithingRecipe extends SimpleSmithingRecipe {
    public static final MapCodec<ElytraChestplateSmithingRecipe> MAP_CODEC =
            MapCodec.unit(ElytraChestplateSmithingRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, ElytraChestplateSmithingRecipe> STREAM_CODEC =
            StreamCodec.unit(new ElytraChestplateSmithingRecipe());
    public static final RecipeSerializer<ElytraChestplateSmithingRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    // Created on first use: the items are not registered yet while this class is being loaded.
    private Optional<Ingredient> template;
    private Ingredient base;
    private Optional<Ingredient> addition;

    public ElytraChestplateSmithingRecipe() {
        super(new Recipe.CommonInfo(false));
    }

    private void init() {
        if (template == null) {
            base = Ingredient.of(Items.NETHERITE_CHESTPLATE);
            addition = Optional.of(Ingredient.of(Items.ELYTRA));
            template = Optional.of(Ingredient.of(ModItems.CREATION_TEMPLATE.get()));
        }
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        ItemStack chestplate = input.base();
        ItemStack elytra = input.addition();

        ItemStack result = chestplate.transmuteCopy(ModItems.ELYTRA_NETHERITE_CHESTPLATE.get());

        ItemEnchantments.Mutable merged = new ItemEnchantments.Mutable(
                chestplate.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
        ItemEnchantments fromElytra = elytra.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        for (Holder<Enchantment> enchantment : fromElytra.keySet()) {
            if (!enchantment.value().isSupportedItem(result)) {
                continue;
            }
            int level = fromElytra.getLevel(enchantment);
            int current = merged.getLevel(enchantment);
            if (current > 0) {
                level = current == level ? Math.min(level + 1, enchantment.value().getMaxLevel())
                        : Math.max(current, level);
            } else if (!isCompatibleWithAll(enchantment, merged)) {
                continue;
            }
            merged.set(enchantment, level);
        }

        result.set(DataComponents.ENCHANTMENTS, merged.toImmutable());
        return result;
    }

    private static boolean isCompatibleWithAll(Holder<Enchantment> enchantment, ItemEnchantments.Mutable existing) {
        for (Holder<Enchantment> other : existing.keySet()) {
            if (!other.equals(enchantment) && !Enchantment.areCompatible(enchantment, other)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public RecipeSerializer<ElytraChestplateSmithingRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        init();
        return template;
    }

    @Override
    public Ingredient baseIngredient() {
        init();
        return base;
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        init();
        return addition;
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        init();
        return PlacementInfo.createFromOptionals(List.of(template, Optional.of(base), addition));
    }

    @Override
    public List<RecipeDisplay> display() {
        init();
        return List.of(new SmithingRecipeDisplay(
                Ingredient.optionalIngredientToDisplay(template),
                base.display(),
                Ingredient.optionalIngredientToDisplay(addition),
                new SlotDisplay.ItemSlotDisplay(ModItems.ELYTRA_NETHERITE_CHESTPLATE.get()),
                new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)));
    }
}
