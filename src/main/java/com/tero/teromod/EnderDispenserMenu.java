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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EnderDispenserMenu extends AbstractContainerMenu {
    private static final int STORAGE_START = 0;
    private static final int STORAGE_END = EnderDispenserStorage.CONTAINER_SIZE;
    private static final int PLAYER_INVENTORY_START = STORAGE_END;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 36;

    private final Container container;
    private final @Nullable BlockPos blockPos;

    public EnderDispenserMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, new SimpleContainer(EnderDispenserStorage.CONTAINER_SIZE),
                buffer.readBlockPos());
    }

    public EnderDispenserMenu(int containerId, Inventory inventory, Container container, BlockPos blockPos) {
        super(ModMenus.ENDER_DISPENSER.get(), containerId);
        this.container = container;
        this.blockPos = blockPos;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(container, column + row * 3, 62 + column * 18, 17 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
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
        if (slotIndex < STORAGE_END) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, STORAGE_START, STORAGE_END, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, original);
        return original;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        if (!this.container.stillValid(player)) {
            return false;
        }
        return this.blockPos != null
                && player.distanceToSqr(this.blockPos.getX() + 0.5, this.blockPos.getY() + 0.5,
                this.blockPos.getZ() + 0.5) <= 64.0
                && player.level().getBlockState(this.blockPos).is(ModBlocks.ENDER_DISPENSER.get());
    }
}
