package nl.theepicblock.tanglr.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import nl.theepicblock.tanglr.level.FutureChunkGenerator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * The chunk generation of future levels is inherently dependent on the parent level's generation.
 * This mixin should ensure that the parent's generation loop runs if ours is.
 */
@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin {
    @Mutable
    @Shadow @Final private ServerChunkCache.MainThreadExecutor mainThreadProcessor;
    @Unique private @Nullable ServerChunkCache parentCache;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;"))
    void afterInit(ServerLevel level, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer fixerUpper, StructureTemplateManager structureManager, Executor dispatcher, ChunkGenerator generator, int viewDistance, int simulationDistance, boolean sync, ChunkProgressListener progressListener, ChunkStatusUpdateListener chunkStatusListener, Supplier overworldDataStorage, CallbackInfo ci) {
        if (generator instanceof FutureChunkGenerator g) {
            this.parentCache = g.getParentLevel().getChunkSource();
//            this.mainThreadProcessor = ((ServerChunkCacheMixin)(Object)this.parentCache).mainThreadProcessor;
        }
    }

    @Inject(method = "runDistanceManagerUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;runGenerationTasks()V"))
    private void onRunGenTasks(CallbackInfoReturnable<Boolean> cir) {
        if (this.parentCache != null) {
            this.parentCache.chunkMap.runGenerationTasks();
            ((ServerChunkCacheMixin)(Object)this.parentCache).mainThreadProcessor.pollTask();
        }
    }
}
