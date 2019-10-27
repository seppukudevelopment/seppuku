package me.rigamortis.seppuku.impl.module.hidden;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.ignore.Ignored;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextComponentString;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 6/29/2019 @ 5:07 AM.
 */
public final class IgnoreModule extends Module {

    public IgnoreModule() {
        super("Ignore", new String[] {"Ignor"}, "Allows you to ignore people client-side", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
        this.toggle();
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if(event.getPacket() instanceof SPacketChat && Minecraft.getMinecraft().player != null) {
                final SPacketChat packet = (SPacketChat) event.getPacket();
                if (packet.getChatComponent() instanceof TextComponentString) {
                    final TextComponentString component = (TextComponentString) packet.getChatComponent();

                    final String message = StringUtils.stripControlCodes(component.getUnformattedText());

                    final boolean serverMessage = message.startsWith("\247c") || message.startsWith("\2475");

                    if (!serverMessage && message.length() > 0) {
                        final String[] split = message.split(" ");

                        if (split != null) {
                            final Ignored ignored = Seppuku.INSTANCE.getIgnoredManager().find(split[0].replace("<", "").replace(">", ""));
                            if (ignored != null) {
                                event.setCanceled(true);
                            }
                        }
                    }
                }
            }
        }
    }

}
