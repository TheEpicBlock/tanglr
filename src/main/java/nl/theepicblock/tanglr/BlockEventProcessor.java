package nl.theepicblock.tanglr;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import nl.theepicblock.tanglr.level.FutureServerLevel;
import nl.theepicblock.tanglr.level.LevelManager;

public class BlockEventProcessor {
    /**
     * @param level The level where the block change occured
     * @param location the block position that was changed
     * @param newState the new state of the block
     */
    public static void enqueueBlockChange(ServerLevel level, BlockPos location, BlockState newState) {
        if (level instanceof FutureServerLevel futureLevel) {
            // Changing blocks in the future does not yet do anything
            return;
        } else {
            // The change happened in the present. Replicate it in the future
            var futureLevel = LevelManager.toFuture(level);
            futureLevel.setBlock(location, newState, Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
        }
    }
}
