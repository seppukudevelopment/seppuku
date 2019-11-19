package me.rigamortis.seppuku.api.event.module;

import me.rigamortis.seppuku.api.module.Module;

public final class EventModulePostLoaded {
    private final Module module;

    public EventModulePostLoaded(final Module module) {
        this.module = module;
    }

    public Module getModule() {
        return module;
    }
}
