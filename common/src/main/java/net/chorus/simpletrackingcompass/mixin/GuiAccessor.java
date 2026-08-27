package net.chorus.simpletrackingcompass.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes {@link Gui}'s private {@code overlayMessageString} and {@code overlayMessageTime} fields.
 * {@code overlayMessageTime} represents the remaining display duration in ticks.
 * */
@Mixin(Gui.class)
public interface GuiAccessor {
    @Accessor("overlayMessageString")
    Component getOverlayMessageString();

    @Accessor("overlayMessageString")
    void setOverlayMessageString(Component string);

    @Accessor("overlayMessageTime")
    int getOverlayMessageTime();

    @Accessor("overlayMessageTime")
    void setOverlayMessageTime(int value);
}
