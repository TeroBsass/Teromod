package com.tero.teromod;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TeroMod.MODID);

    public static final Supplier<BlockEntityType<RailCreatorEntity>> RAIL_CREATOR =
            BLOCK_ENTITIES.register("rail_creator", () -> new BlockEntityType<>(
                    RailCreatorEntity::new,
                    false,
                    ModBlocks.RAIL_CREATOR.get()
                    ));

    public static final Supplier<BlockEntityType<EnderDispenserEntity>> ENDER_DISPENSER =
            BLOCK_ENTITIES.register("ender_dispenser", () -> new BlockEntityType<>(
                    EnderDispenserEntity::new,
                    false,
                    ModBlocks.ENDER_DISPENSER.get()
            ));

    public static final Supplier<BlockEntityType<BookTableEntity>> BOOK_TABLE =
            BLOCK_ENTITIES.register("book_table", () -> new BlockEntityType<>(
                    BookTableEntity::new,
                    false,
                    ModBlocks.BOOK_TABLE.get()
            ));
}
