package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/11/2019 @ 7:30 AM.
 */
public final class HorseJumpModule extends Module {

    public HorseJumpModule() {
        super("HorseJump", new String[] {"JumpPower", "HJump"}, "Makes horses and llamas jump at max height", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if(event.getStage() == EventStageable.EventStage.PRE) {
            Minecraft.getMinecraft().player.horseJumpPower = 1;
            Minecraft.getMinecraft().player.horseJumpPowerCounter = -10;
        }
    }

}
