package me.rigamortis.seppuku.api.task.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * @author Daniel E
 */
public final class BlockPlacementRequest {
    private final BlockPos structurePosition;
    private final EnumFacing placeDirection;

    public BlockPlacementRequest(final BlockPos structurePosition,
                                 final EnumFacing placeDirection) {
        this.structurePosition = structurePosition;
        this.placeDirection = placeDirection;
    }

    public BlockPos getStructurePosition() {
        return structurePosition;
    }

    public EnumFacing getPlaceDirection() {
        return placeDirection;
    }

    public void handlePlaceRequest(final Minecraft minecraft) {
        final BlockPos structurePosition = this.getStructurePosition();
        final IBlockState structureBlockState = minecraft.world.getBlockState(structurePosition);
        final boolean blockActivated = structureBlockState.getBlock().onBlockActivated(minecraft.world,
                structurePosition, structureBlockState, minecraft.player, EnumHand.MAIN_HAND,
                EnumFacing.UP, 0.0f, 0.0f, 0.0f);
        if (blockActivated)
            minecraft.player.connection.sendPacket(new CPacketEntityAction(minecraft.player,
                    CPacketEntityAction.Action.START_SNEAKING));

        if (minecraft.playerController.processRightClickBlock(minecraft.player, minecraft.world,
                structurePosition, this.getPlaceDirection(),
                Vec3d.ZERO, EnumHand.MAIN_HAND) != EnumActionResult.FAIL)
            minecraft.player.swingArm(EnumHand.MAIN_HAND);

        if (blockActivated)
            minecraft.player.connection.sendPacket(new CPacketEntityAction(minecraft.player,
                    CPacketEntityAction.Action.STOP_SNEAKING));
    }
}
