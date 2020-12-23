package me.rigamortis.seppuku.impl.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.FileUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.Vec2f;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author noil
 */
public final class NameAlertModule extends Module {

    public final Value<Boolean> saveToFile = new Value<Boolean>("SaveToFile", new String[]{"Save", "Saves"}, "Saves the alert to a file in your Seppuku 'Config' directory.", false);

    //private final String REGEX_NAME = "(?<=<).*?(?=>)";
    private final String REGEX_NAME = "<(\\S+)\\s*(\\S+?)?>\\s(.*)";

    private final File messagesFile;

    public NameAlertModule() {
        super("NameAlert", new String[]{"NameAlert", "SayMyName", "WhoSaid"}, "Alerts you when someone says your name in chat via a notification.", "NONE", -1, ModuleType.MISC);

        this.messagesFile = new File(Seppuku.INSTANCE.getConfigManager().getConfigDir(), "NameAlerts.txt");
        try {
            if (!this.messagesFile.exists())
                this.messagesFile.createNewFile();
        } catch (IOException e) {
            Seppuku.INSTANCE.getLogger().log(Level.WARNING, "Couldn't create NameAlert messages file.");
        }
    }

    @Listener
    public void onChat(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.POST) {
            if (event.getPacket() instanceof SPacketChat) {
                final SPacketChat packetChat = (SPacketChat) event.getPacket();
                String text = packetChat.getChatComponent().getFormattedText();
                final String localUsername = Minecraft.getMinecraft().getSession().getUsername();

                if ((text.contains(":") && text.toLowerCase().contains(ChatFormatting.LIGHT_PURPLE + "from")) ||
                        (text.toLowerCase().contains(ChatFormatting.GRAY + "") && StringUtils.stripControlCodes(text).contains("whispers to you"))) {
                    Seppuku.INSTANCE.getNotificationManager().addNotification("Whisper", "Someone whispered to you.");
                    if (this.saveToFile.getValue()) {
                        this.saveMessageToFile("Whisper", StringUtils.stripControlCodes(text));
                    }
                    return;
                }

                if (text.toLowerCase().contains(localUsername.toLowerCase())) {
                    text = StringUtils.stripControlCodes(text);
                    // code below is for public chat
                    Pattern chatUsernamePattern = Pattern.compile(REGEX_NAME);
                    Matcher chatUsernameMatcher = chatUsernamePattern.matcher(text);

                    if (chatUsernameMatcher.find()) {
                        String username = chatUsernameMatcher.group(1).replaceAll(">", "");
                        if (!username.equals(localUsername)) {
                            Seppuku.INSTANCE.getNotificationManager().addNotification("Public Chat", String.format("Someone mentioned you in chat. <%s>", username));
                            if (this.saveToFile.getValue()) {
                                this.saveMessageToFile(username, text);
                            }
                        }
                    }
                }
            }
        }
    }

    public void saveMessageToFile(String fromWho, String messageContent) {
        final String time = new SimpleDateFormat().format(new Date());
        final String host = Minecraft.getMinecraft().getCurrentServerData() != null ? Minecraft.getMinecraft().getCurrentServerData().serverIP : "localhost";

        final List<String> linesToAdd = new ArrayList<>();
        final String data = String.format("server: %s, date: %s, from: %s, message: %s", host, time, fromWho, messageContent);
        linesToAdd.add(data);

        FileUtil.write(this.messagesFile, linesToAdd, false);
    }
}
