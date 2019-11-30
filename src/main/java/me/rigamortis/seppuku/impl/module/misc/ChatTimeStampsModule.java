package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentString;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author Seth
 * 8/16/2019 @ 6:11 AM.
 */
public final class ChatTimeStampsModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "Time format, 12 hour or 24 hour.", Mode.TWELVE);

    private enum Mode {
        TWELVE, TWENTY_FOUR
    }


    public ChatTimeStampsModule() {
        super("ChatTimeStamps", new String[]{"ChatStamp", "ChatStamps"}, "Appends a time stamp on chat messages", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChat) {
                final SPacketChat packet = (SPacketChat) event.getPacket();

                if (packet.getChatComponent() instanceof TextComponentString) {
                    final TextComponentString component = (TextComponentString) packet.getChatComponent();

                    String date = "";

                    switch (this.mode.getValue()) {
                        case TWELVE:
                            date = new SimpleDateFormat("h:mm a").format(new Date());
                            break;
                        case TWENTY_FOUR:
                            date = new SimpleDateFormat("k:mm").format(new Date());
                            break;
                    }

                    component.text = "\2477[" + date + "]\247r " + component.getText();
                }
            }
        }
    }

}
