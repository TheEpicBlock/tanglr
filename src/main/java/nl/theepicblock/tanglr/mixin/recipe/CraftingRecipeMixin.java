package nl.theepicblock.tanglr.mixin.recipe;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import nl.theepicblock.tanglr.ItemEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({
        ShapedRecipe.class,
        ShapelessRecipe.class,
        BannerDuplicateRecipe.class,
        ArmorDyeRecipe.class,
        DecoratedPotRecipe.class,
        FireworkRocketRecipe.class,
        FireworkStarFadeRecipe.class,
        FireworkStarRecipe.class,
        MapCloningRecipe.class,
        MapExtendingRecipe.class,
        RepairItemRecipe.class,
        ShieldDecorationRecipe.class,
        ShulkerBoxColoring.class,
        SuspiciousStewRecipe.class,
        TippedArrowRecipe.class,
})
public class CraftingRecipeMixin {
    @ModifyReturnValue(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    private ItemStack editRecipeReturn(ItemStack original, CraftingInput input, HolderLookup.Provider registries) {
        return ItemEvents.onItemCrafted(original, input.items());
    }
}
