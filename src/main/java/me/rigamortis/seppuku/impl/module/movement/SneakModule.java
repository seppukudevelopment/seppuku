package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.OptionalValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/22/2019 @ 5:12 AM.
 */
public final class SneakModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Vanilla", "NCP"});

    public SneakModule() {
        super("Sneak", new String[]{"Sneek"}, "Allows you to sneak at full speed", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (Minecraft.getMinecraft().world != null && !Minecraft.getMinecraft().player.isSneaking()) {
            Minecraft.getMinecraft().player.connection.sendPacket(new CPacketEntityAction(Minecraft.getMinecraft().player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (event.getStage() == EventStageable.EventStage.PRE) {
            switch (this.mode.getInt()) {
                case 0:
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                    break;
                case 1:
                    if (!mc.player.isSneaking()) {
                        if (isMoving()) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                        } else {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                        }
                    }
                    break;
            }
        } else {
            if (this.mode.getInt() == 1) {
                if (isMoving()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                }
            }
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && !Minecraft.getMinecraft().player.isSneaking()) {
                Minecraft.getMinecraft().player.connection.sendPacket(new CPacketEntityAction(Minecraft.getMinecraft().player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
        }
    }

    private boolean isMoving() {
        return GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindForward) || GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindLeft) || GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindRight) || GameSettings.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindBack);
    }

}
