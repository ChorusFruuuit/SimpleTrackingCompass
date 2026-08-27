package net.chorus.simpletrackingcompass.screen.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class RangeSlider extends AbstractSliderButton {
    private final double min;
    private final double max;

    private final Consumer<Double> onValueChange;
    private Function<Double, Component> valueStringifier;

    public RangeSlider(int x, int y, int width, int height,
                       double minValue, double maxValue, double initialValue,
                       Consumer<Double> onValueChange) {
        if (minValue >= maxValue) throw new IllegalArgumentException("Min value must be smaller than the Max value.");
        if (initialValue < minValue || initialValue > maxValue) throw new IllegalArgumentException("The Initial value must lay in between the Min value and the Max value");
        super(x, y, width, height, Component.empty(), (initialValue - minValue) / (maxValue - minValue));

        this.min = minValue;
        this.max = maxValue;

        this.onValueChange = Objects.requireNonNull(onValueChange);
        this.valueStringifier = value -> Component.literal("Value : " + value);

        this.applyValue();
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(valueStringifier.apply(getScaledValue()));
    }

    @Override
    protected void applyValue() {
        onValueChange.accept(getScaledValue());
    }

    public void setValueStringifierAndTooltip(Function<Double, Component> valueStringifier, Tooltip tooltip) {
        this.valueStringifier = valueStringifier;
        setTooltip(tooltip);
        updateMessage();
    }

    private double getScaledValue() {
        return value * (max - min) + min;
    }
}
