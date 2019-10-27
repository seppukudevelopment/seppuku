package me.rigamortis.seppuku.impl.module.render;

import me.rigamortis.seppuku.api.event.render.EventHurtCamEffect;
import me.rigamortis.seppuku.api.module.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/11/2019 @ 2:55 AM.
 */
public final class NoHurtCamModule extends Module {

    public NoHurtCamModule() {
        super("NoHurtCam", new String[] {"AntiHurtCam"}, "Removes hurt camera effects", "NONE", -1, ModuleType.RENDER);
    }

    @Listener
    public void hurtCamEffect(EventHurtCamEffect event) {
        event.setCanceled(true);
    }

}
