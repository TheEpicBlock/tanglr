package nl.theepicblock.tanglr.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.ticks.TickPriority;
import nl.theepicblock.tanglr.level.FutureServerLevel;
import nl.theepicblock.tanglr.level.LevelManager;

public class DelayedRepeaterBlock extends DiodeBlock {
    public static final BooleanProperty OUTPUTTING = BooleanProperty.create("outputting");

    public DelayedRepeaterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(POWERED, false)
                        .setValue(OUTPUTTING, false));
    }

    @Override
    protected MapCodec<? extends DiodeBlock> codec() {
        return null;
    }

    @Override
    protected int getDelay(BlockState blockState) {
        return 0;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, OUTPUTTING);
    }

    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (!(Boolean)blockState.getValue(OUTPUTTING)) {
            return 0;
        } else {
            return blockState.getValue(FACING) == side ? this.getOutputSignal(blockAccess, pos, blockState) : 0;
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.isLocked(level, pos, state)) {
            boolean alrPowered = state.getValue(POWERED);
            boolean shouldPowered = this.shouldTurnOn(level, pos, state);
            if (alrPowered && !shouldPowered) {
                level.setBlock(pos, state.setValue(POWERED, false).setValue(OUTPUTTING, false), 2);
            } else if (!alrPowered) {
                level.setBlock(pos, state.setValue(POWERED, true).setValue(OUTPUTTING, false), 2);
                if (!shouldPowered) {
                    level.scheduleTick(pos, this, this.getDelay(state), TickPriority.VERY_HIGH);
                }
                if (!(level instanceof FutureServerLevel)) {
                    var future = LevelManager.toFuture(level);
                    if (future != null) {
                        future.setBlock(pos, state.setValue(POWERED, true).setValue(OUTPUTTING, true), 2);
                        future.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(pos), 3, pos);
                        if (!shouldPowered) {
                            future.scheduleTick(pos, this, this.getDelay(state), TickPriority.VERY_HIGH);
                        }
                    }
                }
            }
        }
    }
}
