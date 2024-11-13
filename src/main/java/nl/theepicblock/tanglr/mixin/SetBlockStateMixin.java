package nl.theepicblock.tanglr.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import nl.theepicblock.tanglr.TimeLogic;
import nl.theepicblock.tanglr.level.LevelChunkExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public class SetBlockStateMixin implements LevelChunkExtension {
    @Shadow @Final Level level;

    @Unique private boolean tanglr$isGenerating;

    @WrapOperation(method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState onBlockChanged(LevelChunkSection instance, int x, int y, int z, BlockState state, Operation<BlockState> original, BlockPos pos, BlockState state2, boolean isMoving) {
        var result = original.call(instance, x, y, z, state);

        if (((LevelChunkExtension) this).tanglr$isGenerating()) return result;

        if (this.level instanceof ServerLevel sl) {
            TimeLogic.enqueueBlockChange(sl, pos, state, result);
        }
        return result;
    }

    @Inject(method = "postProcessGeneration", at = @At("HEAD"))
    private void generationPostProcessingStart(CallbackInfo ci) {
        tanglr$isGenerating = true;
    }

    @Inject(method = "postProcessGeneration", at = @At("RETURN"))
    private void generationPostProcessingEnd(CallbackInfo ci) {
        tanglr$isGenerating = false;
    }

    @Override
    public boolean tanglr$isGenerating() {
        return tanglr$isGenerating;
    }
}
