package chorus.simpletrackingcompass.screen.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class IconButtonWidget extends Button {
    private final Identifier texture;

    public IconButtonWidget(int x, int y, int width, int height,
                            Identifier texture, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.texture = texture;
    }

    @Override
    public void extractContents(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractDefaultSprite(context);

        context.blit(
            RenderPipelines.GUI_TEXTURED, texture,
            getX(), getY(), 0f, 0f,
            getWidth(), getHeight(),
            getWidth(), getHeight()
        );
    }
}
