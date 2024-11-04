package nl.theepicblock.tanglr.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import nl.theepicblock.tanglr.Tanglr;
import nl.theepicblock.tanglr.TimeLogic;
import nl.theepicblock.tanglr.level.FutureServerLevel;
import nl.theepicblock.tanglr.level.LevelExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static nl.theepicblock.tanglr.TimeLogic.NOT_DEPENDENT;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/core/Holder;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/gameevent/GameEvent$Context;)V"))
    public void onSuccessfulPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        var stack = context.getItemInHand();
        var comp = stack.get(Tanglr.DEPENDENCY_COMPONENT.get());
        if (comp != null) {
            var pop = comp.popSpecialOne();
            if (pop == null) return; // Uhhh, oh no
            var dep = pop.getSecond();
            TimeLogic.setDependency(dep.dependency(), context.getLevel(), context.getClickedPos());
            stack.set(Tanglr.DEPENDENCY_COMPONENT.get(), pop.getFirst());
        } else {
            if (context.getLevel() instanceof FutureServerLevel) {
                // In the future we have to explicitly undeclare dependencies
                ((LevelExtension)context.getLevel()).tanglr$setDependencyId(context.getClickedPos(), NOT_DEPENDENT);
            }
        }
    }
}
