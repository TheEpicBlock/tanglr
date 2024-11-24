package nl.theepicblock.tanglr.level;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

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

    void tanglr$setDependencyId(BlockPos pos, Long id);

    BoundingBox tanglr$getActivationBox();

    // DEBUG ONLY
    Object2LongMap<BlockPos> tanglr$getInternalInfo();
    Object2LongMap<BlockPos> tanglr$getInternalDependency();
}
