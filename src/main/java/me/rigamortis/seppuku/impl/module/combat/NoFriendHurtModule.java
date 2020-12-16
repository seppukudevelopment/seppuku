package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.friend.Friend;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketUseEntity;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * @author noil
 */
public final class NoFriendHurtModule extends Module {

    public NoFriendHurtModule() {
        super("NoFriendHurt", new String[]{"NoFriendDMG", "FriendProtect"}, "Cancels any packets that try to hurt your friends.", "NONE", -1, ModuleType.COMBAT);
    }

    @Listener
    public void onSendPacket(EventSendPacket event) {
        if (event.getStage().equals(EventStageable.EventStage.PRE)) {
            if (event.getPacket() instanceof CPacketUseEntity) {
                final CPacketUseEntity packetUseEntity = (CPacketUseEntity) event.getPacket();
                if (Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().objectMouseOver == null)
                    return;

                final Friend friend = Seppuku.INSTANCE.getFriendManager().isFriend(Minecraft.getMinecraft().objectMouseOver.entityHit);
                if (packetUseEntity.getAction() == CPacketUseEntity.Action.ATTACK && friend != null) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
