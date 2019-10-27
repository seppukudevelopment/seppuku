package me.rigamortis.seppuku.api.event.player;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Author Seth
 * 4/6/2019 @ 6:31 PM.
 */
public class EventPlayerDamageBlock extends EventCancellable {

    private BlockPos pos;
    private EnumFacing face;

    public EventPlayerDamageBlock(BlockPos pos, EnumFacing face) {
        this.pos = pos;
        this.face = face;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public EnumFacing getFace() {
        return face;
    }

    public void setFace(EnumFacing face) {
        this.face = face;
    }
}
