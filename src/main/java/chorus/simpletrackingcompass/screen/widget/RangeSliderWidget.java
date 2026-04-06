package chorus.simpletrackingcompass.screen.widget;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class RangeSliderWidget extends AbstractSliderButton {

    private final int min;
    private final int max;
    private final Consumer<Integer> onValueChange;

    private int current;
    private Component label;

    public RangeSliderWidget(int x, int y, int width, int height,
                             int min, int max, int initial,
                             Consumer<Integer> onValueChange) {
        super(x, y, width, height, Component.empty(),
            min >= max ? 0 : (initial - min) / (double) (max - min));

        this.min = min;
        this.max = max;
        this.current = initial;

        this.label = Component.literal("Value");
        this.onValueChange = Objects.requireNonNull(onValueChange);

        this.onValueChange.accept(this.current);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.label.copy().append(": " + this.current));
    }

    @Override
    protected void applyValue() {
        this.current = min + (int) Math.round(this.value * (max - min));
        this.onValueChange.accept(this.current);
    }

    public void setLabelAndTooltip(Component label, Component tooltip) {
        this.label = label;
        this.setTooltip(Tooltip.create(tooltip));
        updateMessage();
    }
}
