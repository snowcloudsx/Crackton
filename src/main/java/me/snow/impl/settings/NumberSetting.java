package me.snow.impl.settings;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double increment;

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double increment) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public NumberSetting(String name, String description, double defaultValue, double min, double max) {
        this(name, description, defaultValue, min, max, 0.1);
    }

    @Override
    public void setValue(Double value) {
        // Clamp value between min and max
        double clampedValue = Math.max(min, Math.min(max, value));

        // Round to increment
        if (increment > 0) {
            clampedValue = Math.round(clampedValue / increment) * increment;
        }

        super.setValue(clampedValue);
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getIncrement() { return increment; }

    // Convenience methods for int values
    public int getIntValue() { return value.intValue(); }
    public float getFloatValue() { return value.floatValue(); }
}