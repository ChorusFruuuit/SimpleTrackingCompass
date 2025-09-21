package chorus.simpletrackingcompass.screen.widget;

import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.function.Consumer;

public class RangeSliderWidget extends SliderWidget {

    private final int min;
    private final int max;
    private final Consumer<Integer> onValueChange;

    private int current;
    private Text label;

    public RangeSliderWidget(int x, int y, int width, int height,
                             int min, int max, int initial,
                             Consumer<Integer> onValueChange) {
        super(x, y, width, height, Text.empty(),
            min >= max ? 0 : (initial - min) / (double) (max - min));

        this.min = min;
        this.max = max;
        this.current = initial;

        this.label = Text.literal("Value");
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

    public void setLabelAndTooltip(Text label, Text tooltip) {
        this.label = label;
        this.setTooltip(Tooltip.of(tooltip));
        updateMessage();
    }
}
