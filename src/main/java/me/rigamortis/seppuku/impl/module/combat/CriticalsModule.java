package me.rigamortis.seppuku.impl.module.combat;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.OptionalValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 5/1/2019 @ 12:41 AM.
 */
public final class CriticalsModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Jump", "Packet"});

    public CriticalsModule() {
        super("Criticals", new String[] {"Crits"}, "Attempts to preform a critical while hitting entities", "NONE", -1, ModuleType.COMBAT);
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if(event.getStage() == EventStageable.EventStage.PRE) {
            if(event.getPacket() instanceof CPacketUseEntity) {
                final CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();
                if(packet.getAction() == CPacketUseEntity.Action.ATTACK) {
                    final Minecraft mc = Minecraft.getMinecraft();

                    if(mc.player.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && packet.getEntityFromWorld(mc.world) instanceof EntityLivingBase) {
                        switch (this.mode.getInt()) {
                            case 0:
                                mc.player.jump();
                                break;
                            case 1:
                                //TODO make sure u can actually go there
                                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1f, mc.player.posZ, false));
                                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                                break;
                        }
                    }
                }
            }
        }
    }

}
