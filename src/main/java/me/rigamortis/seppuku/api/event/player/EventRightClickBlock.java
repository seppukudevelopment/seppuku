package me.rigamortis.seppuku.api.event.player;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Author Seth
 * 4/7/2019 @ 3:18 PM.
 */
public class EventRightClickBlock extends EventCancellable {

    private BlockPos pos;
    private EnumFacing facing;
    private Vec3d vec;
    private EnumHand hand;

    public EventRightClickBlock(BlockPos pos, EnumFacing facing, Vec3d vec, EnumHand hand) {
        this.pos = pos;
        this.facing = facing;
        this.vec = vec;
        this.hand = hand;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public void setFacing(EnumFacing facing) {
        this.facing = facing;
    }

    public Vec3d getVec() {
        return vec;
    }

    public void setVec(Vec3d vec) {
        this.vec = vec;
    }

    public EnumHand getHand() {
        return hand;
    }

    public void setHand(EnumHand hand) {
        this.hand = hand;
    }
}
