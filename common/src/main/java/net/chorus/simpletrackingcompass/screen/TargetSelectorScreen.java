package net.chorus.simpletrackingcompass.screen;

import com.mojang.authlib.GameProfile;
import net.chorus.simpletrackingcompass.CompassTracker;
import net.chorus.simpletrackingcompass.config.ConfigManager;
import net.chorus.simpletrackingcompass.network.ClientNetworking;
import net.chorus.simpletrackingcompass.network.DestinationStorage;
import net.chorus.simpletrackingcompass.network.PacketDestination;
import net.chorus.simpletrackingcompass.network.packet.PlayerPositionRequest;
import net.chorus.simpletrackingcompass.network.packet.PlayerPositionResponse;
import net.chorus.simpletrackingcompass.screen.widget.RangeSlider;
import net.chorus.simpletrackingcompass.screen.widget.ScrollableList;
import net.chorus.simpletrackingcompass.screen.widget.TexturedButton;
import net.chorus.simpletrackingcompass.util.PlayerUtils;
import net.chorus.simpletrackingcompass.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompass.*;
import static net.chorus.simpletrackingcompass.SimpleTrackingCompassClient.*;

public class TargetSelectorScreen extends Screen implements PacketDestination {
    private final Screen parent;

    private ScrollableList<GameProfile> playerList;
    private final String SUFFIX = " (You)";

    private final Predicate<? super GameProfile> validityTest =
        PlayerUtils::isValidOnlinePlayer;
    private final Predicate<? super GameProfile> trackabilityTest =
        gameProfile -> PlayerUtils.isTrackable(gameProfile.id());

    private static boolean doTrackabilityTest = false;

    public TargetSelectorScreen(Screen parent) {
        super(client(), Utils.getFont(), Component.literal("Select a target"));
        this.parent = parent;

        DestinationStorage.remove(TARGET_SELECTOR_SCREEN);
        DestinationStorage.put(TARGET_SELECTOR_SCREEN, this);
    }

    // Called on screen initialization

    @Override
    protected void init() {
        if (client().player == null) {
            client().setScreen(parent);
            return;
        }

        // 'Done' button

        OptionsScreen optionsScreen = new OptionsScreen(this, client().options);
        optionsScreen.init(width, height);

        Component doneButtonMessage = CommonComponents.GUI_DONE;

        int[] doneBounds = Utils.getWidgetBounds(optionsScreen, doneButtonMessage);

        Button doneButton = Button
            .builder(doneButtonMessage, button -> onClose())
            .bounds(doneBounds[0], doneBounds[1], doneBounds[2], doneBounds[3])
            .build();

        addRenderableWidget(doneButton);

        // 'Hide Compass HUD' button

        int toggleButtonY = (doneBounds[1] - 50) + (50 / 2 - doneBounds[3] / 2);

        CycleButton<Boolean> toggleButton = CycleButton
            .onOffBuilder(CompassTracker.isCompassHidden())
            .create(
                doneBounds[0], toggleButtonY,
                doneBounds[2], doneBounds[3],
                Component.literal("Hide Compass HUD"),
                (cycleButton, value) -> CompassTracker.setCompassHidden(value));

        addRenderableWidget(toggleButton);

        // Slider that sets the interval between server-bound packets

        RangeSlider intervalSlider = new RangeSlider(
            doneBounds[0], doneBounds[1] - 200,
            doneBounds[2], doneBounds[3],
            1, 40, ConfigManager.packetInterval,
            value -> ConfigManager.packetInterval = value.intValue()
        );

        intervalSlider.setValueStringifierAndTooltip(
            value -> Component.literal("Send Packets Every " + value.intValue() + " ticks"),
            Tooltip.create(Component.literal("Sets the interval between server-bound packets. Values below 5 ticks are discouraged as they can (in rare cases) cause server lag."))
        );

        addRenderableWidget(intervalSlider);

        // Scrollable list of players

        playerList = new ScrollableList<>(
            doneBounds[0], doneBounds[1] - 150, doneBounds[2], 100,
            4, 4,
            10, 4,
            20
        );

        // Add the local player to the list and a stringifier for it
        playerList.addEntry(client().player.getGameProfile());
        playerList.addEntryStringifier(client().player.getGameProfile(), gameProfile -> Component.literal(gameProfile.name() + SUFFIX));

        playerList.setDefaultStringifier(gameProfile -> Component.literal(gameProfile.name()));
        playerList.addFilter(validityTest);

        // Add players to the list
        playerList.addAllEntries(
            PlayerUtils.onlinePlayers().stream()
                .map(PlayerInfo::getProfile)
                .filter(gameProfile -> !gameProfile.id().equals(client().player.getGameProfile().id()))
                .toList()
        );

        filterByTrackability(doTrackabilityTest);

        addRenderableWidget(playerList);

        // 'Filter' button

        int[] filterTextureSize = Utils.scaleTextureToFit(FILTER, doneBounds[3] - 4);

        TexturedButton filterButton = new TexturedButton(
            doneBounds[0] + doneBounds[2] + 4, doneBounds[1] - 150,
            doneBounds[3], doneBounds[3],
            filterTextureSize[0], filterTextureSize[1],
            FILTER, button -> {
                doTrackabilityTest = !doTrackabilityTest;
                button.setTooltip(getFilterButtonTooltip(doTrackabilityTest));
                filterByTrackability(doTrackabilityTest);
            }
        );

        filterButton.setOverrideRenderHighlightedSprite(() -> false);
        filterButton.setTooltip(getFilterButtonTooltip(doTrackabilityTest));

        addRenderableWidget(filterButton);

        // 'Refresh' Button

        int[] refreshTextureSize = Utils.scaleTextureToFit(REFRESH, doneBounds[3] - 4);

        TexturedButton refreshButton = new TexturedButton(
            doneBounds[0] + doneBounds[2] + 4, doneBounds[1] - 150 + doneBounds[3] + 4,
            doneBounds[3], doneBounds[3],
            refreshTextureSize[0], refreshTextureSize[1],
            REFRESH, button -> {
                if (client().player == null) return;

                playerList.replaceList(
                    PlayerUtils.onlinePlayers().stream()
                        .map(PlayerInfo::getProfile)
                        .filter(gameProfile -> !gameProfile.id().equals(client().player.getGameProfile().id()))
                        .toList()
                );
                playerList.addEntry(0, client().player.getGameProfile());

                // Done to account for all (hopefully) possible scenarios

                // Without this line the players in other dimensions aren't filtered out properly in some scenarios
                filterByTrackability(false);
                filterByTrackability(doTrackabilityTest);
            }
        );

        refreshButton.setOverrideRenderHighlightedSprite(() -> false);
        refreshButton.setTooltip(Tooltip.create(Component.literal("Refreshes the player list")));

        addRenderableWidget(refreshButton);
    }

