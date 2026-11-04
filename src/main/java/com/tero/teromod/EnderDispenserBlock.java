package com.tero.teromod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;

public class EnderDispenserBlock extends DispenserBlock {
    public EnderDispenserBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level,
                                                        @NonNull BlockPos pos, @NonNull Player player,
                                                        @NonNull BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EnderDispenserEntity dispenser) {
            player.openMenu(dispenser, pos);
            player.awardStat(Stats.INSPECT_DISPENSER);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void dispenseFrom(ServerLevel level, BlockState state, BlockPos pos) {
        EnderDispenserEntity dispenser = level.getBlockEntity(pos, ModBlockEntities.ENDER_DISPENSER.get())
                .orElse(null);
        if (dispenser == null) {
            TeroMod.LOGGER.warn("Ignoring dispensing attempt for Ender Dispenser without its block entity at {}", pos);
            return;
        }

        BlockSource source = new BlockSource(level, pos, state, dispenser);
        int slot = dispenser.getRandomSlot(level.getRandom());
        if (slot < 0) {
            level.levelEvent(1001, pos, 0);
            level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(dispenser.getBlockState()));
            return;
        }

        ItemStack itemStack = dispenser.getItem(slot);
        DispenseItemBehavior behavior = getDispenseMethod(level, itemStack);
        if (behavior != DispenseItemBehavior.NOOP) {
            dispenser.setItem(slot, behavior.dispense(source, itemStack));
        }
    }

    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new EnderDispenserEntity(pos, state);
    }
}
