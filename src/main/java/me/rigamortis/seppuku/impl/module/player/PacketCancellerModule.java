package me.rigamortis.seppuku.impl.module.player;


import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.network.play.server.SPacketChat;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

public final class PacketCancellerModule extends Module {

        public PacketCancellerModule() {
                super("PacketCanceller", new String[]{"antipacket"}, "Holds packets until disabled", "NONE", -1, ModuleType.PLAYER);
        }




        @Listener
        public void receivePacket(EventReceivePacket event) {
                if (event.getPacket() instanceof SPacketChat) {

event.setCanceled(true);
                }
        }
}