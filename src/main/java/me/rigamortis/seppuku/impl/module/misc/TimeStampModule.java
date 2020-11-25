package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.network.play.client.CPacketUpdateSign;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author Seth
 * 5/11/2019 @ 7:37 AM.
 */
public final class TimeStampModule extends Module {

    public TimeStampModule() {
        super("TimeStamp", new String[]{"Stamp"}, "Automatically adds the date at the bottom of signs", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketUpdateSign) {
                final CPacketUpdateSign packet = (CPacketUpdateSign) event.getPacket();

                if (packet.lines[3].length() <= 0) {
                    packet.lines[3] = new SimpleDateFormat("MMM dd, yyyy").format(new Date());
                }
            }
        }
    }

}
