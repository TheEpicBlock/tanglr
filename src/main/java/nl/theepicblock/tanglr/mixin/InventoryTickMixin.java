package nl.theepicblock.tanglr.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import nl.theepicblock.tanglr.ItemEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class InventoryTickMixin {
    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    private void onTick(Level level, Entity entity, int inventorySlot, boolean isCurrentItem, CallbackInfo ci) {
        ItemEvents.onInventoryTick(((ItemStack)(Object)this), level, entity, inventorySlot, isCurrentItem, ci);
    }
}
