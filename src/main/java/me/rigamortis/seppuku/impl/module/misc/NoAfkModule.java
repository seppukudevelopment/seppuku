package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 6/6/2019 @ 12:11 AM.
 */
public final class NoAfkModule extends Module {

    public NoAfkModule() {
        super("NoAFK", new String[]{"AntiAFK"}, "Prevents you from being kicked while idle", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            float yaw = mc.player.rotationYaw;
            yaw += (1 * Math.sin(mc.player.ticksExisted / Math.PI));
            Seppuku.INSTANCE.getRotationManager().setPlayerRotations(yaw, mc.player.rotationPitch);
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketPlayer.Rotation) {
                if (Minecraft.getMinecraft().player.getRidingEntity() != null) {
                    final CPacketPlayer.Rotation packet = (CPacketPlayer.Rotation) event.getPacket();
                    packet.yaw += (1 * Math.sin(Minecraft.getMinecraft().player.ticksExisted / Math.PI));
                }
            }
        }
    }

}
