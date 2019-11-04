package me.rigamortis.seppuku.impl.module.player;

import me.rigamortis.seppuku.api.event.EventStageable;
import me.rigamortis.seppuku.api.event.network.EventSendPacket;
import me.rigamortis.seppuku.api.module.Module;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import team.stiff.pomelo.impl.annotated.handler.annotation.Listener;

/**
 * Author Seth
 * 4/22/2019 @ 1:02 PM.
 */
public final class InteractModule extends Module {

    public InteractModule() {
        super("Interact", new String[]{"GhostHand"}, "Allows you to access entities and blocks through walls", "NONE", -1, ModuleType.PLAYER);
    }

    private boolean clicked;

    @Listener
    public void sendPacket(EventSendPacket event) {
        if (event.getStage() == EventStageable.EventStage.PRE) {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                if (this.clicked) {
                    this.clicked = false;
                    return;
                }
                final CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock) event.getPacket();
                final Minecraft mc = Minecraft.getMinecraft();
                if (mc.currentScreen == null) {
                    final Block block = mc.world.getBlockState(packet.getPos()).getBlock();
                    if (block.onBlockActivated(mc.world, packet.getPos(), mc.world.getBlockState(packet.getPos()), mc.player, packet.getHand(), packet.getDirection(), packet.getFacingX(), packet.getFacingY(), packet.getFacingZ())) {
                        return;
                    }

                    final BlockPos usable = findUsableBlock(packet.getHand(), packet.getDirection(), packet.getFacingX(), packet.getFacingY(), packet.getFacingZ());
                    if (usable != null) {
                        mc.player.swingArm(packet.getHand());
                        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(usable, packet.getDirection(), packet.getHand(), packet.getFacingX(), packet.getFacingY(), packet.getFacingZ()));
                        this.clicked = true;
                    }else{
                        final Entity usableEntity = findUsableEntity(packet.getHand());
                        if(usableEntity != null) {
                            mc.player.connection.sendPacket(new CPacketUseEntity(usableEntity, packet.getHand()));
                            this.clicked = true;
                        }
                    }
                }
            }
        }
    }

    private Entity findUsableEntity(EnumHand hand) {
        final Minecraft mc = Minecraft.getMinecraft();

        Entity entity = null;

        for (int i = 0; i <= mc.playerController.getBlockReachDistance(); i++) {
            final AxisAlignedBB bb = this.traceToBlock(i, mc.getRenderPartialTicks());
            float maxDist = mc.playerController.getBlockReachDistance();
            for (Entity e : mc.world.getEntitiesWithinAABBExcludingEntity(mc.player, bb)) {
                float currentDist = mc.player.getDistance(e);
                if (currentDist <= maxDist) {
                    entity = e;
                    maxDist = currentDist;
                }
            }
        }
        return entity;
    }

    private BlockPos findUsableBlock(EnumHand hand, EnumFacing dir, float x, float y, float z) {
        final Minecraft mc = Minecraft.getMinecraft();

        for (int i = 0; i <= mc.playerController.getBlockReachDistance(); i++) {
            final AxisAlignedBB bb = this.traceToBlock(i, mc.getRenderPartialTicks());
            final BlockPos pos = new BlockPos(bb.minX, bb.minY, bb.minZ);
            final Block block = mc.world.getBlockState(pos).getBlock();
            if (block.onBlockActivated(mc.world, pos, mc.world.getBlockState(pos), mc.player, hand, dir, x, y, z)) {
                return new BlockPos(pos);
            }
        }

        return null;
    }

    private AxisAlignedBB traceToBlock(double dist, float partialTicks) {
        final Vec3d pos = Minecraft.getMinecraft().player.getPositionEyes(partialTicks);
        final Vec3d angles = Minecraft.getMinecraft().player.getLook(partialTicks);
        final Vec3d end = pos.add(angles.x * dist, angles.y * dist, angles.z * dist);
        return new AxisAlignedBB(end.x, end.y, end.z, end.x + 1, end.y + 1, end.z + 1);
    }
}
