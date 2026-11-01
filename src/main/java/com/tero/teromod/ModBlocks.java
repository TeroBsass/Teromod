package com.tero.teromod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("teromod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("teromod");

    public static final DeferredBlock<PlayerDetectorRailBlock> PLAYER_DETECTOR_RAIL =
            BLOCKS.register("player_detector_rail", id -> new PlayerDetectorRailBlock(
                    BlockBehaviour.Properties.of()
                            .noCollision()
                            .strength(0.7F)
                            .sound(SoundType.METAL)
                            .setId(ResourceKey.create(Registries.BLOCK, id))));

    public static final DeferredItem<BlockItem> PLAYER_DETECTOR_RAIL_ITEM =
            ITEMS.registerSimpleBlockItem(PLAYER_DETECTOR_RAIL);
}
