package nl.theepicblock.tanglr.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import nl.theepicblock.tanglr.level.LevelManager;
import nl.theepicblock.tanglr.level.ServerParameters;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow @Final private Executor executor;

    @Shadow @Final protected LevelStorageSource.LevelStorageAccess storageSource;

    @Shadow @Final private Map<ResourceKey<Level>,ServerLevel> levels;

    @Inject(method = "createLevels", at = @At("TAIL"))
    public void setupFutureLevels(ChunkProgressListener listener, CallbackInfo ci) {
        var params = new ServerParameters((MinecraftServer)(Object)this, this.executor, this.storageSource, listener);
        var futureLevels = LevelManager.createFutureLevels(this.levels, params);
        this.levels.putAll(futureLevels);
        futureLevels.values().forEach(futureLevel -> {
            NeoForge.EVENT_BUS.post(new LevelEvent.Load(futureLevel));
        });
    }
}
