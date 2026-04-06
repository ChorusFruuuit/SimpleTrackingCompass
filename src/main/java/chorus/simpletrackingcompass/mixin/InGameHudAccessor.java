package chorus.simpletrackingcompass.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface InGameHudAccessor {
    @Accessor("overlayMessageString")
    Component getOverlayMessage();

    @Accessor("overlayMessageTime")
    int getOverlayRemaining();

    @Accessor("overlayMessageTime")
    void setOverlayRemaining(int value);
}
