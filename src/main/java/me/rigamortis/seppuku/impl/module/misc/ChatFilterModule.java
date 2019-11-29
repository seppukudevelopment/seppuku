package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.StringUtil;
import me.rigamortis.seppuku.api.value.old.BooleanValue;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentString;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 5/25/2019 @ 4:34 AM.
 */
public final class ChatFilterModule extends Module {

    public final BooleanValue unicode = new BooleanValue("Unicode", new String[]{"uc"}, true);
    public final BooleanValue broadcasts = new BooleanValue("Broadcasts", new String[]{"broad", "bc"}, true);
    public final BooleanValue spam = new BooleanValue("Spam", new String[]{"sp", "s"}, true);

    private List<String> cache = new ArrayList<>();

    public ChatFilterModule() {
        super("ChatFilter", new String[]{"CFilter"}, "Filters out annoying chat messages", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onDisable() {
        super.onDisable();
        this.cache.clear();
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChat) {
                final SPacketChat packet = (SPacketChat) event.getPacket();

                if (this.broadcasts.getBoolean()) {
                    if (packet.getChatComponent().getFormattedText().startsWith("\2475[SERVER]")) {
                        event.setCanceled(true);
                    }
                }

                if (this.spam.getBoolean()) {
                    final String chat = packet.getChatComponent().getUnformattedText();

                    if (this.cache.size() > 0) {
                        for (String s : this.cache) {
                            final double diff = StringUtil.levenshteinDistance(s, chat);

                            if (diff >= 0.75f) {
                                event.setCanceled(true);
                            }
                        }
                    }

                    this.cache.add(chat);

                    if (this.cache.size() >= 10) {
                        this.cache.remove(0);
                    }
                }

                if (this.unicode.getBoolean()) {
                    if (packet.getChatComponent() instanceof TextComponentString) {
                        final TextComponentString component = (TextComponentString) packet.getChatComponent();

                        final StringBuilder sb = new StringBuilder();

                        boolean containsUnicode = false;

                        for (String s : component.getFormattedText().split(" ")) {

                            String line = "";

                            for (char c : s.toCharArray()) {
                                if (c >= 0xFEE0) {
                                    c -= 0xFEE0;
                                    containsUnicode = true;
                                }

                                line += c;
                            }

                            sb.append(line + " ");
                        }

                        if (containsUnicode) {
                            packet.chatComponent = new TextComponentString(sb.toString());
                        }
                    }
                }
            }
        }
    }

}
