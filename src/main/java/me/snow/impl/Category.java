package me.snow.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Category {
    PLAYER,
    DONUT,
    MISC,
    COMBAT,
    CLIENT,
    RENDER;

    private final List<Module> modules = new ArrayList<>();

    public void addModule(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }
}
