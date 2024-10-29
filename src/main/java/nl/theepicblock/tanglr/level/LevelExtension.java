package nl.theepicblock.tanglr.level;

import net.minecraft.core.BlockPos;

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
}
