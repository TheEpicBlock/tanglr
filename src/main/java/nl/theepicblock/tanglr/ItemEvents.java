package nl.theepicblock.tanglr;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import nl.theepicblock.tanglr.objects.ItemDependencyComponent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

public class ItemEvents {
    public static ItemStack onItemCrafted(ItemStack in, List<ItemStack> inputs) {
        ItemDependencyComponent component = null;
        for (var stack : inputs) {
            if (stack == null) {
                continue;
            }
            var comp = stack.get(Tanglr.DEPENDENCY_COMPONENT.get());
            if (comp != null) {
                // TODO handle crafting with multiple dependencies
                component = comp;
            }
        }

        if (component != null) {
            in.set(Tanglr.DEPENDENCY_COMPONENT.get(), component);
        }
        return in;
    }

    // TODO track smelting

    // Following events prevent players from using items that don't exist anymore

    public static void onInventoryTick(ItemStack stack, Level level, Entity entity, int inventorySlot, boolean isCurrentItem, CallbackInfo ci) {
        if (entity instanceof ServerPlayer pl && !TimeLogic.decreaseOrTrueIfImplode(stack, level.getServer())) {
            if (pl.getInventory().getItem(inventorySlot) == stack) {
                pl.getInventory().setItem(inventorySlot, ItemStack.EMPTY);
                ci.cancel();
            }
        }
    }

    @SubscribeEvent
    public static void onUse(LivingEntityUseItemEvent.Start e) {
        if (!TimeLogic.decreaseOrTrueIfImplode(e.getItem(), e.getEntity().getServer())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onUseBlock(UseItemOnBlockEvent e) {
        if (!TimeLogic.decreaseOrTrueIfImplode(e.getItemStack(), e.getLevel().getServer())) {
            if (e.getPlayer() != null) {
                if (e.getPlayer().getItemInHand(e.getHand()) == e.getItemStack()) {
                    e.getPlayer().setItemInHand(e.getHand(), ItemStack.EMPTY);
                }
            }
            e.cancelWithResult(ItemInteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void itemPickup(ItemEntityPickupEvent.Pre e) {
        if (!TimeLogic.decreaseOrTrueIfImplode(e.getItemEntity().getItem(), e.getItemEntity().getServer())) {
            e.getItemEntity().setItem(ItemStack.EMPTY);
            e.getItemEntity().discard();
            e.setCanPickup(TriState.FALSE);
        }
    }
}
