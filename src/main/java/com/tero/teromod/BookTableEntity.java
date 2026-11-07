package com.tero.teromod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BookTableEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SLOT_ENCHANT_ITEM = 0, SLOT_BOOK = 1, SLOT_RESULT = 2;
    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    public BookTableEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BOOK_TABLE.get(), pos, blockState);
    }

    @Override public int getContainerSize() {return this.items.size();}
    @Override public boolean isEmpty() {return this.items.stream().allMatch(ItemStack::isEmpty);}
    @Override public @NonNull ItemStack getItem(int slot) {return this.items.get(slot);}
    @Override public @NonNull ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(this.items, slot, count);
        if (!removed.isEmpty() && slot != SLOT_RESULT) {
            updateResult();
        }
        return removed;
    }
    @Override public @NonNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(this.items, slot);
        if (!removed.isEmpty() && slot != SLOT_RESULT) {
            updateResult();
        }
        return removed;
    }
    @Override public void setItem(int slot, @NonNull ItemStack stack) {
        if (slot == SLOT_RESULT && !stack.isEmpty()) return;
        this.items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
        if (slot != SLOT_RESULT) updateResult();
    }
    @Override public boolean stillValid(@NonNull Player player) {return Container.stillValidBlockEntity(this, player);}
    @Override public void clearContent() {
        for (int slot = 0; slot < this.items.size(); slot++) {
            this.items.set(slot, ItemStack.EMPTY);
        }
        updateResult();
    }
    // --- MenuProvider ---
    @Override public @NonNull Component getDisplayName() {return Component.translatable("block.teromod.book_table");}
    @Override public @Nullable AbstractContainerMenu createMenu(int id, @NonNull Inventory inv,
                                                                @NonNull Player player) {
        updateResult();
        return new BookTableMenu(id, inv, this);
    }

    public void consumeInputs(Player player, ItemStack takenResult) {
        if (!(this.level instanceof ServerLevel serverLevel)) return;

        CraftingInput input = CraftingInput.of(2, 1, List.of(
                this.items.get(SLOT_ENCHANT_ITEM),
                this.items.get(SLOT_BOOK)
        ));
        var recipe = serverLevel.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, serverLevel);
        if (recipe.isEmpty()) return;

        ItemStack expectedResult = recipe.get().value().assemble(input);
        if (expectedResult.isEmpty() || !ItemStack.isSameItemSameComponents(expectedResult, takenResult)) return;

        NonNullList<ItemStack> remainingItems = recipe.get().value().getRemainingItems(input);
        for (int slot = SLOT_ENCHANT_ITEM; slot <= SLOT_BOOK; slot++) {
            ItemStack remainder = remainingItems.get(slot);
            ItemStack ingredient = this.items.get(slot);
            ingredient.shrink(1);
            if (ingredient.isEmpty()) {
                this.items.set(slot, remainder);
            } else {
                if (!remainder.isEmpty() && !player.getInventory().add(remainder)) {
                    Containers.dropItemStack(serverLevel, player.getX(), player.getY(), player.getZ(), remainder);
                }
                this.items.set(slot, ingredient);
            }
        }
        setChanged();
        updateResult();
    }

    public void dropInputs(ServerLevel level) {
        for (int slot = SLOT_ENCHANT_ITEM; slot <= SLOT_BOOK; slot++) {
            Containers.dropItemStack(level,
                    this.worldPosition.getX() + 0.5,
                    this.worldPosition.getY() + 0.5,
                    this.worldPosition.getZ() + 0.5,
                    this.items.get(slot));
            this.items.set(slot, ItemStack.EMPTY);
        }
        setChanged();
    }

    private void updateResult() {
        if (!(this.level instanceof ServerLevel serverLevel)) return;

        CraftingInput input = CraftingInput.of(2, 1, List.of(
                this.items.get(SLOT_ENCHANT_ITEM),
                this.items.get(SLOT_BOOK)
        ));

        ItemStack result = serverLevel.recipeAccess()
                .getRecipeFor(RecipeType.CRAFTING, input, serverLevel)
                .map(recipe -> recipe.value().assemble(input))
                .orElse(ItemStack.EMPTY);

        this.items.set(SLOT_RESULT, result);
        boolean hasResult = !result.isEmpty();
        BlockState state = getBlockState();
        if (state.hasProperty(BookTableBlock.LIT) && state.getValue(BookTableBlock.LIT) != hasResult) {
            serverLevel.setBlockAndUpdate(this.worldPosition, state.setValue(BookTableBlock.LIT, hasResult));
        }
        setChanged();
    }

    // --- Saving ---
    @Override protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        NonNullList<ItemStack> inputItems = NonNullList.withSize(SLOT_RESULT, ItemStack.EMPTY);
        for (int slot = 0; slot < SLOT_RESULT; slot++) {
            inputItems.set(slot, this.items.get(slot));
        }
        ContainerHelper.saveAllItems(output, inputItems);
    }
    @Override protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        NonNullList<ItemStack> inputItems = NonNullList.withSize(SLOT_RESULT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, inputItems);
        for (int slot = 0; slot < SLOT_RESULT; slot++) {
            this.items.set(slot, inputItems.get(slot));
        }
        this.items.set(SLOT_RESULT, ItemStack.EMPTY);
        updateResult();
    }

}
