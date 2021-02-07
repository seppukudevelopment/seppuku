package me.rigamortis.seppuku.api.event.render;

import net.minecraft.util.math.BlockPos;

/**
 * @author noil
 */
public class EventRenderBlock {

    private final BlockPos pos;

    public EventRenderBlock(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }
}
