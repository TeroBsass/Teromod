package com.tero.teromod;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;

public class StopperBlock extends Block {
    private static final int DISPENSER_RADIUS = 3;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final int[][] MODEL_PARTS = {
            {0, 0, 1, 16, 1, 15},
            {1, 1, 2, 15, 2, 14},
            {1, 1, 3, 3, 3, 13},
            {6, 1, 3, 8, 3, 13},
            {11, 1, 3, 13, 3, 13},
            {0, 2, 5, 12, 4, 6},
            {0, 2, 10, 12, 4, 11},
            {10, 2, 3, 13, 8, 6},
            {10, 2, 10, 13, 8, 13},
            {12, 6, 3, 14, 12, 13},
            {11, 11, 2, 15, 13, 14},
            {14, 7, 5, 15, 10, 11}
    };
    private static final VoxelShape WEST_SHAPE = createShape(Direction.WEST);
    private static final VoxelShape NORTH_SHAPE = createShape(Direction.NORTH);
    private static final VoxelShape EAST_SHAPE = createShape(Direction.EAST);
    private static final VoxelShape SOUTH_SHAPE = createShape(Direction.SOUTH);

    public StopperBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.WEST));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        for (AbstractMinecart cart : level.getEntitiesOfClass(AbstractMinecart.class,
                new AABB(pos).inflate(0.5, 0.25, 0.5))) {
            handleCart(state, level, pos, cart);
        }

        if (level.getBlockState(pos).is(this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide() || !(entity instanceof AbstractMinecart cart)) {
            return;
        }
        handleCart(state, level, pos, cart);
    }

    private static void handleCart(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        if (!cart.isAlive() || !PlayerDetectorRailBlock.hasPlayerPassenger(cart)) {
            return;
        }
        Vec3 motion = new Vec3(cart.getX() - cart.xOld, 0.0, cart.getZ() - cart.zOld);
        if (motion.horizontalDistanceSqr() <= 1.0E-6) {
            motion = cart.getDeltaMovement();
        }
        if (motion.horizontalDistanceSqr() <= 1.0E-6) {
            return;
        }

        Direction intoStopper = motionDirection(motion);
        Direction trackDirection = intoStopper.getOpposite();
        BlockPos railPos = pos.relative(trackDirection);
        BlockState railState = level.getBlockState(railPos);
        if (!(railState.getBlock() instanceof PlayerDetectorRailBlock rail)) {
            return;
        }

        Direction railImpulse = rail.getImpulseDirection(level, railPos, railState, cart);
        if (motion.x * railImpulse.getStepX() + motion.z * railImpulse.getStepZ() >= 0.0) {
            return;
        }

        destroyCart(level, pos, trackDirection, cart);
    }

    private static Direction motionDirection(Vec3 motion) {
        if (Math.abs(motion.x) >= Math.abs(motion.z)) {
            return motion.x >= 0.0 ? Direction.EAST : Direction.WEST;
        }
        return motion.z >= 0.0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static void destroyCart(Level level, BlockPos pos, Direction trackDirection, AbstractMinecart cart) {
        ItemStack cartItem = cart.getPickResult();
        double x = cart.getX();
        double y = cart.getY();
        double z = cart.getZ();
        List<Entity> passengers = List.copyOf(cart.getPassengers());
        cart.ejectPassengers();
        Direction behind = trackDirection.getOpposite();
        BlockPos exitPos = pos.relative(behind);
        double passengerX = exitPos.getX() + 0.5;
        double passengerY = exitPos.getY();
        double passengerZ = exitPos.getZ() + 0.5;
        passengers.forEach(passenger -> passenger.dismountTo(passengerX, passengerY, passengerZ));
        cart.discard();

        DispenserBlockEntity dispenser = findNearestDispenser(level, x, y, z);
        if (dispenser != null && !cartItem.isEmpty()) {
            int originalCount = cartItem.getCount();
            ItemStack remainder = dispenser.insertItem(cartItem);
            if (remainder.getCount() < originalCount) {
                dispenser.setChanged();
            }
            cartItem = remainder;
        }

        if (!cartItem.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, x, y, z, cartItem));
        }
    }

    private static DispenserBlockEntity findNearestDispenser(Level level, double x, double y, double z) {
        BlockPos origin = BlockPos.containing(x, y, z);
        DispenserBlockEntity nearest = null;
        double nearestDistance = DISPENSER_RADIUS * DISPENSER_RADIUS;

        for (int dx = -DISPENSER_RADIUS; dx <= DISPENSER_RADIUS; dx++) {
            for (int dy = -DISPENSER_RADIUS; dy <= DISPENSER_RADIUS; dy++) {
                for (int dz = -DISPENSER_RADIUS; dz <= DISPENSER_RADIUS; dz++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    double centerX = candidate.getX() + 0.5 - x;
                    double centerY = candidate.getY() + 0.5 - y;
                    double centerZ = candidate.getZ() + 0.5 - z;
                    double distance = centerX * centerX + centerY * centerY + centerZ * centerZ;
                    if (distance > nearestDistance
                            || !level.getBlockState(candidate).is(Blocks.DISPENSER)
                            || !(level.getBlockEntity(candidate) instanceof DispenserBlockEntity candidateDispenser)
                            || candidateDispenser instanceof DropperBlockEntity) {
                        continue;
                    }

                    nearest = candidateDispenser;
                    nearestDistance = distance;
                }
            }
        }

        return nearest;
    }

    private static VoxelShape shapeFor(Direction facing) {
        return switch (facing) {
            case WEST -> WEST_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            default -> WEST_SHAPE;
        };
    }

    private static VoxelShape createShape(Direction facing) {
        VoxelShape shape = Shapes.empty();
        for (int[] part : MODEL_PARTS) {
            int minX = part[0];
            int minY = part[1];
            int minZ = part[2];
            int maxX = part[3];
            int maxY = part[4];
            int maxZ = part[5];
            int rotatedMinX;
            int rotatedMinZ;
            int rotatedMaxX;
            int rotatedMaxZ;

            switch (facing) {
                case NORTH -> {
                    rotatedMinX = 16 - maxZ;
                    rotatedMinZ = minX;
                    rotatedMaxX = 16 - minZ;
                    rotatedMaxZ = maxX;
                }
                case EAST -> {
                    rotatedMinX = 16 - maxX;
                    rotatedMinZ = 16 - maxZ;
                    rotatedMaxX = 16 - minX;
                    rotatedMaxZ = 16 - minZ;
                }
                case SOUTH -> {
                    rotatedMinX = minZ;
                    rotatedMinZ = 16 - maxX;
                    rotatedMaxX = maxZ;
                    rotatedMaxZ = 16 - minX;
                }
                default -> {
                    rotatedMinX = minX;
                    rotatedMinZ = minZ;
                    rotatedMaxX = maxX;
                    rotatedMaxZ = maxZ;
                }
            }

            shape = Shapes.or(shape, Block.box(rotatedMinX, minY, rotatedMinZ, rotatedMaxX, maxY, rotatedMaxZ));
        }
        return shape;
    }
}
