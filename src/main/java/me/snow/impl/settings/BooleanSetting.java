package me.snow.impl.settings;

public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
    }

    public void toggle() {
        setValue(!value);
    }

    public boolean isEnabled() {
        return value;
    }
}