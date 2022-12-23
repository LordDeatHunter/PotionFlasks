package wraith.potion_flasks;

import wraith.potion_flasks.registry.ItemRegistry;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class PotionFlasksGroups {

    public static final ItemGroup POTION_FLASKS = FabricItemGroupBuilder.create(PotionFlasks.ID("potion_flasks")).icon(() -> new ItemStack(ItemRegistry.POTION_FLASK_ICON)).build();

}
