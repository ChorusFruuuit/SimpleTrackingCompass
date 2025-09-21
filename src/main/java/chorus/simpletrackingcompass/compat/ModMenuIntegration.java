package chorus.simpletrackingcompass.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import chorus.simpletrackingcompass.screen.TargetSelectorScreen;
import chorus.simpletrackingcompass.util.PlayerUtils;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<TargetSelectorScreen> getModConfigScreenFactory() {
        return parent -> PlayerUtils.inWorld() ? new TargetSelectorScreen(parent) : null;
    }
}
