package wraith.potion_flasks.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.potion.PotionUtil;

@Environment(EnvType.CLIENT)
public final class PotionFlaskColorProviderRegistry {

    public static void register() {
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex > 0 ? -1 : PotionUtil.getColor(stack), ItemRegistry.POTION_FLASK);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex > 0 ? -1 : PotionUtil.getColor(stack), ItemRegistry.LARGE_POTION_FLASK);
    }

}
