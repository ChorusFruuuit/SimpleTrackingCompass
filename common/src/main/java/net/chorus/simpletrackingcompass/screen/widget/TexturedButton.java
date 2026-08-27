package net.chorus.simpletrackingcompass.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class TexturedButton extends Button {
    private final Identifier texture;
    private final int textureWidth;
    private final int textureHeight;

    public TexturedButton(int x, int y, int width, int height,
                          int textureWidth, int textureHeight,
                          Identifier texture, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractDefaultSprite(graphics);

        int centeredX = getX() + (getWidth() - textureWidth) / 2;
        int centeredY = getY() + (getHeight() - textureHeight) / 2;

        graphics.blit(
            RenderPipelines.GUI_TEXTURED, texture,
            centeredX, centeredY, 0f, 0f,
            textureWidth, textureHeight,
            textureWidth, textureHeight
        );
    }
}
