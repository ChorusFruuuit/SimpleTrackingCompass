package me.chorus.simpletrackingcompass.screen.widget;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IconButtonWidget extends ButtonWidget {
    private static Identifier ICON_TEXTURE;

    public IconButtonWidget(int x, int y, int width, int height,
                            PressAction onPress, Identifier id) {
        super(x, y, width, height, Text.empty(), onPress,
              DEFAULT_NARRATION_SUPPLIER);

        ICON_TEXTURE = id;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                ICON_TEXTURE,
                getX(), getY(),
                0f, 0f,
                getWidth(), getHeight(),
                getWidth(), getHeight()
        );
    }
}
