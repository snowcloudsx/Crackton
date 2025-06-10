package me.snow.impl.settings;

public abstract class Setting<T> {
    protected final String name;
    protected final String description;
    protected T value;
    protected final T defaultValue;

    public Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public T getValue() { return value; }
    public T getDefaultValue() { return defaultValue; }

    public void setValue(T value) {
        this.value = value;
        onValueChanged();
    }

    public void reset() {
        setValue(defaultValue);
    }

    protected void onValueChanged() {
        // Override in subclasses for custom behavior
    }
}