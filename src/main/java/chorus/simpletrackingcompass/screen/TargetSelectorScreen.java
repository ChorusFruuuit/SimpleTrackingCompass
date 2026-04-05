package chorus.simpletrackingcompass.screen;

import com.mojang.authlib.GameProfile;
import chorus.simpletrackingcompass.config.ConfigManager;
import chorus.simpletrackingcompass.hud.CompassHUD;
import chorus.simpletrackingcompass.screen.widget.RangeSliderWidget;
import chorus.simpletrackingcompass.util.Utils;
import chorus.simpletrackingcompass.screen.widget.ScrollableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class TargetSelectorScreen extends Screen {
    private final Screen parent;
    private ScrollableList playerList;

    private final String suffix = " (You)";

    // Constructor

    public TargetSelectorScreen(Screen parent) {
        super(Text.literal("Select a target"));
        this.parent = parent;
    }

    // Init method. Called once when the screen is created

    @Override
    protected void init() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // 'Done' button

        OptionsScreen optionsScreen = new OptionsScreen(
            client.currentScreen,
            client.options
        );

        optionsScreen.init(
            this.width,
            this.height
        );

        Text doneButtonLabel = ScreenTexts.DONE;

        int[] doneBounds = Utils.getButtonBounds(optionsScreen, doneButtonLabel);
        if (doneBounds == null) doneBounds = new int[]{0, 0, 0, 0};

        ButtonWidget doneButton = ButtonWidget
            .builder(doneButtonLabel, btn -> close())
            .dimensions(doneBounds[0], doneBounds[1], doneBounds[2], doneBounds[3])
            .build();

        addDrawableChild(doneButton);

        // 'Hide Compass HUD' button

        int toggleY = (doneBounds[1] - 50) + (50 / 2 - doneBounds[3] / 2);

        CyclingButtonWidget<Boolean> toggleButton = CyclingButtonWidget
            .onOffBuilder(CompassHUD.isCompassHUDHidden)
            .build(
                doneBounds[0], toggleY,
                doneBounds[2], doneBounds[3],
                Text.literal("Hide Compass HUD"),
                (btn, value) ->
                    CompassHUD.isCompassHUDHidden = value
            );

        addDrawableChild(toggleButton);

        // Slider for configuring how often is the compass needle updated

        RangeSliderWidget compassUpdateSlider = new RangeSliderWidget(
            doneBounds[0], doneBounds[1] - 200,
            doneBounds[2], 20,
            1, 40, ConfigManager.UPDATE_INTERVAL,
            value -> ConfigManager.UPDATE_INTERVAL = value
        );

        compassUpdateSlider.setLabelAndTooltip(
            Text.literal("Compass Update Delay"),
            Text.literal("How often is the compass needle updated (in ticks).\nLower = faster updates.")
        );

        addDrawableChild(compassUpdateSlider);

        // Scrollable list of players

        this.playerList = new ScrollableList(
            doneBounds[0], doneBounds[1] - 150,
            doneBounds[2], 100,
            10, 20,
            false
        );

        // Add the client.player to the list
        PlayerEntity player = client.player;
        playerList.addEntry(player.getName().getString() + suffix);

        // Create the list of all players
        Collection<PlayerListEntry> players =
            client.getNetworkHandler() != null
                ? client.getNetworkHandler().getPlayerList()
                : List.of();

        // Add players to the list
        for (PlayerListEntry entry : players) {
            GameProfile profile = entry.getProfile();
            String name = profile.name();

            // Skip the client.player because it has already been added
            if (profile.id().equals(player.getUuid()) || name.trim().isEmpty())
                continue;

            // Add the player to the list
            playerList.addEntry(name);
        }

        addDrawableChild(playerList);
    }

    // Render method. Called every frame

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        if (MinecraftClient.getInstance().getWindow().isFullscreen()) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                20,
                0xFFFFFFFF
            );
        }
    }

    // Overriding close method

    @Override
    public void close() {
        MinecraftClient client = MinecraftClient.getInstance();

        Integer index = playerList.getSelectedIndex();
        String selectedPlayerName = (index != null) ? playerList.getEntry(playerList.getElements().indexOf(playerList.getVisibleEntry(index))) : null;

        UUID selectedPlayerUuid = null;

        if (selectedPlayerName != null && selectedPlayerName.endsWith(suffix)) {
            selectedPlayerName = selectedPlayerName.replace(suffix, "");
            selectedPlayerUuid = (client.player != null) ? client.player.getUuid() : null;
        } else if (selectedPlayerName != null && client.getNetworkHandler() != null) {
            for (PlayerListEntry player : client.getNetworkHandler().getPlayerList()) {
                if (player.getProfile().name().equals(selectedPlayerName)) {
                    selectedPlayerUuid = player.getProfile().id();
                    break;
                }
            }
        }

        CompassHUD.setTrackedPlayer(selectedPlayerUuid, selectedPlayerName, false);

        client.setScreen(this.parent);
    }
}