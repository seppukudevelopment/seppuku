package me.rigamortis.seppuku.impl.module.world;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.gui.EventRenderHelmet;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.event.player.*;
import me.rigamortis.seppuku.api.event.render.EventRenderOverlay;
import me.rigamortis.seppuku.api.event.world.EventAddCollisionBox;
import me.rigamortis.seppuku.api.event.world.EventSetOpaqueCube;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/26/2019 @ 11:56 AM.
 */
public final class PhaseModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The phase mode to use", Mode.SAND);

    private enum Mode {
        SAND, PACKET, SKIP, NOCLIP
    }

    public final Value<Boolean> floor = new Value<Boolean>("Floor", new String[]{"Fl"}, "Prevents falling out of the world if enabled", true);

    public PhaseModule() {
        super("Phase", new String[]{"NoClip"}, "Allows you to glitch through blocks", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (Minecraft.getMinecraft().player != null) {
            Minecraft.getMinecraft().player.noClip = false;
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void setOpaqueCube(EventSetOpaqueCube event) {
        event.setCanceled(true);
    }

    @Listener
    public void renderOverlay(EventRenderOverlay event) {
        event.setCanceled(true);
    }

    @Listener
    public void renderHelmet(EventRenderHelmet event) {
        event.setCanceled(true);
    }

    @Listener
    public void pushOutOfBlocks(EventPushOutOfBlocks event) {
        event.setCanceled(true);
    }

    @Listener
    public void pushedByWater(EventPushedByWater event) {
        event.setCanceled(true);
    }

    @Listener
    public void applyCollision(EventApplyCollision event) {
        event.setCanceled(true);
    }

    /**
     * Replace this somehow, it's deprecated with how we handle block patches now
     *
     * @param event
     */
    @Listener
    public void collideWithBlock(EventAddCollisionBox event) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player != null) {

            final boolean floor = !this.floor.getValue() || event.getPos().getY() >= 1;

            if (this.mode.getValue() == Mode.SAND) {
                if (mc.player.getRidingEntity() != null && event.getEntity() == mc.player.getRidingEntity()) {
                    if (mc.gameSettings.keyBindSprint.isKeyDown() && floor) {
                        event.setCanceled(true);
                    } else {
                        if (mc.gameSettings.keyBindJump.isKeyDown() && event.getPos().getY() >= mc.player.getRidingEntity().posY) {
                            event.setCanceled(true);
                        }
                        if (event.getPos().getY() >= mc.player.getRidingEntity().posY) {
                            event.setCanceled(true);
                        }
                    }
                } else if (event.getEntity() == mc.player) {
                    if (mc.gameSettings.keyBindSneak.isKeyDown() && floor) {
                        event.setCanceled(true);
                    } else {
                        if (mc.gameSettings.keyBindJump.isKeyDown() && event.getPos().getY() >= mc.player.posY) {
                            event.setCanceled(true);
                        }
                        if (event.getPos().getY() >= mc.player.posY) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }

        if (this.mode.getValue() == Mode.NOCLIP) {
            if (event.getEntity() == mc.player || mc.player.getRidingEntity() != null && event.getEntity() == mc.player.getRidingEntity()) {
                event.setCanceled(true);
            }
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (this.mode.getValue() == Mode.NOCLIP) {
                if (event.getPacket() instanceof CPacketPlayer && !(event.getPacket() instanceof CPacketPlayer.Position)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (this.mode.getValue() == Mode.NOCLIP) {
                mc.player.setVelocity(0, 0, 0);
                if (mc.gameSettings.keyBindForward.isKeyDown() || mc.gameSettings.keyBindBack.isKeyDown() || mc.gameSettings.keyBindLeft.isKeyDown() || mc.gameSettings.keyBindRight.isKeyDown()) {
                    final double[] speed = MathUtil.directionSpeed(0.06f);
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + speed[0], mc.player.posY, mc.player.posZ + speed[1], mc.player.onGround));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, 0, mc.player.posZ, mc.player.onGround));
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.06f, mc.player.posZ, mc.player.onGround));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, 0, mc.player.posZ, mc.player.onGround));
                }

                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.06f, mc.player.posZ, mc.player.onGround));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, 0, mc.player.posZ, mc.player.onGround));
                }
            }
        }
    }

    @Listener
    public void onUpdate(EventPlayerUpdate event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();

            if (this.mode.getValue() == Mode.SAND) {
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    if (mc.player.getRidingEntity() != null && mc.player.getRidingEntity() instanceof EntityBoat) {
                        final EntityBoat boat = (EntityBoat) mc.player.getRidingEntity();
                        if (boat.onGround) {
                            boat.motionY = 0.42f;
                        }
                    }
                }
            }

            if (this.mode.getValue() == Mode.PACKET) {
                final Vec3d dir = MathUtil.direction(mc.player.rotationYaw);
                if (mc.player.onGround && mc.player.collidedHorizontally) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir.x * 0.00001f, mc.player.posY, mc.player.posZ + dir.z * 0.0001f, mc.player.onGround));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir.x * 2.0f, mc.player.posY, mc.player.posZ + dir.z * 2.0f, mc.player.onGround));
                }
            }

            if (this.mode.getValue() == Mode.SKIP) {
                final Vec3d dir = MathUtil.direction(mc.player.rotationYaw);
                if (mc.player.onGround && mc.player.collidedHorizontally) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.onGround));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir.x * 0.001f, mc.player.posY + 0.1f, mc.player.posZ + dir.z * 0.001f, mc.player.onGround));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir.x * 0.03f, 0, mc.player.posZ + dir.z * 0.03f, mc.player.onGround));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + dir.x * 0.06f, mc.player.posY, mc.player.posZ + dir.z * 0.06f, mc.player.onGround));
                }
            }
        }
    }

}
