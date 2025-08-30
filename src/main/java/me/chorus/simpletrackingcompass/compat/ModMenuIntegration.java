package me.chorus.simpletrackingcompass.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.chorus.simpletrackingcompass.screen.ModSettingsScreen;
import net.minecraft.client.MinecraftClient;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) {
                // Not in-game
                return null;
            }
            return new ModSettingsScreen(parent);
        };
    }
}
