package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.api.event.world.EventCanCollide;
import me.rigamortis.seppuku.api.module.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 6/5/2019 @ 9:03 PM.
 */
public final class LiquidInteractModule extends Module {

    public LiquidInteractModule() {
        super("LiquidInteract", new String[] {"LiquidInt", "LiqInt"}, "Allows you to interact with liquids", "NONE", -1, ModuleType.WORLD);
    }

    @Listener
    public void canCollide(EventCanCollide event) {
        event.setCanceled(true);
    }

}
