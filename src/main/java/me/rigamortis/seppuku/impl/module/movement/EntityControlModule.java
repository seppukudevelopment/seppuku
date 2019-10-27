package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.entity.EventHorseSaddled;
import me.rigamortis.seppuku.api.event.entity.EventPigTravel;
import me.rigamortis.seppuku.api.event.entity.EventSteerEntity;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityPig;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/24/2019 @ 8:53 PM.
 */
public final class EntityControlModule extends Module {

    public EntityControlModule() {
        super("EntityControl", new String[]{"AntiSaddle", "EntityRide", "NoSaddle"}, "Allows you to control llamas, horses, pigs without a saddle/carrot", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Listener
    public void pigTravel(EventPigTravel event) {
        final Minecraft mc = Minecraft.getMinecraft();
        final boolean moving = mc.player.movementInput.moveForward != 0 || mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.jump;

        final Entity riding = mc.player.getRidingEntity();

        if (riding != null && riding instanceof EntityPig) {
            if (!moving && riding.onGround) {
                event.setCanceled(true);
            }
        }
    }

    @Listener
    public void steerEntity(EventSteerEntity event) {
        event.setCanceled(true);
    }

    @Listener
    public void horseSaddled(EventHorseSaddled event) {
        event.setCanceled(true);
    }

}
