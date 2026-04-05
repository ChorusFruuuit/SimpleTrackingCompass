package chorus.simpletrackingcompass.screen.widget;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;

public class IconButtonWidget extends ButtonWidget {
    private final Identifier texture;

    public IconButtonWidget(int x, int y, int width, int height,
                            Identifier texture, PressAction onPress) {
        super(x, y, width, height, net.minecraft.text.Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.texture = texture;
    }

    @Override
    public void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
        super.drawButton(context);

        context.drawTexture(
            RenderPipelines.GUI_TEXTURED, texture,
            getX(), getY(), 0f, 0f,
            getWidth(), getHeight(),
            getWidth(), getHeight()
        );
    }
}
