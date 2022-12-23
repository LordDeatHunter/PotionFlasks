package wraith.potion_flasks.mixin;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.potion_flasks.item.PotionFlaskItem;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(method = "getRemainingStacks", at = @At("HEAD"), cancellable = true)
    public <C extends Inventory, T extends Recipe<C>> void getRemainingStacks(RecipeType<T> recipeType, C inventory, World world, CallbackInfoReturnable<DefaultedList<ItemStack>> cir) {
        Potion potion = PotionFlaskItem.getPotionForCrafting(inventory);
        if (potion != null) {
            cir.setReturnValue(DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY));
            cir.cancel();
        }
    }


}
