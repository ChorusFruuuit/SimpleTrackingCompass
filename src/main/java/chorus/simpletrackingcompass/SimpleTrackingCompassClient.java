package chorus.simpletrackingcompass;

import chorus.simpletrackingcompass.config.ConfigManager;
import chorus.simpletrackingcompass.hud.CompassHUD;
import chorus.simpletrackingcompass.network.ClientNetworking;
import chorus.simpletrackingcompass.screen.TargetSelectorScreenButtonAdder;
import net.fabricmc.api.ClientModInitializer;

public class SimpleTrackingCompassClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.Options options = ConfigManager.load();
        ConfigManager.SHOULD_SEND_GUIDE_MESSAGE = options.sendGuideMessage();
        ConfigManager.UPDATE_INTERVAL = Math.clamp(options.updateInterval(), 1, 40);

        ClientNetworking.init();

        CompassHUD.register();
        TargetSelectorScreenButtonAdder.register();
    }
}
