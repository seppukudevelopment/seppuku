package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.task.rotation.RotationTask;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author Seth
 * @author noil
 */
public final class NoAfkModule extends Module {

    public final Value<Integer> yawOffset = new Value<Integer>("Yaw", new String[]{"yaw", "y"}, "The yaw to alternate each tick.", 1, 0, 180, 1);

    private final RotationTask rotationTask = new RotationTask("NoAFKTask", 1); /* 1 == low priority */

    public NoAfkModule() {
        super("NoAFK", new String[]{"AntiAFK"}, "Prevents you from being kicked while idle", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            float yaw = mc.player.rotationYaw;
            float pitch = mc.player.rotationPitch;
            yaw += (this.yawOffset.getValue() * Math.sin(mc.player.ticksExisted / Math.PI));

            Seppuku.INSTANCE.getRotationManager().startTask(this.rotationTask);
            if (this.rotationTask.isOnline()) {
                Seppuku.INSTANCE.getRotationManager().setPlayerRotations(yaw, pitch);
                //Seppuku.INSTANCE.getRotationManager().finishTask(this.rotationTask);
            }
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketPlayer.Rotation) {
                if (Minecraft.getMinecraft().player.getRidingEntity() != null) {
                    final CPacketPlayer.Rotation packet = (CPacketPlayer.Rotation) event.getPacket();
                    packet.yaw += (this.yawOffset.getValue() * Math.sin(Minecraft.getMinecraft().player.ticksExisted / Math.PI));
                }
            }
        }
    }

}
