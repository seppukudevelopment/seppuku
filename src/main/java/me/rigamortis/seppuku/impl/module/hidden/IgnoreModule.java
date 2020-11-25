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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Seth
 * @author noil
 */
public final class IgnoreModule extends Module {

    private final String REGEX_NAME = "<(\\S+)\\s*(\\S+?)?>\\s(.*)";

    public IgnoreModule() {
        super("Ignore", new String[]{"Ignor"}, "Allows you to ignore people client-side", "NONE", -1, ModuleType.HIDDEN);
        this.setHidden(true);
        this.toggle();
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChat && Minecraft.getMinecraft().player != null) {
                final SPacketChat packet = (SPacketChat) event.getPacket();
                if (packet.getChatComponent() instanceof TextComponentString) {
                    final TextComponentString component = (TextComponentString) packet.getChatComponent();
                    final boolean serverMessage = component.getFormattedText().startsWith("\247c") || component.getFormattedText().startsWith("\2474") || component.getFormattedText().startsWith("\2475");
                    final String message = StringUtils.stripControlCodes(component.getFormattedText());
                    if (!serverMessage && message.length() > 0) {
                        Pattern chatUsernamePattern = Pattern.compile(REGEX_NAME);
                        Matcher chatUsernameMatcher = chatUsernamePattern.matcher(message);
                        if (chatUsernameMatcher.find()) {
                            String username = chatUsernameMatcher.group(1).replaceAll(">", "").toLowerCase();
                            final Ignored ignored = Seppuku.INSTANCE.getIgnoredManager().find(username);
                            if (ignored == null && !username.equalsIgnoreCase(Minecraft.getMinecraft().session.getUsername())) {
                                event.setCanceled(true);
                            }
                        }
                    }
                }
            }
        }
    }

}
