package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.OptionalValue;
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

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"12", "24"});

    public ChatTimeStampsModule() {
        super("ChatTimeStamps", new String[]{"ChatStamp", "ChatStamps"}, "Appends a time stamp on chat messages", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChat) {
                final SPacketChat packet = (SPacketChat) event.getPacket();

                if (packet.getChatComponent() instanceof TextComponentString) {
                    final TextComponentString component = (TextComponentString) packet.getChatComponent();

                    String date = "";

                    switch (this.mode.getInt()) {
                        case 0:
                            date = new SimpleDateFormat("h:mm a").format(new Date());
                            break;
                        case 1:
                            date = new SimpleDateFormat("k:mm").format(new Date());
                            break;
                    }

                    component.text = "\2477[" + date + "]\247r " + component.getText();
                }
            }
        }
    }

}
