package wraith.potion_flasks;

import net.fabricmc.api.ClientModInitializer;
import wraith.potion_flasks.registry.PotionFlaskColorProviderRegistry;
import wraith.potion_flasks.registry.PotionFlaskModelPredicateRegistry;

public class PotionFlasksClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PotionFlaskColorProviderRegistry.register();
        PotionFlaskModelPredicateRegistry.register();
    }

}
