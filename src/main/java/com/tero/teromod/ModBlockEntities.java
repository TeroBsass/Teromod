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
}
