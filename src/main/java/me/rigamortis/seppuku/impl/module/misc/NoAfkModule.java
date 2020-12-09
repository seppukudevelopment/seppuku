package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 6/6/2019 @ 12:11 AM.
 */
public final class NoAfkModule extends Module {

    public final Value<Integer> yawOffset = new Value<Integer>("Yaw", new String[]{"yaw", "y"}, "The yaw to alternate each tick.", 1, 0, 180, 1);
    public final Value<Integer> pitchOffset = new Value<Integer>("Pitch", new String[]{"pitch", "x"}, "The pitch to alternate each tick.", 0, 0, 90, 1);

    public NoAfkModule() {
        super("NoAFK", new String[]{"AntiAFK"}, "Prevents you from being kicked while idle", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            float yaw = mc.player.rotationYaw;
            float pitch = mc.player.rotationPitch;
            if (this.yawOffset.getValue() > 0) {
                yaw += (this.yawOffset.getValue() * Math.sin(mc.player.ticksExisted / Math.PI));
            }
            if (this.pitchOffset.getValue() > 0) {
                pitch += (this.pitchOffset.getValue() * Math.sin(mc.player.ticksExisted / Math.PI));
            }
            Seppuku.INSTANCE.getRotationManager().setPlayerRotations(yaw, pitch);
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketPlayer.Rotation) {
                if (Minecraft.getMinecraft().player.getRidingEntity() != null) {
                    final CPacketPlayer.Rotation packet = (CPacketPlayer.Rotation) event.getPacket();
                    if (this.yawOffset.getValue() > 0) {
                        packet.yaw += (this.yawOffset.getValue() * Math.sin(Minecraft.getMinecraft().player.ticksExisted / Math.PI));
                    }
                    if (this.pitchOffset.getValue() > 0) {
                        packet.pitch += (this.pitchOffset.getValue() * Math.sin(Minecraft.getMinecraft().player.ticksExisted / Math.PI));
                    }
                }
            }
        }
    }

}
