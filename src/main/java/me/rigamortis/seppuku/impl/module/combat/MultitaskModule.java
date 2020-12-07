package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.api.event.player.EventHandActive;
import me.rigamortis.seppuku.api.event.player.EventHittingBlock;
import me.rigamortis.seppuku.api.event.player.EventResetBlockRemoving;
import me.rigamortis.seppuku.api.module.Module;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public final class MultitaskModule extends Module {

    public MultitaskModule() {
        super("Multitask", new String[]{"multi", "task"}, "Allows the player to perform multiple actions at once. (eating, placing, attacking)", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onActiveHand(EventHandActive event) {
        event.setCanceled(true);
    }

    @Listener
    public void onHittingBlock(EventHittingBlock event) {
        event.setCanceled(true);
    }

    @Listener
    public void onResetBlockRemoving(EventResetBlockRemoving event) {
        event.setCanceled(true);
    }
}
