package wraith.potion_flasks;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wraith.potion_flasks.registry.ItemRegistry;

public class PotionFlasks implements ModInitializer {

    public static final String MOD_ID = "potion_flasks";
    public static final Logger LOGGER = LogManager.getLogger();

    public static Identifier ID(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Loading [Potion Flasks]");
        ItemRegistry.register();
        LOGGER.info("[Potion Flasks] has successfully been loaded!");
    }

}
