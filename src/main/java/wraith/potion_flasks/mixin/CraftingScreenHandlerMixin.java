package wraith.potion_flasks.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wraith.potion_flasks.item.PotionFlaskItem;
import wraith.potion_flasks.registry.ItemRegistry;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private static void updateResult(int syncId, World world, PlayerEntity player, CraftingInventory craftingInventory, CraftingResultInventory resultInventory, CallbackInfo ci) {
        if (world.isClient) {
            return;
        }
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
        Potion potion = PotionFlaskItem.getPotionForCrafting(craftingInventory);
        if (potion == null) {
            return;
        }
        ItemStack itemStack = new ItemStack(ItemRegistry.POTION_FLASK);
        itemStack.getOrCreateTag().putInt("filled_amount", 5);
        PotionUtil.setPotion(itemStack, potion);
        resultInventory.setStack(0, itemStack);

        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, 0, itemStack));
        ci.cancel();
    }

}
