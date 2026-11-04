package com.tero.teromod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class EnderDispenserEntity extends DispenserBlockEntity implements MenuProvider {
    public EnderDispenserEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ENDER_DISPENSER.get(), pos, blockState);
    }

    private EnderDispenserStorage storage() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return EnderDispenserStorage.get(serverLevel.getServer().overworld());
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.ENDER_DISPENSER.get();
    }

    @Override
    public int getContainerSize() {
        return EnderDispenserStorage.CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        EnderDispenserStorage storage = storage();
        return storage == null || storage.isEmpty();
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        EnderDispenserStorage storage = storage();
        return storage == null ? ItemStack.EMPTY : storage.getItem(slot);
    }

    @Override
    public @NonNull ItemStack removeItem(int slot, int count) {
        EnderDispenserStorage storage = storage();
        if (storage == null) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = storage.removeItem(slot, count);
        if (!removed.isEmpty()) {
            super.setChanged();
        }
        return removed;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        EnderDispenserStorage storage = storage();
        if (storage == null) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = storage.removeItemNoUpdate(slot);
        if (!removed.isEmpty()) {
            super.setChanged();
        }
        return removed;
    }

    @Override
    public void setItem(int slot, @NonNull ItemStack itemStack) {
        EnderDispenserStorage storage = storage();
        if (storage != null && slot >= 0 && slot < EnderDispenserStorage.CONTAINER_SIZE) {
            storage.setItem(slot, itemStack);
            super.setChanged();
        }
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        EnderDispenserStorage storage = storage();
        if (storage != null) {
            storage.clearContent();
            super.setChanged();
        }
    }

    @Override
    public int getRandomSlot(@NonNull RandomSource random) {
        int selectedSlot = -1;
        int seenItems = 1;
        for (int slot = 0; slot < EnderDispenserStorage.CONTAINER_SIZE; slot++) {
            if (!getItem(slot).isEmpty() && random.nextInt(seenItems++) == 0) {
                selectedSlot = slot;
            }
        }
        return selectedSlot;
    }

    @Override
    public void setChanged() {
        EnderDispenserStorage storage = storage();
        if (storage != null) {
            storage.setChanged();
        }
        super.setChanged();
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable("block.teromod.ender_dispenser");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        EnderDispenserStorage storage = storage();
        return storage == null ? null : new EnderDispenserMenu(containerId, inventory, this, this.worldPosition);
    }
}
