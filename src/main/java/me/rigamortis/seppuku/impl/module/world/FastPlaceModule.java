package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/23/2019 @ 12:58 PM.
 */
public final class FastPlaceModule extends Module {

    public FastPlaceModule() {
        super("FastPlace", new String[] {"Fp"}, "Removes place delay", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Minecraft.getMinecraft().rightClickDelayTimer = 6;
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if(event.getStage() == EventStageable.EventStage.PRE) {
            Minecraft.getMinecraft().rightClickDelayTimer = 0;
        }
    }

}
