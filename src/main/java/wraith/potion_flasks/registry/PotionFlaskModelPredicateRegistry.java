package wraith.potion_flasks.registry;

import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.nbt.NbtCompound;
import wraith.potion_flasks.PotionFlasks;
import wraith.potion_flasks.item.PotionFlaskItem;

public final class PotionFlaskModelPredicateRegistry {

    public static void register() {
        FabricModelPredicateProviderRegistry.register(PotionFlasks.ID("filled_amount"),
                (itemStack, clientWorld, livingEntity) -> {
                    if (itemStack.isEmpty() || !(itemStack.getItem() instanceof PotionFlaskItem)) {
                        return 0f;
                    }
                    NbtCompound tag = itemStack.getTag();
                    if (tag == null || !tag.contains("filled_amount")) {
                        return ((PotionFlaskItem)itemStack.getItem()).getCapacity();
                    }
                    return tag.getInt("filled_amount");
                }
        );
    }

}
