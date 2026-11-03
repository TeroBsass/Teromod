package com.tero.teromod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class RailCreatorMenu extends AbstractContainerMenu {
    private static final int RESULT_SLOT = RailCreatorEntity.SLOT_RESULT;
    private static final int PLAYER_INVENTORY_START = 4;
    private static final int PLAYER_INVENTORY_END = 40;
    private final Container container;
    private final @Nullable BlockPos blockPos;

    public RailCreatorMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, new SimpleContainer(4), buffer.readBlockPos());
    }

    public RailCreatorMenu(int id, Inventory inventory, Container container) {
        this(id, inventory, container,
                container instanceof RailCreatorEntity entity ? entity.getBlockPos() : null);
    }

    private RailCreatorMenu(int id, Inventory inventory, Container container, @Nullable BlockPos blockPos) {
        super(ModMenus.RAIL_CREATOR.get(), id);
        this.container = container;
        this.blockPos = blockPos;

        addSlot(new Slot(container, RailCreatorEntity.SLOT_RED_STONE, 8, 46) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return stack.is(Items.REDSTONE);
            }
        });
        addSlot(new Slot(container, RailCreatorEntity.SLOT_RAIL, 26, 46) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return stack.is(Items.RAIL);
            }
        });
        addSlot(new Slot(container, RailCreatorEntity.SLOT_IRON_NUGGET, 44, 46) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return stack.is(Items.IRON_NUGGET);
            }
        });
        addSlot(new Slot(container, RESULT_SLOT, 98, 46) {
            @Override
            public boolean mayPlace(@NonNull ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(@NonNull Player player, @NonNull ItemStack stack) {
                if (container instanceof RailCreatorEntity entity) {
                    entity.consumeInputs();
                }
                super.onTake(player, stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 80 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 135));
        }
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int originalCount = stack.getCount();

        if (slotIndex == RESULT_SLOT) {
            if (!canFitEntireStack(stack)) {
                return ItemStack.EMPTY;
            }
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < RESULT_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, RESULT_SLOT, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == originalCount) {
            return ItemStack.EMPTY;
        }

        ItemStack moved = original.copyWithCount(originalCount - stack.getCount());
        if (slotIndex == RESULT_SLOT) {
            slot.onTake(player, moved);
        }
        return moved;
    }

    private boolean canFitEntireStack(ItemStack stack) {
        int remaining = stack.getCount();
        for (int index = PLAYER_INVENTORY_START; index < PLAYER_INVENTORY_END && remaining > 0; index++) {
            Slot target = this.slots.get(index);
            if (!target.mayPlace(stack)) {
                continue;
            }

            ItemStack existing = target.getItem();
            int capacity = Math.min(stack.getMaxStackSize(), target.getMaxStackSize(stack));
            if (existing.isEmpty()) {
                remaining -= capacity;
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                remaining -= capacity - existing.getCount();
            }
        }
        return remaining <= 0;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        if (!this.container.stillValid(player)) {
            return false;
        }
        return this.blockPos == null
                || player.distanceToSqr(this.blockPos.getX() + 0.5, this.blockPos.getY() + 0.5,
                this.blockPos.getZ() + 0.5) <= 64.0
                && player.level().getBlockState(this.blockPos).is(ModBlocks.RAIL_CREATOR.get());
    }
}
