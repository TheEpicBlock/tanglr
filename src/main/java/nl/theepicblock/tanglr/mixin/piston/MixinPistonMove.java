package nl.theepicblock.tanglr.mixin.piston;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import nl.theepicblock.tanglr.PistonBlockEntityDuck;
import nl.theepicblock.tanglr.level.LevelExtension;
import nl.theepicblock.tanglr.objects.PositionInfoHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonBaseBlock.class)
public class MixinPistonMove {
    @ModifyReceiver(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;", ordinal = 1))
    private BlockPos preSet(BlockPos original, Direction direction, Level level, BlockPos pos, Direction facing, boolean extending, @Share("stuffs") LocalRef<Pair<Long,Long>> stuffs) {
        stuffs.set(null);
        if (level.isClientSide()) return original;
        var ext = (LevelExtension)level;
        var dep = ext.tanglr$getDependencyId(original);
        var holder = PositionInfoHolder.get(level.getServer());
        if (dep != null) {
            stuffs.set(new Pair<>(
                    dep,
                    holder.lookup(dep).generation
            ));
        }
        return original;
    }

    @ModifyExpressionValue(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/minecraft/world/level/block/entity/BlockEntity;", ordinal = 0))
    private BlockEntity postSet(BlockEntity original, @Share("stuffs") LocalRef<Pair<Long,Long>> stuffs) {
        if (stuffs.get() != null) {
            ((PistonBlockEntityDuck)original).tangrl$set(stuffs.get().getFirst(), stuffs.get().getSecond());
        }
        return original;
    }
}