package me.chorus.simpletrackingcompass;

import me.chorus.simpletrackingcompass.mixin.ScreenInvoker;
import me.chorus.simpletrackingcompass.util.ModUtils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.util.Identifier;

import static me.chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

public class GameMenuCompassButton {
    private static final Identifier ICON = Identifier.of(
            MOD_ID,"textures/gui/compass_404.png");

    public static void register() {
        ScreenEvents.AFTER_INIT.register(
            (client, screen, a, b) -> {
                if (!(screen instanceof GameMenuScreen)) return;

                int[] buttonPos = ModUtils.getButtonPosition(
                        screen,
                        "menu.returnToGame"
                );

                int iconX = buttonPos[0] + buttonPos[2] + 6;
                int iconY = buttonPos[1];
                int iconWidth = buttonPos[3];
                int iconHeight = buttonPos[3];

                GameMenuScreen gameMenuScreen = new GameMenuScreen(true);

                IconButtonWidget openCompassSettings = new IconButtonWidget(
                        iconX, iconY,
                        iconWidth, iconHeight,
                        btn ->
                                client.setScreen(new ModSettingsScreen(gameMenuScreen)),
                        ICON
                );

                ((ScreenInvoker) screen).invokeAddDrawableChild(openCompassSettings);
            }
        );
    }
}