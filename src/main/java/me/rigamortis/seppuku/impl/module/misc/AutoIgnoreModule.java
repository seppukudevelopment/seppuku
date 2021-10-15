package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.friend.Friend;
import me.rigamortis.seppuku.api.ignore.Ignored;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Seth
 * @author noil
 */
public final class AutoIgnoreModule extends Module {

    private static final String REGEX_NAME = "<(\\S+)\\s*(\\S+?)?>\\s(.*)";

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The auto ignore mode to use", Mode.CLIENT);
    public final Value<Boolean> allowFriends = new Value<Boolean>("AllowFriends", new String[]{"AllowF", "Friends", "AF", "F"}, "If enabled, any friend's message will not be auto-ignored", true);

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
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketChat) {
                final SPacketChat packet = (SPacketChat) event.getPacket();
                if (packet.getChatComponent() instanceof TextComponentString) {
                    final TextComponentString component = (TextComponentString) packet.getChatComponent();
                    final String message = StringUtils.stripControlCodes(component.getFormattedText());
                    final boolean serverMessage = component.getFormattedText().startsWith("\247c") || component.getFormattedText().startsWith("\247e") || component.getFormattedText().startsWith("\2475");
                    if (!serverMessage && this.blacklistContains(message)) {
                        Pattern chatUsernamePattern = Pattern.compile(REGEX_NAME);
                        Matcher chatUsernameMatcher = chatUsernamePattern.matcher(message);
                        if (chatUsernameMatcher.find()) {
                            String username = chatUsernameMatcher.group(1).replaceAll(">", "").toLowerCase();

                            // Check if the user is a friend
                            if (this.allowFriends.getValue()) {
                                final Friend friend = Seppuku.INSTANCE.getFriendManager().find(username);
                                if (friend != null) {
                                    return;
                                }
                            }

                            final Ignored ignored = Seppuku.INSTANCE.getIgnoredManager().find(username);
                            if (ignored == null && !username.equalsIgnoreCase(Minecraft.getMinecraft().session.getUsername())) {
                                switch (this.mode.getValue()) {
                                    case CLIENT:
                                        Seppuku.INSTANCE.getIgnoredManager().add(username);

                                        ITextComponent ignoreMessage = (new TextComponentString("Added ")).appendSibling(
                                                new TextComponentString("\247c" + username)
                                                        .setStyle(new Style()
                                                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, packet.chatComponent)))
                                        ).appendSibling(new TextComponentString("\247f to your ignore list"));

                                        Seppuku.INSTANCE.logcChat(ignoreMessage);
                                        break;
                                    case SERVER:
                                        Seppuku.INSTANCE.getChatManager().add("/ignore " + username);

                                        ignoreMessage = (new TextComponentString("Ignored ")).appendSibling(
                                                new TextComponentString("\247c" + username)
                                                        .setStyle(new Style()
                                                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, packet.chatComponent))));

                                        Seppuku.INSTANCE.logcChat(ignoreMessage);
                                        break;
                                    case BOTH:
                                        Seppuku.INSTANCE.getChatManager().add("/ignore " + username);
                                        Seppuku.INSTANCE.getIgnoredManager().add(username);

                                        ignoreMessage = (new TextComponentString("Added ")).appendSibling(
                                                new TextComponentString("\247c" + username)
                                                        .setStyle(new Style()
                                                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, packet.chatComponent)))
                                        ).appendSibling(new TextComponentString("\247f to your ignore list"));

                                        Seppuku.INSTANCE.logcChat(ignoreMessage);
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
