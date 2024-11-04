package nl.theepicblock.tanglr.mixin.recipe;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import nl.theepicblock.tanglr.ItemEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin({AbstractCookingRecipe.class, SingleItemRecipe.class})
public class SingleInputRecipesMixin {
    @ModifyReturnValue(method = "assemble(Lnet/minecraft/world/item/crafting/SingleRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    private ItemStack editRecipeReturn(ItemStack original, SingleRecipeInput input, HolderLookup.Provider registries) {
        return ItemEvents.onItemCrafted(original, List.of(input.item()));
    }
}
