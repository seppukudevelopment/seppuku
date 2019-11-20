package me.rigamortis.seppuku.impl.management;

import me.rigamortis.seppuku.Seppuku;
import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.player.EventPlayerJoin;
import me.rigamortis.seppuku.api.event.player.EventPlayerLeave;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 7/23/2019 @ 7:38 AM.
 */
public final class JoinLeaveManager {

    public JoinLeaveManager() {
        Seppuku.INSTANCE.getEventManager().addEventListener(this);
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof SPacketPlayerListItem) {
                final SPacketPlayerListItem packet = (SPacketPlayerListItem) event.getPacket();
                final Minecraft mc = Minecraft.getMinecraft();
                if (mc.player != null && mc.player.ticksExisted >= 1000) {
                    if (packet.getAction() == SPacketPlayerListItem.Action.ADD_PLAYER) {
                        for (SPacketPlayerListItem.AddPlayerData playerData : packet.getEntries()) {
                            if (playerData.getProfile().getId() != mc.session.getProfile().getId()) {
                                new Thread(() -> {
                                    final String name = Seppuku.INSTANCE.getApiManager().resolveName(playerData.getProfile().getId().toString());
                                    if (name != null) {
                                        Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventPlayerJoin(name, playerData.getProfile().getId().toString()));
                                    }
                                }).start();
                            }
                        }
                    }
                    if (packet.getAction() == SPacketPlayerListItem.Action.REMOVE_PLAYER) {
                        for (SPacketPlayerListItem.AddPlayerData playerData : packet.getEntries()) {
                            if (playerData.getProfile().getId() != mc.session.getProfile().getId()) {
                                new Thread(() -> {
                                    final String name = Seppuku.INSTANCE.getApiManager().resolveName(playerData.getProfile().getId().toString());
                                    if (name != null) {
                                        Seppuku.INSTANCE.getEventManager().dispatchEvent(new EventPlayerLeave(name, playerData.getProfile().getId().toString()));
                                    }
                                }).start();
                            }
                        }
                    }
                }
            }
        }
    }

    public void unload() {
        Seppuku.INSTANCE.getEventManager().removeEventListener(this);
    }
}
