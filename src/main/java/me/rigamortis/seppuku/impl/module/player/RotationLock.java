package me.rigamortis.seppuku.impl.module.player;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.BooleanValue;
import net.minecraft.client.Minecraft;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/1/2019 @ 7:56 PM.
 */
public final class RotationLock extends Module {

    public final BooleanValue yawLock = new BooleanValue("Yaw", new String[]{"Y"}, true);
    public final BooleanValue pitchLock = new BooleanValue("Pitch", new String[]{"P"}, false);

    private float yaw;
    private float pitch;

    public RotationLock() {
        super("RotationLock", new String[]{"RotLock", "Rotation"}, "Locks you rotation for precision", "NONE", -1, ModuleType.PLAYER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if(Minecraft.getMinecraft().player != null) {
            this.yaw = Minecraft.getMinecraft().player.rotationYaw;
            this.pitch = Minecraft.getMinecraft().player.rotationPitch;
        }
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        Seppuku.INSTANCE.getRotationManager().updateRotations();
        if(this.yawLock.getBoolean()) {
            Seppuku.INSTANCE.getRotationManager().setPlayerYaw(this.yaw);
        }
        if(this.pitchLock.getBoolean()) {
            Seppuku.INSTANCE.getRotationManager().setPlayerPitch(this.pitch);
        }
    }

}
