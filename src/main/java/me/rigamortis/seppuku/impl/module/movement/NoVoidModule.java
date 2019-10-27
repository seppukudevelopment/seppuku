package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventMove;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/10/2019 @ 4:43 AM.
 */
public final class NoVoidModule extends Module {

    public final NumberValue height = new NumberValue("Height", new String[]{"hgt"}, 16, Integer.class, 0, 256, 1);

    public NoVoidModule() {
        super("NoVoid", new String[] {"AntiVoid"}, "Slows down movement when over the void", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            if(!mc.player.noClip) {
                if(mc.player.posY <= this.height.getInt()) {

                    final RayTraceResult trace = mc.world.rayTraceBlocks(mc.player.getPositionVector(), new Vec3d(mc.player.posX, 0, mc.player.posZ), false, false, false);

                    if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK) {
                        return;
                    }

                    mc.player.setVelocity(0, 0, 0);

                    if (mc.player.getRidingEntity() != null) {
                        mc.player.getRidingEntity().setVelocity(0, 0, 0);
                    }
                }
            }
        }
    }

}
