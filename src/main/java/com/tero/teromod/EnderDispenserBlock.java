package com.tero.teromod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new EnderDispenserEntity(pos, state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level,
                                               @NonNull BlockPos pos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }
}
