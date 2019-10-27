package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.value.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/17/2019 @ 5:25 AM.
 */
public final class VelocityModule extends Module {

    public final NumberValue horizontal_vel = new NumberValue("Horizontal_Velocity", new String[]{"Horizontal_Velocity", "HVel", "HV", "HorizontalVel"}, 0, Integer.class, 0, 100, 1);
    public final NumberValue vertical_vel = new NumberValue("Vertical_Velocity", new String[]{"Vertical_Velocity", "VVel", "VV", "VerticalVel"}, 0, Integer.class, 0, 100, 1);
    public final BooleanValue explosions = new BooleanValue("Explosions", new String[]{"Explosions", "Explosion", "EXP", "EX", "Expl"}, true);

    public VelocityModule() {
        super("Velocity", new String[]{"Vel", "AntiVelocity", "Knockback", "AntiKnockback"}, "Modify the velocity you take", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketEntityVelocity) {
                final SPacketEntityVelocity packet = (SPacketEntityVelocity) event.getPacket();
                if (packet.getEntityID() == Minecraft.getMinecraft().player.getEntityId()) {
                    if (this.horizontal_vel.getInt() == 0 && this.vertical_vel.getInt() == 0) {
                        event.setCanceled(true);
                        return;
                    }

                    if (this.horizontal_vel.getInt() != 100) {
                        packet.motionX = packet.motionX / 100 * this.horizontal_vel.getInt();
                        packet.motionZ = packet.motionZ / 100 * this.horizontal_vel.getInt();
                    }

                    if (this.vertical_vel.getInt() != 100) {
                        packet.motionY = packet.motionY / 100 * this.vertical_vel.getInt();
                    }
                }
            }
            if (event.getPacket() instanceof SPacketExplosion && this.explosions.getBoolean()) {
                final SPacketExplosion packet = (SPacketExplosion) event.getPacket();

                if (this.horizontal_vel.getInt() == 0 && this.vertical_vel.getInt() == 0) {
                    event.setCanceled(true);
                    return;
                }

                if (this.horizontal_vel.getInt() != 100) {
                    packet.motionX = packet.motionX / 100 * this.horizontal_vel.getInt();
                    packet.motionZ = packet.motionZ / 100 * this.horizontal_vel.getInt();
                }

                if (this.vertical_vel.getInt() != 100) {
                    packet.motionY = packet.motionY / 100 * this.vertical_vel.getInt();
                }
            }
        }
    }

}
