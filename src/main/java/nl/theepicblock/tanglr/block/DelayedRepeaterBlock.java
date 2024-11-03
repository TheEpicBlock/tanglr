package nl.theepicblock.tanglr.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.ticks.TickPriority;
import nl.theepicblock.tanglr.level.FutureServerLevel;
import nl.theepicblock.tanglr.level.LevelManager;

public class DelayedRepeaterBlock extends DiodeBlock {
    public DelayedRepeaterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends DiodeBlock> codec() {
        return null;
    }

    @Override
    protected int getDelay(BlockState blockState) {
        return 0;
    }

    @Override
    protected boolean shouldTurnOn(Level level, BlockPos pos, BlockState state) {
        return wouldFutureTurnOn(level, pos, state) && (level instanceof FutureServerLevel);
    }

    protected boolean wouldFutureTurnOn(Level level, BlockPos pos, BlockState state) {
        return super.shouldTurnOn(level, pos, state);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    protected void onTickStuffs(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide && !(level instanceof FutureServerLevel) && this.wouldFutureTurnOn(level, pos, state)) {
            var future = LevelManager.toFuture((ServerLevel)level);
            future.scheduleTick(pos, this, this.getDelay(state), TickPriority.VERY_HIGH);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        onTickStuffs(level, pos, state);
        super.tick(state, level, pos, random);
    }

    @Override
    protected void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state) {
        onTickStuffs(level, pos, state);
        super.checkTickOnNeighbor(level, pos, state);
    }
}
