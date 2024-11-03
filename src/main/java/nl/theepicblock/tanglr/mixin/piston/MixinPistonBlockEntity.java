package nl.theepicblock.tanglr.mixin.piston;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import nl.theepicblock.tanglr.PistonBlockEntityDuck;
import nl.theepicblock.tanglr.TimeLogic;
import nl.theepicblock.tanglr.level.LevelExtension;
import nl.theepicblock.tanglr.objects.PositionInfoHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonMovingBlockEntity.class)
public abstract class MixinPistonBlockEntity extends BlockEntity implements PistonBlockEntityDuck {
    @Shadow private BlockState movedState;
    @Unique private long dependency = 0;
    @Unique private long generation = -1;

    public MixinPistonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void tangrl$set(long dependency, long generation) {
        this.dependency = dependency;
        this.generation = generation;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static void onSet(Level level, BlockPos pos, BlockState state, PistonMovingBlockEntity blockEntity, CallbackInfo ci) {
        var me = ((MixinPistonBlockEntity)(Object)blockEntity);
        if (me.generation != -1) {
            var ext = (LevelExtension)me.level;
            var server = me.level.getServer();
            if (server == null) return;
            var holder = PositionInfoHolder.get(server);
            if (me.dependency != TimeLogic.NOT_DEPENDENT && holder.lookup(me.dependency).generation != me.generation) {
                me.level.setBlock(me.worldPosition, Blocks.AIR.defaultBlockState(), 3);
                me.movedState = Blocks.AIR.defaultBlockState();
            } else {
                // Move the dependency
                TimeLogic.setDependency(me.dependency, me.level, me.worldPosition);
            }
        }
    }
}
