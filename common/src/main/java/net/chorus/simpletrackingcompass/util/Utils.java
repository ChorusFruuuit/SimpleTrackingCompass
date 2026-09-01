package net.chorus.simpletrackingcompass.util;

import net.chorus.simpletrackingcompass.mixin.GuiAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompass.*;
import static net.chorus.simpletrackingcompass.SimpleTrackingCompassClient.*;

public final class Utils {
    public static int[] getTextureSize(ResourceLocation id) {
        try {
            Optional<Resource> resource = resourceManager().getResource(id);
            if (resource.isPresent()) {
                ImageInputStream imageInputStream = ImageIO.createImageInputStream(resource.get().open());

                Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
                if (readers.hasNext()) {
                    ImageReader reader = readers.next();

                    try {
                        reader.setInput(imageInputStream, true, true);
                        return new int[]{reader.getWidth(0), reader.getHeight(0)};
                    } finally {
                        reader.dispose();
                        imageInputStream.close();
                    }
                }
            }
        } catch (IOException | IllegalStateException e) {
            LOGGER.warn(String.format("Failed to retrieve the file size at %s", id), e);
        }

        return new int[]{0, 0}; // Default fallback
    }

    public static int[] scaleTextureToFit(ResourceLocation id, int maxSide) {
        int[] size = getTextureSize(id);
        double scaleFactor = maxSide != 0 ? Math.max(size[0], size[1]) / (double) maxSide : 0;
        int newWidth = scaleFactor != 0 ? (int) Math.round(size[0] / scaleFactor) : 0;
        int newHeight = scaleFactor != 0 ? (int) Math.round(size[1] / scaleFactor) : 0;

        return new int[]{newWidth, newHeight};
    }

    public static int[] getWidgetBounds(Screen screen, Component message) {
        Optional<? extends GuiEventListener> guiElement = screen.children().stream()
            .filter(element ->
                element instanceof AbstractWidget widget
                    && widget.getMessage().equals(message)
            ).findAny();

        if (guiElement.isPresent() && guiElement.get() instanceof AbstractWidget widget) {
            return new int[]{
                widget.getX(), widget.getY(),
                widget.getWidth(), widget.getHeight()
            };
        } else {
            return new int[]{0, 0, 0, 0}; // Default fallback
        }
    }

    public static String removeWhitespace(String s) {
        return s != null ? s.replaceAll("\\s+", "") : null;
    }

    public static Font getFont() {
        return client().font;
    }

    public static ResourceLocation identifierWithModNamespace(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static GuiAccessor getGuiAccessor() {
        return (GuiAccessor) client().gui;
    }

    // Angle transformation helpers

    /**
     * Transforms the given {@code geometricAngle}
     * into a {@link #normalizedAngle(double) normalized} {@code minecraftAngle}
     * */
    public static double toMinecraftAngle(double geometricAngle) {
        return transformAngle(geometricAngle);
    }

    /**
     * Transforms the given {@code minecraftAngle}
     * into a {@link #normalizedAngle(double) normalized} {@code geometricAngle}
     * */
    public static double toGeometricAngle(double minecraftAngle) {
        return transformAngle(minecraftAngle);
    }

    /** Transforms the given {@code angle} between a {@code geometricAngle} and a {@code minecraftAngle} */
    private static double transformAngle(double angle) {
        return normalizedAngle(-normalizedAngle(angle) - 90);
    }

    /** Clamps the given {@code angle} between 0 and 359 */
    public static double normalizedAngle(double angle) {
        angle = angle % 360;
        return angle >= 0 ? angle : angle + 360;
    }
}
