package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public class PearlDupeModule extends Module {
    private final Value<String> message = new Value<>("Message", new String[] { "msg", "text" }, "Command to send", "kill");

//    private static final int TP_DISTANCE = 16;

    public PearlDupeModule() {
        super("PearlDupe", new String[] { "pearldupe", "dupepearl", "pearlexploit" }, "Sends /kill (or another command) when receiving a teleport position packet. Be careful!", "NONE", -1, Module.ModuleType.MISC);
    }

    @Listener
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getStage() != EventStageable.EventStage.PRE)
            return;

        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            final Minecraft mc = Minecraft.getMinecraft();
            final EntityPlayerSP localPlayer = mc.player;
            if (localPlayer == null)
                return;
            if (localPlayer.ticksExisted < 20)
                return;
            if (isPearlOrFarTeleport(localPlayer, (SPacketPlayerPosLook)event.getPacket()))
                sendKillMessage(localPlayer);
            toggle();
        }
    }

    private boolean isPearlOrFarTeleport(EntityPlayerSP localPlayer, SPacketPlayerPosLook packetPlayerPosLook) {
        return (localPlayer.getDistance(packetPlayerPosLook.getX(), packetPlayerPosLook.getY(), packetPlayerPosLook.getZ()) > 16.0D);
    }

    private void sendKillMessage(EntityPlayerSP localPlayer) {
        localPlayer.sendChatMessage("/" + this.message.getValue());
    }
}
