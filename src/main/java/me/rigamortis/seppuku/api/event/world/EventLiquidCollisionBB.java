package me.rigamortis.seppuku.api.event.world;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * Author Seth
 * 4/16/2019 @ 7:37 AM.
 */
public class EventLiquidCollisionBB extends EventCancellable {

    private AxisAlignedBB boundingBox;
    private BlockPos blockPos;

    public EventLiquidCollisionBB() {

    }

    public EventLiquidCollisionBB(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }
}
