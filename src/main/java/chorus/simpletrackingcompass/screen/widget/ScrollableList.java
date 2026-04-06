package chorus.simpletrackingcompass.screen.widget;

import chorus.simpletrackingcompass.util.Utils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ScrollableList implements Renderable, GuiEventListener, NarratableEntry {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int entryHeight;
    private final int offset = 4;

    private final EditBox searchField;
    private String filter = "";

    private final List<String> allElements = new ArrayList<>();
    private final List<String> visibleElements = new ArrayList<>();

    private int selectedIndex = -1;

    private int scrollY = 0;
    private int scrollX = 0;
    private int maxScrollY = offset;
    private int maxScrollX = offset;

    // Implementation of abstract methods

    @Override
    @NonNull
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(@NonNull NarrationElementOutput builder) {
    }

    // Constructor

    public ScrollableList(int x, int y, int width, int height,
                          int entryHeight, int searchFieldHeight,
                          boolean isInside) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = isInside ? height - searchFieldHeight : height;
        this.entryHeight = entryHeight + this.offset;

        this.searchField = new EditBox(
            getTextRenderer(),
            x, y - searchFieldHeight,
            width, searchFieldHeight,
            Component.literal("Search...")
        );
        this.searchField.setHint(Component.literal("Search..."));
        this.searchField.setResponder(changedListener -> {
            selectedIndex = -1;
            refreshVisibleEntries(changedListener);
        });
    }

    // Setters and Getters

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

    @SuppressWarnings("unused")
    public String removeEntry(int index) {
        String removed = allElements.remove(index);
        refreshVisibleEntries(this.filter);
        return removed;
    }

    public Integer getSelectedIndex() {
        return isSelected() ? this.selectedIndex : null;
    }

    public List<String> getElements() {
        return allElements;
    }

    // Render method. Called every frame

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.searchField.extractWidgetRenderState(context, mouseX, mouseY, delta);

        context.fill(x, y, x + width, y + height, 0xAA000000);
        context.enableScissor(x, y, x + width, y + height);

        int visibleCount = height / entryHeight;
        int startIndex = (int) Math.round((double) scrollY / (double) entryHeight);

        for (int i = 0; (i < visibleCount) && (startIndex + i < visibleElements.size()); i++) {
            String text = visibleElements.get(startIndex + i);
            int color = (startIndex + i == selectedIndex && isSelected())
                ? 0xFF808080 : 0xFFFFFFFF;

            context.text(
                getTextRenderer(),
                Component.literal(text),
                offset + (x - scrollX),
                offset + (y + i * entryHeight),
                color
            );
        }

        context.disableScissor();
    }

    // Mouse and keyboard event methods

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isFocused() {
        return searchField.isFocused() || selectedIndex != -1;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!insideList(mouseX, mouseY)) return false;

        if (isShiftDown()) {
            scrollX -= (int) (verticalAmount * 5);
            scrollX = Math.clamp(scrollX, 0, maxScrollX);
        } else {
            scrollY -= (int) (verticalAmount * entryHeight);
            scrollY = Math.clamp(scrollY, 0, maxScrollY);
        }

        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        boolean clickedSearch = searchField.isMouseOver(click.x(), click.y());
        boolean clickedList = insideList(click.x(), click.y());

        if (clickedSearch && !searchField.isFocused()) {
            searchField.setFocused(true);
            selectedIndex = -1;
        } else if (!clickedSearch && searchField.isFocused() &&
            (!clickedList || searchField.getValue().isEmpty())) {
            searchField.setFocused(false);
            searchField.setValue("");
        }

        if (clickedList) {
            int previouslySelectedIndex = selectedIndex;
            selectedIndex = visibleElements.isEmpty()
                ? -1
                : (int) Math.clamp(
                    Math.floor((click.y() - y + scrollY) / entryHeight),
                    0,
                    visibleElements.size() - 1
                );
            if (selectedIndex == previouslySelectedIndex) selectedIndex = -1;
        }

        return clickedSearch || clickedList;
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent input) {
        return searchField.keyPressed(input);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent input) {
        return searchField.charTyped(input);
    }

    // Private helper methods

    private Font getTextRenderer() {
        return Minecraft.getInstance().font;
    }

    private boolean isShiftDown() {
        Window window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
            || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private void filterVisibleEntries(String filter) {
        visibleElements.clear();
        for (String element : allElements) {
            if (Utils.removeWhitespace(element.toLowerCase()).contains(
                Utils.removeWhitespace(filter.toLowerCase()))) {
                visibleElements.add(element);
            }
        }
    }

    private void refreshVisibleEntries(String filter) {
        this.filter = filter;
        filterVisibleEntries(this.filter);

        String widest = "";
        for (String entry : visibleElements) {
            if (getTextRenderer().width(entry) >
                getTextRenderer().width(widest)) {
                widest = entry;
            }
        }
        calculateScrollBounds(widest);

        scrollY = Math.clamp(scrollY, 0, maxScrollY);
        scrollX = Math.clamp(scrollX, 0, maxScrollX);
    }

    private void calculateScrollBounds(String text) {
        int contentHeight = visibleElements.size() * entryHeight;
        maxScrollY = Math.max(0, contentHeight - height);

        int elementWidth = getTextRenderer().width(text) + 2 * offset;
        maxScrollX = Math.max(0, elementWidth - width);
    }

    private boolean insideList(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width &&
            mouseY >= y && mouseY <= y + height;
    }
}