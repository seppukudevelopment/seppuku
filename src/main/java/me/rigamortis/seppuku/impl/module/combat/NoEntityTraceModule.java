package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.api.event.player.EventGetMouseOver;
import me.rigamortis.seppuku.api.module.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public final class NoEntityTraceModule extends Module {

    public NoEntityTraceModule() {
        super("NoEntityTrace", new String[]{"NoMiningTrace", "EntityTrace", "MiningTrace", "NoBB"}, "Mine through entities by overriding the moused over entity-list.", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onGetMouseOver(EventGetMouseOver event) {
        event.setCanceled(true);
    }
}
