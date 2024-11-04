package nl.theepicblock.tanglr.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import nl.theepicblock.tanglr.TimeLogic;
import nl.theepicblock.tanglr.level.FutureServerLevel;
import nl.theepicblock.tanglr.level.LevelExtension;
import nl.theepicblock.tanglr.level.LevelManager;
import nl.theepicblock.tanglr.objects.PositionInfoHolder;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED;

public class TimeMoverBlock extends DirectionalBlock {
    public TimeMoverBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return null;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean flag = state.getValue(POWERED);
            if (flag != level.hasNeighborSignal(pos)) {
                if (flag) {
                    level.scheduleTick(pos, this, 2);
                } else {
                    if (level instanceof ServerLevel sl) {
                        timeMove(pos.relative(state.getValue(FACING)), sl);
                    }
                    level.setBlock(pos, state.cycle(POWERED), 2);
                }
            }
        }

    }

    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(POWERED), 2);
        }
    }

    protected void timeMove(BlockPos pos, ServerLevel level) {
        if (level instanceof FutureServerLevel) {
            var present = LevelManager.toPresent(level);
            if (present.getBlockState(pos).isAir()) {
                var stateToMove = level.getBlockState(pos);
                if (stateToMove.isAir()) return;
                present.setBlock(pos, stateToMove, Block.UPDATE_ALL);
                var futureExt = (LevelExtension)level;
                var dep = futureExt.tanglr$getDependencyId(pos);
                if (dep == null || dep == TimeLogic.NOT_DEPENDENT) {
                    return;
                }
                present.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(pos), 3, pos);
                TimeLogic.unDepend(level, pos);
                TimeLogic.setDependency(dep, present, pos);
                // The future block now implicitly depends on the block in the past
            }
        } else {
            // TODO
        }
    }



    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
    }
}
