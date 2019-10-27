package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.event.player.EventPlayerUpdate;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.BooleanValue;
import me.rigamortis.seppuku.api.value.NumberValue;
import me.rigamortis.seppuku.api.value.OptionalValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Author Seth
 * 4/26/2019 @ 1:56 PM.
 */
public final class FlightModule extends Module {

    public final OptionalValue mode = new OptionalValue("Mode", new String[]{"Mode", "M"}, 0, new String[]{"Vanilla", "Packet"});

    public final NumberValue speed = new NumberValue("Speed", new String[]{"Spd"}, 0.06f, Float.class, 0.0f, 10.0f, 0.01f);

    public final BooleanValue noKick = new BooleanValue("NoKick", new String[]{"AntiKick", "Kick"}, true);

    private int teleportId;
    private List<CPacketPlayer> packets = new ArrayList<>();

    public FlightModule() {
        super("Flight", new String[]{"Fly"}, "Allows you to fly", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (this.mode.getInt() == 1) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (mc.world != null) {
                this.teleportId = 0;
                this.packets.clear();
                final CPacketPlayer bounds = new CPacketPlayer.Position(mc.player.posX, 0, mc.player.posZ, mc.player.onGround);
                this.packets.add(bounds);
                mc.player.connection.sendPacket(bounds);
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public String getMetaData() {
        return this.mode.getSelectedOption();
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
        }
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (this.mode.getInt() == 0) {
                mc.player.setVelocity(0, 0, 0);

                mc.player.jumpMovementFactor = speed.getFloat();

                if (this.noKick.getBoolean()) {
                    if (mc.player.ticksExisted % 4 == 0) {
                        mc.player.motionY = -0.04f;
                    }
                }

                final double[] dir = MathUtil.directionSpeed(speed.getFloat());

                if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                    mc.player.motionX = dir[0];
                    mc.player.motionZ = dir[1];
                } else {
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                }

                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    if (this.noKick.getBoolean()) {
                        mc.player.motionY = mc.player.ticksExisted % 20 == 0 ? -0.04f : speed.getFloat();
                    } else {
                        mc.player.motionY += speed.getFloat();
                    }
                }

                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.motionY -= speed.getFloat();
                }
            }

            if (this.mode.getInt() == 1) {
                if (this.teleportId <= 0) {
                    final CPacketPlayer bounds = new CPacketPlayer.Position(Minecraft.getMinecraft().player.posX, 0, Minecraft.getMinecraft().player.posZ, Minecraft.getMinecraft().player.onGround);
                    this.packets.add(bounds);
                    Minecraft.getMinecraft().player.connection.sendPacket(bounds);
                    return;
                }

                mc.player.setVelocity(0, 0, 0);

                if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625d, 0, -0.0625d)).isEmpty()) {
                    double ySpeed = 0;

                    if (mc.gameSettings.keyBindJump.isKeyDown()) {

                        if (this.noKick.getBoolean()) {
                            ySpeed = mc.player.ticksExisted % 20 == 0 ? -0.04f : 0.062f;
                        } else {
                            ySpeed = 0.062f;
                        }
                    } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                        ySpeed = -0.062d;
                    } else {
                        ySpeed = mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625d, -0.0625d, -0.0625d)).isEmpty() ? (mc.player.ticksExisted % 4 == 0) ? (this.noKick.getBoolean() ? -0.04f : 0.0f) : 0.0f : 0.0f;
                    }

                    final double[] directionalSpeed = MathUtil.directionSpeed(this.speed.getFloat());

                    if (mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindSneak.isKeyDown() || mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown()) {
                        if (directionalSpeed[0] != 0.0d || ySpeed != 0.0d || directionalSpeed[1] != 0.0d) {
                            if (mc.player.movementInput.jump && (mc.player.moveStrafing != 0 || mc.player.moveForward != 0)) {
                                mc.player.setVelocity(0, 0, 0);
                                move(0, 0, 0);
                                for (int i = 0; i <= 3; i++) {
                                    mc.player.setVelocity(0, ySpeed * i, 0);
                                    move(0, ySpeed * i, 0);
                                }
                            } else {
                                if (mc.player.movementInput.jump) {
                                    mc.player.setVelocity(0, 0, 0);
                                    move(0, 0, 0);
                                    for (int i = 0; i <= 3; i++) {
                                        mc.player.setVelocity(0, ySpeed * i, 0);
                                        move(0, ySpeed * i, 0);
                                    }
                                } else {
                                    for (int i = 0; i <= 2; i++) {
                                        mc.player.setVelocity(directionalSpeed[0] * i, ySpeed * i, directionalSpeed[1] * i);
                                        move(directionalSpeed[0] * i, ySpeed * i, directionalSpeed[1] * i);
                                    }
                                }
                            }
                        }
                    } else {
                        if (this.noKick.getBoolean()) {
                            if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625d, -0.0625d, -0.0625d)).isEmpty()) {
                                mc.player.setVelocity(0, (mc.player.ticksExisted % 2 == 0) ? 0.04f : -0.04f, 0);
                                move(0, (mc.player.ticksExisted % 2 == 0) ? 0.04f : -0.04f, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    private void move(double x, double y, double z) {
        final Minecraft mc = Minecraft.getMinecraft();
        final CPacketPlayer pos = new CPacketPlayer.Position(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z, mc.player.onGround);
        this.packets.add(pos);
        mc.player.connection.sendPacket(pos);

        final CPacketPlayer bounds = new CPacketPlayer.Position(mc.player.posX + x, 0, mc.player.posZ + z, mc.player.onGround);
        this.packets.add(bounds);
        mc.player.connection.sendPacket(bounds);

        this.teleportId++;
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.teleportId - 1));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.teleportId));
        mc.player.connection.sendPacket(new CPacketConfirmTeleport(this.teleportId + 1));
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.mode.getInt() == 1) {
                if (event.getPacket() instanceof CPacketPlayer && !(event.getPacket() instanceof CPacketPlayer.Position)) {
                    event.setCanceled(true);
                }
                if (event.getPacket() instanceof CPacketPlayer) {
                    final CPacketPlayer packet = (CPacketPlayer) event.getPacket();
                    if (packets.contains(packet)) {
                        packets.remove(packet);
                        return;
                    }
                    event.setCanceled(true);
                }
            }
        }
    }

    @Listener
    public void recievePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.mode.getInt() == 1) {
                if (event.getPacket() instanceof SPacketPlayerPosLook) {
                    final SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
                    if (Minecraft.getMinecraft().player.isEntityAlive() && Minecraft.getMinecraft().world.isBlockLoaded(new BlockPos(Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ)) && !(Minecraft.getMinecraft().currentScreen instanceof GuiDownloadTerrain)) {
                        if (this.teleportId <= 0) {
                            this.teleportId = packet.getTeleportId();
                        } else {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

}
