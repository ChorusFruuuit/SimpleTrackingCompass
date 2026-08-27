package net.chorus.simpletrackingcompass.mixin;

import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@link Hud}'s private {@code overlayMessageString} and {@code overlayMessageTime} fields.
 * {@code overlayMessageTime} represents the remaining display duration in ticks.
 * */
@Mixin(Hud.class)
public interface HudAccessor {
    @Accessor("overlayMessageString")
    Component getOverlayMessageString();

    @Accessor("overlayMessageString")
    void setOverlayMessageString(Component string);

    @Accessor("overlayMessageTime")
    int getOverlayMessageTime();

    @Accessor("overlayMessageTime")
    void setOverlayMessageTime(int value);
}
