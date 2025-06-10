package me.snow.impl.settings;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;
    private int currentIndex;

    public ModeSetting(String name, String description, String defaultValue, String... modes) {
        super(name, description, defaultValue);
        this.modes = Arrays.asList(modes);
        this.currentIndex = this.modes.indexOf(defaultValue);
        if (this.currentIndex == -1) {
            this.currentIndex = 0;
            this.value = this.modes.get(0);
        }
    }

    @Override
    public void setValue(String value) {
        int index = modes.indexOf(value);
        if (index != -1) {
            this.currentIndex = index;
            super.setValue(value);
        }
    }

    public void setMode(int index) {
        if (index >= 0 && index < modes.size()) {
            this.currentIndex = index;
            super.setValue(modes.get(index));
        }
    }

    public void nextMode() {
        setMode((currentIndex + 1) % modes.size());
    }

    public void previousMode() {
        setMode((currentIndex - 1 + modes.size()) % modes.size());
    }

    public List<String> getModes() { return modes; }
    public int getCurrentIndex() { return currentIndex; }
    public boolean is(String mode) { return value.equalsIgnoreCase(mode); }
}