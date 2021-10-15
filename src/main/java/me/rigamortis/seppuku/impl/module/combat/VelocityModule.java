package me.rigamortis.seppuku.impl.module.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/17/2019 @ 5:25 AM.
 */
public final class VelocityModule extends Module {

    public final Value<Integer> horizontalVelocity = new Value<>("Horizontal", new String[]{"HorizontalVelocity", "Horizontal_Velocity", "HVel", "HV", "HorizontalVel", "Horizontal", "H"}, "The horizontal velocity you will take", 0, 0, 100, 1);
    public final Value<Integer> verticalVelocity = new Value<>("Vertical", new String[]{"VerticalVelocity", "Vertical_Velocity", "VVel", "VV", "VerticalVel", "Vertical", "Vert", "V"}, "The vertical velocity you will take", 0, 0, 100, 1);
    public final Value<Boolean> explosions = new Value<>("Explosions", new String[]{"Explosions", "Explosion", "EXP", "EX", "Expl"}, "Apply velocity modifier on explosion velocity", true);
    public final Value<Boolean> bobbers = new Value<>("Bobbers", new String[]{"Bobb", "Bob", "FishHook", "FishHooks"}, "Apply velocity modifier on fishing bobber velocity", true);

    public final Minecraft mc = Minecraft.getMinecraft();

    public VelocityModule() {
        super("Velocity", new String[]{"Vel", "AntiVelocity", "Knockback", "AntiKnockback"}, "Modify the velocity you take", "NONE", -1, ModuleType.COMBAT);
    }

    @Override
    public String getMetaData() {
        return String.format(ChatFormatting.WHITE + "H:%s%%" + ChatFormatting.GRAY + "|" + ChatFormatting.WHITE + "V:%s%%", this.horizontalVelocity.getValue(), this.verticalVelocity.getValue());
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (mc.player == null || mc.world == null)
                return;

            if (event.getPacket() instanceof SPacketEntityStatus && this.bobbers.getValue()) {
                final SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();
                if (packet.getOpCode() == 31) {
                    final Entity entity = packet.getEntity(mc.world);
                    if (entity instanceof EntityFishHook) {
                        final EntityFishHook fishHook = (EntityFishHook) entity;
                        if (fishHook.caughtEntity == mc.player) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
            if (event.getPacket() instanceof SPacketEntityVelocity) {
                final SPacketEntityVelocity packet = (SPacketEntityVelocity) event.getPacket();
                if (packet.getEntityID() == mc.player.getEntityId()) {
                    if (this.horizontalVelocity.getValue() == 0 && this.verticalVelocity.getValue() == 0) {
                        event.setCanceled(true);
                        return;
                    }

                    if (this.horizontalVelocity.getValue() != 100) {
                        packet.motionX = packet.motionX / 100 * this.horizontalVelocity.getValue();
                        packet.motionZ = packet.motionZ / 100 * this.horizontalVelocity.getValue();
                    }

                    if (this.verticalVelocity.getValue() != 100) {
                        packet.motionY = packet.motionY / 100 * this.verticalVelocity.getValue();
                    }
                }
            }
            if (event.getPacket() instanceof SPacketExplosion && this.explosions.getValue()) {
                final SPacketExplosion packet = (SPacketExplosion) event.getPacket();

                if (this.horizontalVelocity.getValue() == 0 && this.verticalVelocity.getValue() == 0) {
                    event.setCanceled(true);
                    return;
                }

                if (this.horizontalVelocity.getValue() != 100) {
                    packet.motionX = packet.motionX / 100 * this.horizontalVelocity.getValue();
                    packet.motionZ = packet.motionZ / 100 * this.horizontalVelocity.getValue();
                }

                if (this.verticalVelocity.getValue() != 100) {
                    packet.motionY = packet.motionY / 100 * this.verticalVelocity.getValue();
                }
            }
        }
    }

}
