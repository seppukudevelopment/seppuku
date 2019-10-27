package me.rigamortis.seppuku.impl.module.player;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Author Seth
 * 4/9/2019 @ 9:14 AM.
 */
public final class BlinkModule extends Module {

    private EntityOtherPlayerMP entity;
    private final Queue<Packet> packets = new ConcurrentLinkedQueue();

    public BlinkModule() {
        super("Blink", new String[]{"FakeLag"}, "Holds packets until disabled", "NONE", -1, ModuleType.PLAYER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != null) {
            this.entity = new EntityOtherPlayerMP(mc.world, mc.session.getProfile());
            this.entity.copyLocationAndAnglesFrom(mc.player);
            this.entity.rotationYaw = mc.player.rotationYaw;
            this.entity.rotationYawHead = mc.player.rotationYawHead;
            this.entity.inventory.copyInventory(mc.player.inventory);
            mc.world.addEntityToWorld(6942069, this.entity);
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Packet packet = event.getPacket();

            if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().isSingleplayer()) {
                return;
            }

            if (packet instanceof CPacketChatMessage || packet instanceof CPacketConfirmTeleport || packet instanceof CPacketKeepAlive || packet instanceof CPacketTabComplete || packet instanceof CPacketClientStatus) {
                return;
            }

            this.packets.add(packet);
            event.setCanceled(true);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (Minecraft.getMinecraft().world != null) {
            if (this.entity != null) {
                Minecraft.getMinecraft().world.removeEntity(this.entity);
            }
            if (this.packets.size() > 0) {
                for (Packet packet : this.packets) {
                    Minecraft.getMinecraft().player.connection.sendPacket(packet);
                }
                this.packets.clear();
            }
        }
    }

}
