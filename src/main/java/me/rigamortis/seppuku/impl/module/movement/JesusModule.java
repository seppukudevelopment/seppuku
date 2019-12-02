package me.rigamortis.seppuku.impl.module.movement;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.event.player.EventUpdateWalkingPlayer;
import me.rigamortis.seppuku.api.event.world.EventLiquidCollisionBB;
import me.rigamortis.seppuku.api.module.Module;
import me.rigamortis.seppuku.api.value.Value;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/16/2019 @ 7:47 AM.
 */
public final class JesusModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]{"Mode", "M"}, "The current Jesus/WaterWalk mode to use.", Mode.NCP);

    private enum Mode {
        VANILLA, NCP, BOUNCE
    }

    public final Value<Float> offset = new Value<Float>("Offset", new String[]{"Off", "O"}, "Amount to offset the player into the water's bounding box.", 0.05f, 0.0f, 0.9f, 0.01f);

    public JesusModule() {
        super("Jesus", new String[]{"LiquidWalk", "WaterWalk"}, "Allows you to walk on water", "NONE", -1, ModuleType.MOVEMENT);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    @Listener
    public void getLiquidCollisionBB(EventLiquidCollisionBB event) {
        if(Minecraft.getMinecraft().world != null && Minecraft.getMinecraft().player != null) {
            if (this.checkCollide() && !(Minecraft.getMinecraft().player.motionY >= 0.1f) && event.getBlockPos().getY() < Minecraft.getMinecraft().player.posY - this.offset.getValue()) {
                if (Minecraft.getMinecraft().player.getRidingEntity() != null) {
                    event.setBoundingBox(new AxisAlignedBB(0, 0, 0, 1, 1 - this.offset.getValue(), 1));
                } else {
                    if (this.mode.getValue() == Mode.BOUNCE) {
                        event.setBoundingBox(new AxisAlignedBB(0, 0, 0, 1, 0.9f, 1));
                    } else {
                        event.setBoundingBox(Block.FULL_BLOCK_AABB);
                    }
                }
                event.setCanceled(true);
            }
        }
    }

    @Listener
    public void updateWalkingPlayer(EventUpdateWalkingPlayer event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (!Minecraft.getMinecraft().player.isSneaking() && !Minecraft.getMinecraft().player.noClip && !Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown() && isInLiquid()) {
                Minecraft.getMinecraft().player.motionY = 0.1f;
            }
        }
    }

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketPlayer) {
                if (this.mode.getValue() != Mode.VANILLA && Minecraft.getMinecraft().player.getRidingEntity() == null && !Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown()) {
                    final CPacketPlayer packet = (CPacketPlayer) event.getPacket();

                    if (!isInLiquid() && isOnLiquid(this.offset.getValue()) && checkCollide() && Minecraft.getMinecraft().player.ticksExisted % 3 == 0) {
                        packet.y -= this.offset.getValue();
                    }
                }
            }
        }
    }

    private boolean checkCollide() {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player.isSneaking()) {
            return false;
        }

        if (mc.player.getRidingEntity() != null) {
            if (mc.player.getRidingEntity().fallDistance >= 3.0f) {
                return false;
            }
        }

        if (mc.player.fallDistance >= 3.0f) {
            return false;
        }

        return true;
    }

    public static boolean isInLiquid() {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player.fallDistance >= 3.0f) {
            return false;
        }

        if (mc.player != null) {
            boolean inLiquid = false;
            final AxisAlignedBB bb = mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().getEntityBoundingBox() : mc.player.getEntityBoundingBox();
            int y = (int) bb.minY;
            for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX) + 1; x++) {
                for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ) + 1; z++) {
                    final Block block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (!(block instanceof BlockAir)) {
                        if (!(block instanceof BlockLiquid)) {
                            return false;
                        }
                        inLiquid = true;
                    }
                }
            }
            return inLiquid;
        }
        return false;
    }

    public static boolean isOnLiquid(double offset) {
        final Minecraft mc = Minecraft.getMinecraft();

        if (mc.player.fallDistance >= 3.0f) {
            return false;
        }

        if (mc.player != null) {
            final AxisAlignedBB bb = mc.player.getRidingEntity() != null ? mc.player.getRidingEntity().getEntityBoundingBox().contract(0.0d, 0.0d, 0.0d).offset(0.0d, -offset, 0.0d) : mc.player.getEntityBoundingBox().contract(0.0d, 0.0d, 0.0d).offset(0.0d, -offset, 0.0d);
            boolean onLiquid = false;
            int y = (int) bb.minY;
            for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX + 1.0D); x++) {
                for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ + 1.0D); z++) {
                    final Block block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block != Blocks.AIR) {
                        if (!(block instanceof BlockLiquid)) {
                            return false;
                        }
                        onLiquid = true;
                    }
                }
            }
            return onLiquid;
        }

        return false;
    }

}