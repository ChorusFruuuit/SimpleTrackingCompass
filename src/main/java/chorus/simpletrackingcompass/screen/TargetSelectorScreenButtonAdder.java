package chorus.simpletrackingcompass.screen;

import chorus.simpletrackingcompass.screen.widget.IconButtonWidget;
import chorus.simpletrackingcompass.util.Utils;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static chorus.simpletrackingcompass.SimpleTrackingCompass.MOD_ID;

public class TargetSelectorScreenButtonAdder {
    private static final Identifier ICON = Identifier.of(
        MOD_ID, "textures/gui/compass_32.png"
    );

    public static void register() {
        ScreenEvents.AFTER_INIT.register(
            (client, screen, sw, sh) -> {
                if (!(screen instanceof GameMenuScreen)) return;

                Text returnButtonLabel = Text.translatable("menu.returnToGame");

                int[] buttonBounds = Utils.getButtonBounds(screen, returnButtonLabel);
                if (buttonBounds == null) buttonBounds = new int[]{0, 0, 0, 0};

                IconButtonWidget settingsButton = getSettingsButton(client, buttonBounds);
                Screens.getButtons(screen).add(settingsButton);
            }
        );
    }

    private static IconButtonWidget getSettingsButton(MinecraftClient client, int[] buttonBounds) {
        int iconX = buttonBounds[0] + buttonBounds[2] + 6;
        int iconY = buttonBounds[1];
        int iconWidth = buttonBounds[3];
        int iconHeight = buttonBounds[3];

        GameMenuScreen gameMenuScreen = new GameMenuScreen(true);

        return new IconButtonWidget(
            iconX, iconY,
            iconWidth, iconHeight,
            ICON,
            btn ->
                client.setScreen(new TargetSelectorScreen(gameMenuScreen))
        );
    }
}