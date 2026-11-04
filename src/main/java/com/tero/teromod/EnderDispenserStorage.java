package com.tero.teromod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class EnderDispenserStorage extends SavedData implements Container {
    public static final int CONTAINER_SIZE = 9;

    private static final Codec<EnderDispenserStorage> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ItemStack.OPTIONAL_CODEC.listOf()
                            .optionalFieldOf("items", List.of())
                            .forGetter(storage -> List.copyOf(storage.items))
            ).apply(instance, EnderDispenserStorage::new));

    private static final SavedDataType<EnderDispenserStorage> DATA_TYPE = new SavedDataType<EnderDispenserStorage>(
            Identifier.fromNamespaceAndPath(TeroMod.MODID, "ender_dispenser"),
            EnderDispenserStorage::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    private EnderDispenserStorage() {
    }

    private EnderDispenserStorage(List<ItemStack> savedItems) {
        int count = Math.min(savedItems.size(), CONTAINER_SIZE);
        for (int slot = 0; slot < count; slot++) {
            this.items.set(slot, savedItems.get(slot));
        }
    }

    public static EnderDispenserStorage get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(DATA_TYPE);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        return slot >= 0 && slot < CONTAINER_SIZE ? this.items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public @NonNull ItemStack removeItem(int slot, int count) {
        if (slot < 0 || slot >= CONTAINER_SIZE) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = ContainerHelper.removeItem(this.items, slot, count);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= CONTAINER_SIZE) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = ContainerHelper.takeItem(this.items, slot);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public void setItem(int slot, @NonNull ItemStack stack) {
        if (slot < 0 || slot >= CONTAINER_SIZE) {
            return;
        }
        this.items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.items.replaceAll(ignored -> ItemStack.EMPTY);
        setChanged();
    }

    @Override
    public void setChanged() {
        setDirty();
    }

}
