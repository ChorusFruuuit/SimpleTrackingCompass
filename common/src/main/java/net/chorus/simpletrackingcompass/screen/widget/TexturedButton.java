package net.chorus.simpletrackingcompass.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class TexturedButton extends Button {
    private final ResourceLocation texture;
    private final int textureWidth;
    private final int textureHeight;

    private static final WidgetSprites SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("widget/button"),
        ResourceLocation.withDefaultNamespace("widget/button_disabled"),
        ResourceLocation.withDefaultNamespace("widget/button_highlighted")
    );
    private Supplier<Boolean> overrideRenderHighlightedSprite;

    public TexturedButton(int x, int y, int width, int height,
                          int textureWidth, int textureHeight,
                          ResourceLocation texture, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderDefaultSprite(graphics);

        int centeredX = getX() + (getWidth() - textureWidth) / 2;
        int centeredY = getY() + (getHeight() - textureHeight) / 2;

        graphics.blit(
            RenderPipelines.GUI_TEXTURED, texture,
            centeredX, centeredY, 0f, 0f,
            textureWidth, textureHeight,
            textureWidth, textureHeight
        );
    }

    private void renderDefaultSprite(final GuiGraphics graphics) {
        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            SPRITES.get(
                this.active,
                this.overrideRenderHighlightedSprite != null
                    ? this.overrideRenderHighlightedSprite.get()
                    : this.isHoveredOrFocused()
            ),
            this.getX(),
            this.getY(),
            this.getWidth(),
            this.getHeight(),
            ARGB.white(this.alpha)
        );
    }

    public void setOverrideRenderHighlightedSprite(Supplier<Boolean> overrideRenderHighlightedSprite) {
        this.overrideRenderHighlightedSprite = overrideRenderHighlightedSprite;
    }
}
