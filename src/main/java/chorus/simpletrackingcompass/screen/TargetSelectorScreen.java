package chorus.simpletrackingcompass.screen;

import com.mojang.authlib.GameProfile;
import chorus.simpletrackingcompass.config.ConfigManager;
import chorus.simpletrackingcompass.hud.CompassHUD;
import chorus.simpletrackingcompass.screen.widget.RangeSliderWidget;
import chorus.simpletrackingcompass.util.Utils;
import chorus.simpletrackingcompass.screen.widget.ScrollableList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;

public class TargetSelectorScreen extends Screen {
    private final Screen parent;
    private ScrollableList playerList;

    private final String suffix = " (You)";

    // Constructor

    public TargetSelectorScreen(Screen parent) {
        super(Component.literal("Select a target"));
        this.parent = parent;
    }

    // Init method. Called once when the screen is created

    @Override
    protected void init() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        // 'Done' button

        OptionsScreen optionsScreen = new OptionsScreen(
            this,
            client.options,
            true
        );

        optionsScreen.init(
            this.width,
            this.height
        );

        Component doneButtonLabel = CommonComponents.GUI_DONE;

        int[] doneBounds = Utils.getButtonBounds(optionsScreen, doneButtonLabel);
        if (doneBounds == null) doneBounds = new int[]{0, 0, 0, 0};

        Button doneButton = Button
            .builder(doneButtonLabel, _ -> onClose())
            .bounds(doneBounds[0], doneBounds[1], doneBounds[2], doneBounds[3])
            .build();

        addRenderableWidget(doneButton);

        // 'Hide Compass HUD' button

        int toggleY = (doneBounds[1] - 50) + (50 / 2 - doneBounds[3] / 2);

        CycleButton<Boolean> toggleButton = CycleButton
            .onOffBuilder(CompassHUD.isCompassHUDHidden)
            .create(
                doneBounds[0], toggleY,
                doneBounds[2], doneBounds[3],
                Component.literal("Hide Compass HUD"),
                (_, value) ->
                    CompassHUD.isCompassHUDHidden = value
            );

        addRenderableWidget(toggleButton);

        // Slider for configuring how often is the target player position updated

        RangeSliderWidget compassUpdateSlider = new RangeSliderWidget(
            doneBounds[0], doneBounds[1] - 200,
            doneBounds[2], 20,
            1, 40, ConfigManager.UPDATE_INTERVAL,
            value -> ConfigManager.UPDATE_INTERVAL = value
        );

        compassUpdateSlider.setLabelAndTooltip(
            Component.literal("Compass Update Delay"),
            Component.literal("How often is the target player position updated (in ticks) when the target player is not within your render distance. Lower values - faster updates. Recommended: 10. This does not affect compass needle update frequency.")
        );

        addRenderableWidget(compassUpdateSlider);

        // Scrollable list of players

        this.playerList = new ScrollableList(
            doneBounds[0], doneBounds[1] - 150,
            doneBounds[2], 100,
            10, 20,
            false
        );

        // Add the client.player to the list
        Player player = client.player;
        playerList.addEntry(player.getName().getString() + suffix);

        // Create the list of all players
        Collection<PlayerInfo> players =
            client.getConnection() != null
                ? client.getConnection().getOnlinePlayers()
                : List.of();

        // Add players to the list
        for (PlayerInfo entry : players) {
            GameProfile profile = entry.getProfile();
            String name = profile.name();

            // Skip the client.player because it has already been added
            if (profile.id().equals(player.getUUID()) || name.trim().isEmpty())
                continue;

            // Add the player to the list
            playerList.addEntry(name);
        }

        addRenderableWidget(playerList);
    }

    // Render method. Called every frame

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        super.extractRenderState(context, mouseX, mouseY, deltaTicks);

        if (Minecraft.getInstance().getWindow().isFullscreen()) {
            context.centeredText(
                this.font,
                this.title,
                this.width / 2,
                20,
                0xFFFFFFFF
            );
        }
    }

    // Overriding close method

    @Override
    public void onClose() {
        Minecraft client = Minecraft.getInstance();

        Integer index = playerList.getSelectedIndex();
        String selectedPlayerName = (index != null) ? playerList.getEntry(playerList.getElements().indexOf(playerList.getVisibleEntry(index))) : null;

        UUID selectedPlayerUuid = null;

        if (selectedPlayerName != null && selectedPlayerName.endsWith(suffix)) {
            selectedPlayerName = selectedPlayerName.replace(suffix, "");
            selectedPlayerUuid = (client.player != null) ? client.player.getUUID() : null;
        } else if (selectedPlayerName != null && client.getConnection() != null) {
            for (PlayerInfo player : client.getConnection().getOnlinePlayers()) {
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

// TODO: Add a button to filter out the players who are not within the render distance