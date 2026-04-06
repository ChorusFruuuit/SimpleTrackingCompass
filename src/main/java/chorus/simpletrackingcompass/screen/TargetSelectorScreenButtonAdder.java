package chorus.simpletrackingcompass.screen;

import chorus.simpletrackingcompass.screen.widget.IconButtonWidget;
import chorus.simpletrackingcompass.util.Utils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

public class TargetSelectorScreenButtonAdder {
    private static final Identifier ICON = Identifier.fromNamespaceAndPath(
        MOD_ID, "textures/gui/compass_32.png"
    );

    public static void register() {
        ScreenEvents.AFTER_INIT.register(
            (client, screen, _, _) -> {
                if (!(screen instanceof PauseScreen)) return;

                Component returnButtonLabel = Component.translatable("menu.returnToGame");

                int[] buttonBounds = Utils.getButtonBounds(screen, returnButtonLabel);
                if (buttonBounds == null) buttonBounds = new int[]{0, 0, 0, 0};

                IconButtonWidget settingsButton = getSettingsButton(client, buttonBounds);
                Screens.getWidgets(screen).add(settingsButton);
            }
        );
    }

    private static IconButtonWidget getSettingsButton(Minecraft client, int[] buttonBounds) {
        int iconX = buttonBounds[0] + buttonBounds[2] + 6;
        int iconY = buttonBounds[1];
        int iconWidth = buttonBounds[3];
        int iconHeight = buttonBounds[3];

        PauseScreen gameMenuScreen = new PauseScreen(true);

        return new IconButtonWidget(
            iconX, iconY,
            iconWidth, iconHeight,
            ICON,
            _ ->
                client.setScreen(new TargetSelectorScreen(gameMenuScreen))
        );
    }
}