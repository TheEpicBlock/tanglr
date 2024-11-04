package nl.theepicblock.tanglr.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import nl.theepicblock.tanglr.Tanglr;
import nl.theepicblock.tanglr.objects.ItemDependencyComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryAdd implements Container {
    @WrapOperation(method = "hasRemainingSpaceForItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    public boolean wrapIsEqual(ItemStack stack, ItemStack other, Operation<Boolean> original) {
        // We don't want to check for our component's equality
        var tmp = stack.get(Tanglr.DEPENDENCY_COMPONENT.get());
        var tmp2 = other.get(Tanglr.DEPENDENCY_COMPONENT.get());
        stack.set(Tanglr.DEPENDENCY_COMPONENT.get(), null);
        other.set(Tanglr.DEPENDENCY_COMPONENT.get(), null);
        var v = original.call(stack, other);
        stack.set(Tanglr.DEPENDENCY_COMPONENT.get(), tmp);
        other.set(Tanglr.DEPENDENCY_COMPONENT.get(), tmp2);
        return v;
    }

    @Inject(method = "addResource(ILnet/minecraft/world/item/ItemStack;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I"))
    public void onAdd(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        var comp = stack.get(Tanglr.DEPENDENCY_COMPONENT.get());
        if (comp == null) return;
        var stackInSlot = this.getItem(slot);
        int j = this.getMaxStackSize(stackInSlot) - stackInSlot.getCount();
        int k = Math.min(stack.getCount(), j);
        var split = comp.split(k);
        var stackInSlotComp = stackInSlot.get(Tanglr.DEPENDENCY_COMPONENT.get());
        stackInSlot.set(Tanglr.DEPENDENCY_COMPONENT.get(), ItemDependencyComponent.merge(stackInSlotComp, split.getFirst()));
        stack.set(Tanglr.DEPENDENCY_COMPONENT.get(), split.getSecond());
    }
}
