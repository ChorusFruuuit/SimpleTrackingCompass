package net.chorus.simpletrackingcompass.screen;

import net.chorus.simpletrackingcompass.screen.widget.TexturedButton;
import net.chorus.simpletrackingcompass.util.Utils;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompassClient.*;

public final class CompassButtonFactory {
    public static TexturedButton afterScreenInit(Screen screen) {
        if (!(screen instanceof PauseScreen pauseScreen)) return null;

        Component returnButtonMessage = Component.translatable("menu.returnToGame");

        int[] returnButtonBounds = Utils.getWidgetBounds(pauseScreen, returnButtonMessage);
        return getCompassButton(returnButtonBounds, pauseScreen);
    }

    private static TexturedButton getCompassButton(int[] returnButtonBounds, Screen pauseScreen) {
        int buttonX = returnButtonBounds[0] + returnButtonBounds[2] + 4;
        int buttonY = returnButtonBounds[1];
        int buttonWidth = returnButtonBounds[3];
        int buttonHeight = returnButtonBounds[3];

        int[] compassStaticTextureSize = Utils.scaleTextureToFit(COMPASS_STATIC, returnButtonBounds[3]);

        return new TexturedButton(
            buttonX, buttonY,
            buttonWidth, buttonHeight,
            compassStaticTextureSize[0], compassStaticTextureSize[1],
            COMPASS_STATIC,
            button -> client().setScreen(new TargetSelectorScreen(pauseScreen))
        );
    }
}
