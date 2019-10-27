package me.rigamortis.seppuku.impl.module.player;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.minecraft.EventDisplayGui;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.value.OptionalValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumHand;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/9/2019 @ 9:06 AM.
 */
public final class GodModeModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Portal", "Riding", "Lock", "Packet"});

    public final BooleanValue footsteps = new BooleanValue("FootSteps", new String[]{"Foot"}, false);

    private Entity riding;

    public GodModeModule() {
        super("GodMode", new String[]{"God", "Pgm", "PortalGodmode", "PortalGod", "SickoMode"}, "Makes you invincible in certain cases", "NONE", -1, ModuleType.PLAYER);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (this.mode.getInt() == 1) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (mc.player.getRidingEntity() != null) {
                this.riding = mc.player.getRidingEntity();
                mc.player.dismountRidingEntity();
                mc.world.removeEntity(this.riding);
                mc.player.setPosition(mc.player.getPosition().getX(), mc.player.getPosition().getY() -1, mc.player.getPosition().getZ());
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (this.mode.getInt() == 1) {
            if (this.riding != null) {
                Minecraft.getMinecraft().player.connection.sendPacket(new CPacketUseEntity(this.riding, EnumHand.MAIN_HAND));
            }
        }
        if (this.mode.getInt() == 2) {
            Minecraft.getMinecraft().player.respawnPlayer();
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (this.mode.getInt() == 1) {

                if (this.riding != null) {
                    this.riding.posX = mc.player.posX;
                    this.riding.posY = mc.player.posY + (this.footsteps.getBoolean() ? 0.3f : 0.0f);
                    this.riding.posZ = mc.player.posZ;
                    this.riding.rotationYaw = Minecraft.getMinecraft().player.rotationYaw;
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, true));
                    mc.player.connection.sendPacket(new CPacketInput(mc.player.movementInput.moveForward, mc.player.movementInput.moveStrafe, false, false));
                    mc.player.connection.sendPacket(new CPacketVehicleMove(this.riding));
                }
            }
        }
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (this.mode.getInt() == 2) {
                if (mc.currentScreen instanceof GuiGameOver) {
                    mc.displayGuiScreen(null);
                }
                mc.player.setHealth(20.0f);
                mc.player.getFoodStats().setFoodLevel(20);
                mc.player.isDead = false;
            }
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.mode.getInt() == 0) {
                if (event.getPacket() instanceof CPacketConfirmTeleport) {
                    event.setCanceled(true);
                }
            }
            if (this.mode.getInt() == 1) {
                if (event.getPacket() instanceof CPacketUseEntity) {

                    final Minecraft mc = Minecraft.getMinecraft();

                    final CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();
                    if (this.riding != null) {
                        final Entity entity = packet.getEntityFromWorld(mc.world);
                        if (entity != null) {
                            this.riding.posX = entity.posX;
                            this.riding.posY = entity.posY;
                            this.riding.posZ = entity.posZ;
                            this.riding.rotationYaw = mc.player.rotationYaw;
                            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, true));
                            mc.player.connection.sendPacket(new CPacketInput(mc.player.movementInput.moveForward, mc.player.movementInput.moveStrafe, false, false));
                            mc.player.connection.sendPacket(new CPacketVehicleMove(this.riding));
                        }
                    }
                }

                if (event.getPacket() instanceof CPacketPlayer.Position || event.getPacket() instanceof CPacketPlayer.PositionRotation) {
                    event.setCanceled(true);
                }
            }
            if (this.mode.getInt() == 3) {
                if (event.getPacket() instanceof CPacketPlayer) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Listener
    public void displayGui(EventDisplayGui event) {
        if (event.getScreen() != null && event.getScreen() instanceof GuiGameOver && this.mode.getInt() == 2) {
            event.setCanceled(true);
        }
    }

}
