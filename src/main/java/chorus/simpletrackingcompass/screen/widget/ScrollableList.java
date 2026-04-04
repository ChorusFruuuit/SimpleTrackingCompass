package chorus.simpletrackingcompass.screen.widget;

import chorus.simpletrackingcompass.util.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ScrollableList implements Drawable, Element, Selectable {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int entryHeight;
    private final int offset = 4;

    private final TextFieldWidget searchField;
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
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
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

        this.searchField = new TextFieldWidget(
            getTextRenderer(),
            x, y - searchFieldHeight,
            width, searchFieldHeight,
            Text.literal("Search...")
        );
        this.searchField.setPlaceholder(Text.literal("Search..."));
        this.searchField.setChangedListener(changedListener -> {
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.searchField.render(context, mouseX, mouseY, delta);

        context.fill(x, y, x + width, y + height, 0xAA000000);
        context.enableScissor(x, y, x + width, y + height);

        int visibleCount = height / entryHeight;
        int startIndex = (int) Math.round((double) scrollY / (double) entryHeight);

        for (int i = 0; (i < visibleCount) && (startIndex + i < visibleElements.size()); i++) {
            String text = visibleElements.get(startIndex + i);
            int color = (startIndex + i == selectedIndex && isSelected())
                ? 0xFF808080 : 0xFFFFFFFF;

            context.drawTextWithShadow(
                getTextRenderer(),
                Text.literal(text),
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
    public boolean mouseClicked(Click click, boolean doubled) {
        boolean clickedSearch = searchField.isMouseOver(click.x(), click.y());
        boolean clickedList = insideList(click.x(), click.y());

        if (clickedSearch && !searchField.isFocused()) {
            searchField.setFocused(true);
            selectedIndex = -1;
        } else if (!clickedSearch && searchField.isFocused() &&
            (!clickedList || searchField.getText().isEmpty())) {
            searchField.setFocused(false);
            searchField.setText("");
        }

        if (clickedList) {
            int previouslySelectedIndex = selectedIndex;
            selectedIndex = (int) Math.clamp(
                Math.floor((click.y() - y + scrollY) / entryHeight),
                0,
                visibleElements.size() - 1
            );
            if (selectedIndex == previouslySelectedIndex) selectedIndex = -1;
        }

        return clickedSearch || clickedList;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        return searchField.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        return searchField.charTyped(input);
    }

    // Private helper methods

    private TextRenderer getTextRenderer() {
        return MinecraftClient.getInstance().textRenderer;
    }

    private boolean isShiftDown() {
        Window window = MinecraftClient.getInstance().getWindow();
        return InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)
            || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
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
            if (getTextRenderer().getWidth(entry) >
                getTextRenderer().getWidth(widest)) {
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

        int elementWidth = getTextRenderer().getWidth(text) + 2 * offset;
        maxScrollX = Math.max(0, elementWidth - width);
    }

    private boolean insideList(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width &&
            mouseY >= y && mouseY <= y + height;
    }
}