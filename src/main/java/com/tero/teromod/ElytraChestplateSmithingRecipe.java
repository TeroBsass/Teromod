package com.tero.teromod;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
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
 * Smithing table: Creation Template + any chestplate + elytra = the same chestplate that can glide.
 *
 * The result is the very same item (so its icon, trim, dye, name, damage and enchantments are untouched),
 * with the glider component added. For the vanilla armor materials the equipment asset is swapped for a
 * copy that also draws the elytra wings on the back (assets/teromod/equipment/&lt;material&gt;_elytra.json);
 * the Advanced Netherite chestplates are covered the same way; other modded chestplates glide too but get
 * no wings model.
 *
 * The enchantments of the elytra are merged in wherever they can be applied to the result:
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
            // The recipe has no data. StreamCodec.unit would insist on one exact instance, but every data
            // reload creates a new one (MAP_CODEC), so write nothing and make a fresh recipe when reading.
            StreamCodec.of((buf, recipe) -> {
            }, buf -> new ElytraChestplateSmithingRecipe());
    public static final RecipeSerializer<ElytraChestplateSmithingRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    /**
     * Armor assets that have a copy with wings in this mod's resources (assets/teromod/equipment):
     * "&lt;namespace&gt;_&lt;name&gt;_elytra.json", or just "&lt;name&gt;_elytra.json" for vanilla.
     * Besides vanilla this covers the Advanced Netherite mod.
     */
    private static final Map<String, Set<String>> WINGED_ASSETS = Map.of(
            "minecraft", Set.of("leather", "chainmail", "iron", "gold", "diamond", "netherite", "copper"),
            "advancednetherite", Set.of("netherite_iron", "netherite_gold", "netherite_emerald", "netherite_diamond"));

    // Created on first use: the items are not registered yet while this class is being loaded.
    private Optional<Ingredient> template;
    private Ingredient base;
    private Optional<Ingredient> addition;

    public ElytraChestplateSmithingRecipe() {
        super(new Recipe.CommonInfo(false));
    }

    private void init() {
        if (template == null) {
            base = Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(ItemTags.CHEST_ARMOR));
            addition = Optional.of(Ingredient.of(Items.ELYTRA));
            template = Optional.of(Ingredient.of(ModItems.CREATION_TEMPLATE.get()));
        }
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        // A chestplate that already glides cannot get a second pair of wings.
        return !input.base().has(DataComponents.GLIDER) && super.matches(input, level);
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        ItemStack chestplate = input.base();
        ItemStack elytra = input.addition();

        ItemStack result = chestplate.copyWithCount(1);
        result.set(DataComponents.GLIDER, Unit.INSTANCE);

        Equippable equippable = result.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.assetId().isPresent()) {
            Identifier asset = equippable.assetId().get().identifier();
            String namespace = asset.getNamespace();
            if (WINGED_ASSETS.getOrDefault(namespace, Set.of()).contains(asset.getPath())) {
                String name = (namespace.equals("minecraft") ? "" : namespace + "_") + asset.getPath() + "_elytra";
                result.set(DataComponents.EQUIPPABLE, Equippable.builder(equippable.slot())
                        .setEquipSound(equippable.equipSound())
                        .setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID,
                                Identifier.fromNamespaceAndPath(TeroMod.MODID, name)))
                        .build());
            }
        }

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
                new SlotDisplay.ItemSlotDisplay(Items.NETHERITE_CHESTPLATE),
                new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)));
    }
}
