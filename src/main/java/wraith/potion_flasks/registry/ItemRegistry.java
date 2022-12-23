package wraith.potion_flasks.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import wraith.potion_flasks.PotionFlasks;
import wraith.potion_flasks.PotionFlasksGroups;
import wraith.potion_flasks.item.FlaskItem;
import wraith.potion_flasks.item.PotionFlaskItem;

public final class ItemRegistry {

    public static final Item POTION_FLASK_ICON = new Item(new FabricItemSettings().maxCount(1).fireproof());
    public static final FlaskItem LARGE_FLASK = new FlaskItem(new FabricItemSettings().group(PotionFlasksGroups.POTION_FLASKS).maxCount(4));
    public static final FlaskItem FLASK = new FlaskItem(new FabricItemSettings().group(PotionFlasksGroups.POTION_FLASKS).maxCount(16));

    public static final PotionFlaskItem POTION_FLASK = new PotionFlaskItem(new FabricItemSettings().group(PotionFlasksGroups.POTION_FLASKS).maxCount(1), 3);
    public static final PotionFlaskItem LARGE_POTION_FLASK = new PotionFlaskItem(new FabricItemSettings().group(PotionFlasksGroups.POTION_FLASKS).maxCount(1), 6);

    public static void register() {
        Registry.register(Registry.ITEM, PotionFlasks.ID("potion_flask"), POTION_FLASK);
        Registry.register(Registry.ITEM, PotionFlasks.ID("large_potion_flask"), LARGE_POTION_FLASK);
        Registry.register(Registry.ITEM, PotionFlasks.ID("potion_flasks_icon"), POTION_FLASK_ICON);
        Registry.register(Registry.ITEM, PotionFlasks.ID("flask"), FLASK);
        Registry.register(Registry.ITEM, PotionFlasks.ID("large_flask"), LARGE_FLASK);
    }

    public static FlaskItem getFlask(PotionFlaskItem item) {
        return item.getCapacity() == 3 ? FLASK : LARGE_FLASK;
    }

    public static PotionFlaskItem getPotionFlask(FlaskItem item) {
        return item == FLASK ? POTION_FLASK : LARGE_POTION_FLASK;
    }

}
