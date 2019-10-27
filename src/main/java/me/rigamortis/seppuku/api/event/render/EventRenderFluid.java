package me.rigamortis.seppuku.api.event.render;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Author Seth
 * 4/11/2019 @ 3:21 AM.
 */
public class EventRenderFluid extends EventCancellable {

    private IBlockAccess blockAccess;
    private IBlockState blockState;
    private BlockPos blockPos;
    private BufferBuilder bufferBuilder;
    private boolean renderable;

    public EventRenderFluid(IBlockAccess blockAccess, IBlockState blockState, BlockPos blockPos, BufferBuilder bufferBuilder) {
        this.blockAccess = blockAccess;
        this.blockState = blockState;
        this.blockPos = blockPos;
        this.bufferBuilder = bufferBuilder;
    }

    public IBlockAccess getBlockAccess() {
        return blockAccess;
    }

    public void setBlockAccess(IBlockAccess blockAccess) {
        this.blockAccess = blockAccess;
    }

    public IBlockState getBlockState() {
        return blockState;
    }

    public void setBlockState(IBlockState blockState) {
        this.blockState = blockState;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public BufferBuilder getBufferBuilder() {
        return bufferBuilder;
    }

    public void setBufferBuilder(BufferBuilder bufferBuilder) {
        this.bufferBuilder = bufferBuilder;
    }

    public boolean isRenderable() {
        return renderable;
    }

    public void setRenderable(boolean renderable) {
        this.renderable = renderable;
    }
}
