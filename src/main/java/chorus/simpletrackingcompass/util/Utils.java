package chorus.simpletrackingcompass.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

public class Utils {

    public static int[] getTextureSize(Identifier id) {
        ResourceManager RM = MinecraftClient.getInstance().getResourceManager();

        try {
            Optional<Resource> resource = RM.getResource(id);
            if (resource.isPresent()) {
                BufferedImage img = ImageIO.read(resource.get().getInputStream());
                return new int[]{img.getWidth(), img.getHeight()};
            }
        } catch (IOException ignored) {
        }

        return new int[]{0, 0};
    }

    public static int[] scaleTextureToFit(Identifier id, int maxSide) {
        int[] size = getTextureSize(id);
        double scaleFactor = Math.max(size[0], size[1]) / (double) maxSide;
        int newWidth = (int) Math.round(size[0] / scaleFactor);
        int newHeight = (int) Math.round(size[1] / scaleFactor);

        return new int[]{newWidth, newHeight};
    }

    public static int[] getButtonBounds(Screen screen, Text text) {
        for (Element element : screen.children()) {
            if (element instanceof ButtonWidget button) {
                if (button.getMessage().getString().equals(text.getString())) {
                    return new int[]{
                        button.getX(), button.getY(),
                        button.getWidth(), button.getHeight()
                    };
                }
            }
        }

        return null;
    }

    public static int calculateAngle(double playerX, double playerZ,
                                     double targetX, double targetZ) {
        double dx = targetX - playerX;
        double dz = targetZ - playerZ;

        double angleRad = Math.atan2(dz, dx);
        double angleDeg = Math.toDegrees(angleRad);
        return (int) ((angleDeg - 90 + 360) % 360);
    }

    public static String removeWhitespace(String s) {
        return s == null ? null : s.replaceAll("\\s+", "");
    }
}
