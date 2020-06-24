package me.rigamortis.seppuku.impl.module.player;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.gui.EventRenderHelmet;
import me.rigamortis.seppuku.api.event.network.EventReceivePacket;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.event.player.*;
import me.rigamortis.seppuku.api.event.render.EventRenderOverlay;
import me.rigamortis.seppuku.api.event.world.EventAddCollisionBox;
import me.rigamortis.seppuku.api.event.world.EventLiquidCollisionBB;
import me.rigamortis.seppuku.api.event.world.EventSetOpaqueCube;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.util.MathUtil;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/24/2019 @ 7:45 PM.
 */
public final class FreeCamModule extends Module {

    private Entity riding;
    private EntityOtherPlayerMP entity;
    private Vec3d position;
    private float yaw;
    private float pitch;

    public final Value<Float> speed = new Value<Float>("Speed", new String[]{"Spd"}, "Speed of freecam flight, higher number equals quicker motion.", 1.0f, 0.0f, 10.0f, 0.1f);

    public final Value<Boolean> view = new Value<Boolean>("3D", new String[]{"View"}, "The old Nodus client style free-cam, kind of like an elytra. (Hold forward key & move the mouse to turn)", false);

    public final Value<Boolean> packet = new Value<Boolean>("Packet", new String[]{"Pack"}, "Disables any player position or rotation packets from being sent during free-cam if enabled.", true);

    public FreeCamModule() {
        super("FreeCam", new String[]{"FreeCamera"}, "Out of body experience", "NONE", -1, ModuleType.PLAYER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != null) {
            if (mc.player.getRidingEntity() != null) {
                this.riding = mc.player.getRidingEntity();
                mc.player.dismountRidingEntity();
            }
            this.entity = new EntityOtherPlayerMP(mc.world, mc.session.getProfile());
            this.entity.copyLocationAndAnglesFrom(mc.player);
            this.entity.rotationYaw = mc.player.rotationYaw;
            this.entity.rotationYawHead = mc.player.rotationYawHead;
            this.entity.inventory.copyInventory(mc.player.inventory);
            mc.world.addEntityToWorld(69420, this.entity);
            this.position = mc.player.getPositionVector();
            this.yaw = mc.player.rotationYaw;
            this.pitch = mc.player.rotationPitch;
            mc.player.noClip = true;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != null) {
            if (this.riding != null) {
                mc.player.startRiding(this.riding, true);
            }
            if (this.entity != null) {
                mc.world.removeEntity(this.entity);
            }
            if (this.position != null) {
                mc.player.setPosition(this.position.x, this.position.y, this.position.z);
            }
            mc.player.rotationYaw = this.yaw;
            mc.player.rotationPitch = this.pitch;
            mc.player.noClip = false;
            mc.player.motionX = 0;
            mc.player.motionY = 0;
            mc.player.motionZ = 0;
        }
    }

    @Listener
    public void onMove(EventMove event) {
        Minecraft.getMinecraft().player.noClip = true;
    }

    @Listener
    public void onWalkingUpdate(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            final Minecraft mc = Minecraft.getMinecraft();
            mc.player.setVelocity(0, 0, 0);
            mc.player.renderArmPitch = 5000;
            mc.player.jumpMovementFactor = this.speed.getValue();

            final double[] dir = MathUtil.directionSpeed(this.speed.getValue());

            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                mc.player.motionX = dir[0];
                mc.player.motionZ = dir[1];
            } else {
                mc.player.motionX = 0;
                mc.player.motionZ = 0;
            }

            mc.player.setSprinting(false);

            if (this.view.getValue()) {
                if (!mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.motionY = (this.speed.getValue() * (-MathUtil.degToRad(mc.player.rotationPitch))) * mc.player.movementInput.moveForward;
                }
            }

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.motionY += this.speed.getValue();
            }

            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.motionY -= this.speed.getValue();
            }
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (Minecraft.getMinecraft().world != null) {
                if (this.packet.getValue()) {
                    if (event.getPacket() instanceof CPacketPlayer) {
                        event.setCanceled(true);
                    }
                } else {
                    if (!(event.getPacket() instanceof CPacketUseEntity) && !(event.getPacket() instanceof CPacketPlayerTryUseItem) && !(event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) && !(event.getPacket() instanceof CPacketPlayer) && !(event.getPacket() instanceof CPacketVehicleMove) && !(event.getPacket() instanceof CPacketChatMessage) && !(event.getPacket() instanceof CPacketKeepAlive)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @Listener
    public void receivePacket(EventReceivePacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if(event.getPacket() instanceof SPacketSetPassengers) {
                final SPacketSetPassengers packet = (SPacketSetPassengers) event.getPacket();
                final Entity riding = Minecraft.getMinecraft().world.getEntityByID(packet.getEntityId());

                if(riding != null && riding == this.riding) {
                    this.riding = null;
                }
            }
            if (event.getPacket() instanceof SPacketPlayerPosLook) {
                final SPacketPlayerPosLook packet = (SPacketPlayerPosLook) event.getPacket();
                if (this.packet.getValue()) {
                    if (this.entity != null) {
                        this.entity.setPositionAndRotation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
                    }
                    this.position = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                    Minecraft.getMinecraft().player.connection.sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
                    event.setCanceled(true);
                } else {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Listener
    public void collideWithBlock(EventAddCollisionBox event) {
        if (event.getEntity() == Minecraft.getMinecraft().player) {
            event.setCanceled(true);
        }
    }

    @Listener
    public void getLiquidCollisionBB(EventLiquidCollisionBB event) {
        event.setBoundingBox(Block.NULL_AABB);
        event.setCanceled(true);
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

}
