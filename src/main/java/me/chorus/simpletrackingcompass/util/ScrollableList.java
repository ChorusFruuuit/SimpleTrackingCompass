package me.chorus.simpletrackingcompass.util;

import me.chorus.simpletrackingcompass.mixin.ScreenInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ScrollableList implements Drawable, Element, Selectable {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int totalItemHeight;
    private final int entryHeight;
    private final int offset = 4;

    private boolean visible = false;

    private final TextFieldWidget searchField;
    private String filter = "";

    private final MinecraftClient client;

    private final List<String> allElements = new ArrayList<>();
    private final List<String> visibleElements = new ArrayList<>();

    private int selectedIndex = -1;

    private int scrollY = 0;
    private int scrollX = 0;
    private int maxScrollY = offset;
    private int maxScrollX = offset;

    // Implementation of abstract methods

    @Override
    public void setFocused(boolean focused) { }

    @Override
    public boolean isFocused() { return false; }

    @Override
    public SelectionType getType() { return SelectionType.NONE; }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) { }

    // Constructor

    public ScrollableList(MinecraftClient client, Screen screen, int x, int y, int width, int height,
                          int itemHeight, int entryHeight, boolean isInside) {
        this.client = client;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = isInside ? height - entryHeight : height;
        this.totalItemHeight = itemHeight + this.offset;
        this.entryHeight = entryHeight;

        this.searchField = new TextFieldWidget(
                client.textRenderer,
                x, y - entryHeight,
                width, entryHeight,
                Text.literal("Search...")
        );
        this.searchField.setPlaceholder(Text.literal("Search..."));
        this.searchField.setChangedListener(this::refreshVisibleEntries);

        ((ScreenInvoker) screen).invokeAddDrawableChild(this.searchField);
        ((ScreenInvoker) screen).invokeAddDrawableChild(this);
    }

    //  Public method for showing and hiding the list

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    // Setters and Getters

    public boolean isHidden() {
        return !this.visible;
    }

    public boolean isSelected() {
        return selectedIndex != -1;
    }

    public void addEntry(String text) {
        allElements.add(text);
        refreshVisibleEntries(this.filter);
    }

    public String getEntry(int index) {
        return allElements.get(index);
    }

    public String getVisibleEntry(int index) {
        return visibleElements.get(index);
    }

    public String removeEntry(int index) {
        String removed = allElements.remove(index);
        refreshVisibleEntries(this.filter);
        return removed;
    }

    public Integer getSelectedIndex() {
        return isSelected() ? this.selectedIndex : null;
    }

    public List<String> getAllElements() {
        return allElements;
    }

    // Render method. Called every frame

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (isHidden()) return;

        this.searchField.render(context, mouseX, mouseY, delta);

        context.fill(x, y, x + width, y + height, 0xAA000000);
        context.enableScissor(x, y, x + width, y + height);

        int visibleCount = height / totalItemHeight;
        int startIndex = (int) Math.round((double) scrollY / (double) totalItemHeight);

        for (int i = 0; (i < visibleCount) && (startIndex + i < visibleElements.size()); i++) {
            String text = visibleElements.get(startIndex + i);
            int color = (startIndex + i == selectedIndex && isSelected())
                    ? 0xFF808080 : 0xFFFFFFFF;

            int drawX = offset + (x - scrollX);
            int drawY = offset + (y + i * totalItemHeight);

            context.drawText(client.textRenderer, text, drawX, drawY, color, false);
        }

        context.disableScissor();
    }

    // Mouse and keyboard event methods

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isHidden()) return false;

        if (insideList(mouseX, mouseY)) {
            if (isShiftDown()) {
                scrollX -= (int) (verticalAmount * 5);
                scrollX = Math.min(maxScrollX, Math.max(0, scrollX));
            } else {
                scrollY -= (int) (verticalAmount * totalItemHeight);
                scrollY = Math.min(maxScrollY, Math.max(0, scrollY));
            }
        }

        return true;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (isHidden()) return false;

        return !insideEntry(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHidden()) return false;

        boolean clickedInsideEntry = insideEntry(mouseX, mouseY);
        boolean clickedInsideList = insideList(mouseX, mouseY);

        if (!clickedInsideEntry && !clickedInsideList && searchField.isFocused()) {
            searchField.setFocused(false);
            searchField.setText("");
        }

        if (clickedInsideList){
            int previouslySelectedIndex = selectedIndex;
            selectedIndex = (int) Math.min(
                    Math.max(0, Math.floor((mouseY - y + scrollY) / totalItemHeight)),
                    visibleElements.size() - 1
            );
            if (selectedIndex == previouslySelectedIndex) selectedIndex = -1;
        }

        return clickedInsideEntry || clickedInsideList;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (isHidden()) return false;
        return searchField.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isHidden()) return false;
        return searchField.keyPressed(keyCode, scanCode, modifiers);
    }

    // Private helper methods

    private boolean isShiftDown() {
        long window = MinecraftClient.getInstance().getWindow().getHandle();
        return InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private void filterVisibleEntries(String filter) {
        visibleElements.clear();
        for (String element : allElements) {
            if (element.trim().toLowerCase().contains(filter.trim().toLowerCase())) {
                visibleElements.add(element);
            }
        }
    }

    private void refreshVisibleEntries(String filter) {
        this.filter = filter;
        filterVisibleEntries(this.filter);

        String widest = "";
        for (String entry : visibleElements) {
            if (client.textRenderer.getWidth(entry) >
                    client.textRenderer.getWidth(widest)) {
                widest = entry;
            }
        }
        calculateScrollBounds(widest);

        scrollY = Math.min(maxScrollY, Math.max(0, scrollY));
        scrollX = Math.min(maxScrollX, Math.max(0, scrollX));
    }

    private void calculateScrollBounds(String text) {
        int contentHeight = visibleElements.size() * totalItemHeight;
        maxScrollY = Math.max(0, contentHeight - height);

        int elementWidth = client.textRenderer.getWidth(text) + 2 * offset;
        maxScrollX = Math.max(0, elementWidth - width);
    }

    private boolean insideEntry(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y - entryHeight && mouseY <= y;
    }

    private boolean insideList(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
    }
}