    // Called every frame during the screen's lifecycle

    @Override
    public void render(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
        super.render(graphics, mouseX, mouseY, deltaTicks);

        if (client().getWindow().isFullscreen()) {
            graphics.drawCenteredString(
                font, title,
                width / 2, 20,
                0xFFFFFFFF
            );
        }
    }

    private void filterByTrackability(boolean doTrackabilityTest) {
        if (doTrackabilityTest) {
            playerList.addFilter(trackabilityTest);
            if (!isServerModded()) return;

            for (int i = 0; i < playerList.getEntryList(true).size(); i++) {
                if (Boolean.TRUE.equals(PlayerUtils.isWithinRenderDistance(playerList.getEntry(i, true).id()))) continue;

                ClientNetworking.sendServerboundPacket(new PlayerPositionRequest(
                    Optional.ofNullable(playerList.getEntry(i, true).id()),
                    TARGET_SELECTOR_SCREEN
                ));
            }
        } else {
            playerList.removeAllFilters();
            playerList.addFilter(validityTest);
        }
    }

    private void filterByDimension(UUID uuid, ResourceKey<Level> dimension) {
        if (client().level == null || client().level.dimension().equals(dimension)) return;

        playerList.addFilter(gameProfile -> !gameProfile.id().equals(uuid));
    }

    private Tooltip getFilterButtonTooltip(boolean filterActive) {
        return Tooltip.create(Component.literal("Filters out the players that you aren't able to track.\nCurrent State: ").append(Component.literal(filterActive ? "ON" : "OFF").withStyle(filterActive ? ChatFormatting.GREEN : ChatFormatting.RED)));
    }

    public static void setDoTrackabilityTest(boolean test) {
        doTrackabilityTest = test;
    }

    // Called by ClientNetworking when receiving a packet payload

    @Override
    public void processPacket(CustomPacketPayload packetPayload) {
        if (!(packetPayload instanceof PlayerPositionResponse positionResponse)
            || positionResponse.playerUUID().isEmpty()
            || positionResponse.playerDimension().isEmpty()) return;

        filterByDimension(positionResponse.playerUUID().get(), positionResponse.playerDimension().get());
    }

    // Called on screen termination

    @Override
    public void onClose() {
        GameProfile chosenPlayer = playerList.getSelectedEntry();
        CompassTracker.resolveAuto(chosenPlayer, false);

        client().setScreen(parent);
    }
}
