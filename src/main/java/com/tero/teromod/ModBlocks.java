package com.tero.teromod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TeroMod.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TeroMod.MODID);

    public static final DeferredBlock<PlayerDetectorRailBlock> PLAYER_DETECTOR_RAIL =
            BLOCKS.register("player_detector_rail", id -> new PlayerDetectorRailBlock(
                    BlockBehaviour.Properties.of()
                            .noCollision()
                            .strength(0.7F)
                            .sound(SoundType.METAL)
                            .setId(ResourceKey.create(Registries.BLOCK, id))));

    public static final DeferredItem<BlockItem> PLAYER_DETECTOR_RAIL_ITEM =
            ITEMS.registerSimpleBlockItem(PLAYER_DETECTOR_RAIL);

    public static final DeferredBlock<RailCreatorBlock> RAIL_CREATOR =
            BLOCKS.registerBlock(
                    "rail_creator",
                    RailCreatorBlock::new,
                    props -> props
                            .mapColor(MapColor.METAL)
                            .requiresCorrectToolForDrops()
                            .strength(2.5F)
                            .sound(SoundType.ANVIL)
                            .lightLevel(state -> state.getValue(RailCreatorBlock.LIT) ? 13 : 0));

    public static final DeferredItem<BlockItem> RAIL_CREATOR_ITEM = ITEMS.registerItem(
            "rail_creator", props -> new BlockItem(RAIL_CREATOR.get(), props)
    );
}
