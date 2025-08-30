package me.chorus.simpletrackingcompass;

import me.chorus.simpletrackingcompass.hud.CompassHUD;
import me.chorus.simpletrackingcompass.network.ClientNetworking;
import me.chorus.simpletrackingcompass.screen.widget.GameMenuCompassButton;
import net.fabricmc.api.ClientModInitializer;

public class SimpleTrackingCompassClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CompassHUD.register();
        GameMenuCompassButton.register();

        ClientNetworking.init();
    }
}
