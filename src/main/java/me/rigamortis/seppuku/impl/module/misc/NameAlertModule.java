package me.rigamortis.seppuku.impl.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.StringUtils;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author noil
 */
public final class NameAlertModule extends Module {

    //private final String REGEX_NAME = "(?<=<).*?(?=>)";
    private final String REGEX_NAME = "<(\\S+)\\s*(\\S+?)?>\\s(.*)";

    public NameAlertModule() {
        super("NameAlert", new String[]{"NameAlert", "SayMyName", "WhoSaid"}, "Alerts you when someone says your name in chat via a notification.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onChat(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.POST) {
            if (event.getPacket() instanceof SPacketChat) {
                final SPacketChat packetChat = (SPacketChat) event.getPacket();
                String text = packetChat.getChatComponent().getFormattedText();
                final String localUsername = Minecraft.getMinecraft().getSession().getUsername();

                if (text.contains(":") && text.toLowerCase().contains(ChatFormatting.LIGHT_PURPLE + "from")) {
                    Seppuku.INSTANCE.getNotificationManager().addNotification("Whisper", "Someone whispered to you.");
                    return;
                }

                if (text.contains(localUsername)) {
                    text = StringUtils.stripControlCodes(text);
                    // code below is for public chat
                    Pattern chatUsernamePattern = Pattern.compile(REGEX_NAME);
                    Matcher chatUsernameMatcher = chatUsernamePattern.matcher(text);

                    if (chatUsernameMatcher.find()) {
                        String username = chatUsernameMatcher.group(1).replaceAll(">", "");
                        if (!username.equals(localUsername)) {
                            Seppuku.INSTANCE.getNotificationManager().addNotification("Public Chat", String.format("Someone mentioned you in chat. <%s>", username));
                        }
                    }
                }
            }
        }
    }
}
