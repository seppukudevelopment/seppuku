package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.ignore.Ignored;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextComponentString;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 7/1/2019 @ 10:22 PM.
 */
public final class AutoIgnoreModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The auto ignore mode to use.", Mode.CLIENT);

    private List<String> blacklist = new ArrayList<>();

    public AutoIgnoreModule() {
        super("AutoIgnore", new String[]{"AutomaticIgnore", "AIG", "AIgnore"}, "Automatically ignores someone if they say a certain word or phrase", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    public boolean blacklistContains(String message) {
        for (String s : this.blacklist) {
            if (message.toLowerCase().contains(s.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChat) {
                final SPacketChat packet = (SPacketChat) event.getPacket();
                if (packet.getChatComponent() instanceof TextComponentString) {
                    final TextComponentString component = (TextComponentString) packet.getChatComponent();
                    final String message = StringUtils.stripControlCodes(component.getUnformattedText());

                    final boolean serverMessage = message.startsWith("\247c") || message.startsWith("\2475");

                    if (!serverMessage && this.blacklistContains(message)) {
                        final String[] split = message.split(" ");

                        if (split != null) {
                            final String username = split[0].replace("<", "").replace(">", "");
                            final Ignored ignored = Seppuku.INSTANCE.getIgnoredManager().find(username);
                            if (ignored == null && !username.equalsIgnoreCase(Minecraft.getMinecraft().session.getUsername())) {
                                switch (this.mode.getValue()) {
                                    case CLIENT:
                                        Seppuku.INSTANCE.getIgnoredManager().add(username);
                                        Seppuku.INSTANCE.logChat("Added \247c" + username + "\247f to your ignore list");
                                        break;
                                    case SERVER:
                                        Seppuku.INSTANCE.getChatManager().add("/ignore " + username);
                                        break;
                                    case BOTH:
                                        Seppuku.INSTANCE.getChatManager().add("/ignore " + username);
                                        Seppuku.INSTANCE.getIgnoredManager().add(username);
                                        Seppuku.INSTANCE.logChat("Added \247c" + username + "\247f to your ignore list");
                                        break;
                                }
                            }
                        }
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    private enum Mode {
        CLIENT, SERVER, BOTH
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(List<String> blacklist) {
        this.blacklist = blacklist;
    }
}
