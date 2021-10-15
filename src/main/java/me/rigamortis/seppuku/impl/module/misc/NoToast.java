package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.render.EventDrawToast;
import me.rigamortis.seppuku.api.module.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class NoToast extends Module {

    public NoToast() {
        super("NoToast", new String[]{"Toast"}, "Prevents toasts from being displayed on screen. (Achievements, etc.)", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void onToast(EventDrawToast event) {
        event.setCanceled(true);
    }
}
