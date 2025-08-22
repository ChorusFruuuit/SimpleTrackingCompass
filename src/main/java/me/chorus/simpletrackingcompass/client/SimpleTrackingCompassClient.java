package me.chorus.simpletrackingcompass.client;

import me.chorus.simpletrackingcompass.CompassHUD;
import me.chorus.simpletrackingcompass.GameMenuCompassButton;
import me.chorus.simpletrackingcompass.network.ClientNetworking;
import net.fabricmc.api.ClientModInitializer;

public class SimpleTrackingCompassClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CompassHUD.register();
        GameMenuCompassButton.register();

        ClientNetworking.init();
    }
}
