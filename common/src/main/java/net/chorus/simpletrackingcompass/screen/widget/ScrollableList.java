package net.chorus.simpletrackingcompass.screen.widget;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.chorus.simpletrackingcompass.util.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompassClient.*;

public class ScrollableList<E> implements Renderable, GuiEventListener, NarratableEntry {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private final int leftPadding;
    private final int topPadding;

    private final int entryHeight;
    private final int ySpacing;

    private final EditBox searchField;
    private Predicate<? super E> searchFilter = _ -> true;

    private final Set<Predicate<? super E>> filters = new HashSet<>();

    private final List<E> entries = new ArrayList<>();
    private final List<E> filteredEntries = new ArrayList<>();

    private final double entriesFitting;
    private final int visibleEntryCount;

    private final Map<E, Function<E, Component>> stringifierMap = new HashMap<>();
    private Function<E, Component> defaultStringifier = entry -> Component.literal(entry.toString());

    private int selectedIndex = -1;

    private int scrollX = 0;
    private int scrollY = 0;
    private int maxScrollX;
    private int maxScrollY;

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
                          int leftPadding, int topPadding,
                          int entryHeight, int ySpacing,
                          int searchFieldHeight) {
        if (x < 0 || y < 0 || width < 0 || height < 0 || leftPadding < 0 || topPadding < 0 || entryHeight < 0 || ySpacing < 0 || searchFieldHeight < 0) throw new IllegalArgumentException("Neither argument can be negative.");

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.leftPadding = leftPadding;
        this.topPadding = topPadding;

        this.entryHeight = entryHeight;
        this.ySpacing = ySpacing;

        this.entriesFitting = (double) (this.height - this.topPadding) / (this.entryHeight + this.ySpacing);
        this.visibleEntryCount = (int) Math.ceil(this.entriesFitting);

        this.maxScrollX = this.leftPadding;
        this.maxScrollY = this.topPadding;

        this.searchField = new EditBox(
            Utils.getFont(),
            this.x, this.y - searchFieldHeight,
            this.width, searchFieldHeight,
            Component.literal("Search...")
        );
        this.searchField.setHint(Component.literal("Search..."));
        this.searchField.setResponder(changedListener -> {
            searchFilter = entry ->
                Utils.removeWhitespace(getStringifier(entry).apply(entry).getString().toLowerCase()).contains(Utils.removeWhitespace(changedListener.toLowerCase()));
            updateFilteredEntries(combinedPredicate());
        });
    }

    // Setters and Getters

    public boolean isSelected() {
        return selectedIndex != -1;
    }

    public E getSelectedEntry() {
        return isSelected() ? getEntry(selectedIndex, true) : null;
    }

    public void addAllEntries(List<E> entries) {
        this.entries.addAll(entries);
        updateFilteredEntries(combinedPredicate());
    }

    public void addEntry(E entry) {
        entries.add(entry);
        updateFilteredEntries(combinedPredicate());
    }

    public void addEntry(int index, E entry) {
        entries.add(index, entry);
        updateFilteredEntries(combinedPredicate());
    }

    @SuppressWarnings("unused")
    public E removeEntry(int index) {
        E removed = entries.remove(index);
        updateFilteredEntries(combinedPredicate());
        return removed;
    }

    public E getEntry(int index, boolean filtered) {
        return filtered ? filteredEntries.get(index) : entries.get(index);
    }

    public List<E> getEntryList(boolean filtered) {
        return filtered ? filteredEntries : entries;
    }

    public void replaceList(List<E> newList) {
        entries.clear();
        filteredEntries.clear();

        entries.addAll(newList);
        filteredEntries.addAll(newList);

        updateFilteredEntries(combinedPredicate());
    }

    public void addEntryStringifier(E entry, Function<E, Component> stringifier) {
        stringifierMap.put(entry, stringifier);
        updateFilteredEntries(combinedPredicate());
    }

    public void setDefaultStringifier(Function<E, Component> stringifier) {
        defaultStringifier = stringifier;
        updateFilteredEntries(combinedPredicate());
    }

    public void addFilter(Predicate<? super E> filter) {
        filters.add(filter);
        updateFilteredEntries(combinedPredicate());
    }

    @SuppressWarnings("unused")
    public boolean removeFilter(Predicate<? super E> filter) {
        boolean contained = filters.remove(filter);
        updateFilteredEntries(combinedPredicate());
        return contained;
    }

    public void removeAllFilters() {
        filters.clear();
    }

    // Render method. Called every frame

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        searchField.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.fill(x, y, x + width, y + height, 0xAA000000);
        graphics.enableScissor(x, y, x + width, y + height);

        int startIndex = (int) Math.floor((double) scrollY / (entryHeight + ySpacing));

        for (int i = 0; (i < visibleEntryCount) && (startIndex + i < filteredEntries.size()); i++) {
            E entry = filteredEntries.get(startIndex + i);
            Function<E, Component> stringifier = getStringifier(entry);

            graphics.text(
                Utils.getFont(),
                startIndex + i == selectedIndex && isSelected()
                    ? stringifier.apply(entry).copy().withColor(0xFF808080)
                    : stringifier.apply(entry),
                (x - scrollX) + leftPadding,
                y + topPadding + i * (entryHeight + ySpacing),
                -1
            );
        }

        graphics.disableScissor();
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
        return searchField.isMouseOver(mouseX, mouseY) || insideList(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!insideList(mouseX, mouseY)) return false;

        if (isShiftDown()) {
            scrollX -= (int) (verticalAmount * 5);
            scrollX = Math.clamp(scrollX, 0, maxScrollX);
        } else {
            scrollY -= (int) (verticalAmount * (entryHeight + ySpacing));
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
        }

        if (clickedList && searchField.isFocused() && searchField.getValue().isEmpty()) {
            searchField.setFocused(false);
        }

        if (clickedList) {
            int previouslySelectedIndex = selectedIndex;
            selectedIndex = !filteredEntries.isEmpty() ? (int)
                Math.clamp(
                    Math.floor((click.y() - y + scrollY) / (entryHeight + ySpacing)),
                    0, filteredEntries.size() - 1
                ) : -1;
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

    private Function<E, Component> getStringifier(E entry) {
        return stringifierMap.getOrDefault(entry, defaultStringifier);
    }

    private boolean isShiftDown() {
        Window window = client().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
            || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private Predicate<? super E> combinedPredicate() {
        return combinedPredicate(Stream.concat(filters.stream(), Stream.of(searchFilter)));
    }

    private Predicate<? super E> combinedPredicate(Stream<Predicate<? super E>> predicates) {
        return predicates
            .reduce(
                _ -> true,
                (partialResult, predicate) ->
                    entry -> partialResult.test(entry) && predicate.test(entry)
            );
    }

    private void filterEntries(Predicate<? super E> filter) {
        filteredEntries.clear();
        entries.stream()
            .filter(filter != null ? filter : _ -> true)
            .forEach(filteredEntries::add);
    }

    private void updateFilteredEntries(Predicate<? super E> filter) {
        selectedIndex = -1;

        filterEntries(filter);

        Component widest = Component.empty();
        for (E entry : filteredEntries) {
            if (Utils.getFont().width(getStringifier(entry).apply(entry)) >
                Utils.getFont().width(widest)) {
                widest = getStringifier(entry).apply(entry);
            }
        }
        calculateScrollBounds(widest);

        scrollY = 0;
        scrollX = Math.clamp(scrollX, 0, maxScrollX);
    }

    private void calculateScrollBounds(Component stringifiedEntry) {
        int entryWidth = Utils.getFont().width(stringifiedEntry);
        maxScrollX = Math.max(0, entryWidth - (width - leftPadding));

        int fullyVisibleCount = visibleEntryCount * (entryHeight + ySpacing) - ySpacing < (height - topPadding)
            ? visibleEntryCount
            : (int) Math.floor(entriesFitting);
        maxScrollY = Math.max(0, (filteredEntries.size() - fullyVisibleCount) * (entryHeight + ySpacing));
    }

    private boolean insideList(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width
            && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public String toString() {
        return entries.toString();
    }
}
