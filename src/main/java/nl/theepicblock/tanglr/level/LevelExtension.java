package nl.theepicblock.tanglr.level;

import net.minecraft.core.BlockPos;

import java.util.HashMap;

public interface LevelExtension {
    /**
     * Gets info for looking up dependants
     */
    Long tanglr$getInfoId(BlockPos pos);

    void tanglr$setInfoId(BlockPos pos, long id);

    /**
     * Gets info for looking up dependencies
     */
    Long tanglr$getDependencyId(BlockPos pos);

    void tanglr$setDependencyId(BlockPos pos, long id);


    // DEBUG ONLY
    HashMap<BlockPos, Long> tanglr$getInternalInfo();
    HashMap<BlockPos, Long> tanglr$getInternalDependency();
}
