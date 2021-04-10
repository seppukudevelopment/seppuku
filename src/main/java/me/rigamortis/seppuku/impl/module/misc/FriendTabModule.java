package me.rigamortis.seppuku.impl.module.misc;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.gui.EventGetGuiTabName;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.friend.Friend;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.network.play.server.SPacketPlayerListHeaderFooter;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Written by TBM
 */
public final class FriendTabModule extends Module {

    public FriendTabModule() {
        super("FriendTab", new String[]{"FTab", "FriendT", "FriendTabOverlay", "FriendTabHighlight"}, "Displays friends names in tab as a different colour and as their nickname.", "NONE", -1, ModuleType.MISC);
    }

    @Listener
    public void onGetGuiTabName(EventGetGuiTabName event) {
        final Friend friend = Seppuku.INSTANCE.getFriendManager().find(event.getName());
        if (friend != null) event.setName("\247d" + friend.getAlias());
    }

}
