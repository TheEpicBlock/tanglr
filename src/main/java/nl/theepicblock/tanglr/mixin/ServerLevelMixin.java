package nl.theepicblock.tanglr.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import nl.theepicblock.tanglr.level.LevelExtension;
import nl.theepicblock.tanglr.level.TimeDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements LevelExtension {
    @Shadow public abstract DimensionDataStorage getDataStorage();

    @Unique private TimeDataStorage storage;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(MinecraftServer server, Executor dispatcher, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey dimension, LevelStem levelStem, ChunkProgressListener progressListener, boolean isDebug, long biomeZoomSeed, List customSpawners, boolean tickTime, RandomSequences randomSequences, CallbackInfo ci) {
        this.storage = this.getDataStorage().get(TimeDataStorage.factory(), "tanglr_world_attachment");
    }

    @Override
    public Long tanglr$getInfoId(BlockPos pos) {
        return this.storage.infoIds.get(pos);
    }

    @Override
    public void tanglr$setInfoId(BlockPos pos, long id) {
        this.storage.infoIds.put(pos, id);
    }

    @Override
    public Long tanglr$getDependencyId(BlockPos pos) {
        return this.storage.depIds.get(pos);
    }

    @Override
    public void tanglr$setDependencyId(BlockPos pos, Long id) {
        if (id == null) {
            this.storage.depIds.remove(pos);
        } else {
            this.storage.depIds.put(pos, id);
        }
    }

    @Override
    public HashMap<BlockPos,Long> tanglr$getInternalInfo() {
        return this.storage.infoIds;
    }

    @Override
    public HashMap<BlockPos,Long> tanglr$getInternalDependency() {
        return this.storage.depIds;
    }
}
