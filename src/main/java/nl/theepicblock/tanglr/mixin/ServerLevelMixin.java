package nl.theepicblock.tanglr.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import nl.theepicblock.tanglr.level.LevelExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements LevelExtension {
    @Unique private HashMap<BlockPos, Long> infoIds = new HashMap<>();
    @Unique private HashMap<BlockPos, Long> depIds = new HashMap<>();

    @Override
    public Long tanglr$getInfoId(BlockPos pos) {
        return this.infoIds.get(pos);
    }

    @Override
    public void tanglr$setInfoId(BlockPos pos, long id) {
        this.infoIds.put(pos, id);
    }

    @Override
    public Long tanglr$getDependencyId(BlockPos pos) {
        return this.depIds.get(pos);
    }

    @Override
    public void tanglr$setDependencyId(BlockPos pos, Long id) {
        if (id == null) {
            this.depIds.remove(pos);
        } else {
            this.depIds.put(pos, id);
        }
    }

    @Override
    public HashMap<BlockPos,Long> tanglr$getInternalInfo() {
        return this.infoIds;
    }

    @Override
    public HashMap<BlockPos,Long> tanglr$getInternalDependency() {
        return this.depIds;
    }
}
