package me.chorus.simpletrackingcompass;

import me.chorus.simpletrackingcompass.util.ModUtils;
import me.chorus.simpletrackingcompass.util.ScrollableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.UUID;

public class ModSettingsScreen extends Screen {
    private final Screen parent;
    private ScrollableList playerSelectorList;

    private final String suffix = " (You)";

    public ModSettingsScreen(Screen parent) {
        super(Text.literal("Track a player"));
        this.parent = parent;
    }

    @Override
    protected void init() {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Creating 'Done' button

        OptionsScreen optionsScreen = new OptionsScreen(
                client.currentScreen,
                client.options
        );

        optionsScreen.init(
                client,
                this.width,
                this.height
        );

        int[] donePos = ModUtils.getButtonPosition(
                optionsScreen,
                "gui.done"
        );

        ButtonWidget doneButton = ButtonWidget.builder(
                    Text.translatable("gui.done"), btn -> closeScreen()
                )
                .dimensions(donePos[0], donePos[1],
                            donePos[2], donePos[3])
                .build();

        addDrawableChild(doneButton);

        // Creating 'Hide Compass HUD' button

        String toggleLabel = CompassHUD.CompassHUDHidden ? "Hide Compass HUD: ON" : "Hide Compass HUD: OFF";

        int hideY = (donePos[1] - 50) + (50 / 2 - donePos[3] / 2);

        ButtonWidget hideButton = ButtonWidget.builder(
            Text.of(toggleLabel), btn -> {
                CompassHUD.CompassHUDHidden = !CompassHUD.CompassHUDHidden;

                String newLabel = CompassHUD.CompassHUDHidden ? "Hide Compass HUD: ON" : "Hide Compass HUD: OFF";
                btn.setMessage(Text.of(newLabel));
            }
        )
        .dimensions(donePos[0], hideY,
                    donePos[2], donePos[3])
        .build();

        addDrawableChild(hideButton);

        // Creating scrollable list of players

        this.playerSelectorList = new ScrollableList(
                client,
                client.currentScreen,
                donePos[0],
                donePos[1] - 150,
                donePos[2],
                100,
                10,
                20,
                false
        );
        playerSelectorList.setVisible(true);

        PlayerEntity player = client.player;
        playerSelectorList.addEntry(player.getName().getString() + suffix);

        Collection<PlayerListEntry> players = client.getNetworkHandler().getPlayerList();

        for (PlayerListEntry entry : players) {
            // Skip the current player
            if (entry.getProfile().getId().equals(player.getUuid()) || entry.getProfile().getName().trim().isEmpty()) continue;

            String name = entry.getProfile().getName();
            playerSelectorList.addEntry(name);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC key
            closeScreen();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void closeScreen() {
        MinecraftClient client = MinecraftClient.getInstance();

        Integer index = playerSelectorList.getSelectedIndex();
        String selectedPlayerName = (index != null) ? playerSelectorList.getEntry(playerSelectorList.getAllElements().indexOf(playerSelectorList.getVisibleEntry(index))) : null;

        UUID selectedPlayerUuid = null;

        if (selectedPlayerName != null && selectedPlayerName.endsWith(suffix)) {
            selectedPlayerName = selectedPlayerName.replace(suffix, "");
            selectedPlayerUuid = client.player.getUuid();
        }
        else if (selectedPlayerName != null) {
            for (PlayerListEntry player : client.getNetworkHandler().getPlayerList()) {
                if (player.getProfile().getName().equals(selectedPlayerName)) {
                    selectedPlayerUuid = player.getProfile().getId();
                    break;
                }
            }
        }

        CompassHUD.setTargetPlayer(selectedPlayerUuid, selectedPlayerName, false);

        client.setScreen(this.parent);
    }
}
