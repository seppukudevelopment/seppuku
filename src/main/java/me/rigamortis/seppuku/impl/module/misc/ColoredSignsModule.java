package me.rigamortis.seppuku.impl.module.misc;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.network.play.client.CPacketUpdateSign;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/17/2019 @ 12:25 AM.
 */
public final class ColoredSignsModule extends Module {

    public ColoredSignsModule() {
        super("ColoredSigns", new String[] {"CSigns", "CSign", "SignColor", "SignColor"}, "Allows you to use the & character to color signs(Patched on spigot)", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if(event.getStage() == EventStageable.EventStage.PRE) {
            if(event.getPacket() instanceof CPacketUpdateSign) {
                final CPacketUpdateSign packet = (CPacketUpdateSign) event.getPacket();
                for(int i = 0; i < 4; i++) {
                    packet.lines[i] = packet.lines[i].replace("&", "\247" + "\247a");
                }
            }
        }
    }

}
