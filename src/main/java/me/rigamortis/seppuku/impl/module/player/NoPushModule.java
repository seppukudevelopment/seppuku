package me.rigamortis.seppuku.impl.module.player;

import me.rigamortis.seppuku.api.event.player.EventApplyCollision;
import me.rigamortis.seppuku.api.event.player.EventPushOutOfBlocks;
import me.rigamortis.seppuku.api.event.player.EventPushedByWater;
import me.rigamortis.seppuku.api.module.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/9/2019 @ 12:52 AM.
 */
public final class NoPushModule extends Module {

    public NoPushModule() {
        super("NoPush", new String[]{"AntiPush"}, "Disable collision with entities, blocks and water", "NONE", -1, ModuleType.PLAYER);
    }

    @Listener
    public void pushOutOfBlocks(EventPushOutOfBlocks event) {
        event.setCanceled(true);
    }

    @Listener
    public void pushedByWater(EventPushedByWater event) {
        event.setCanceled(true);
    }

    @Listener
    public void applyCollision(EventApplyCollision event) {
        event.setCanceled(true);
    }

}
