package me.rigamortis.seppuku.api.event.world;

import me.rigamortis.seppuku.api.event.EventCancellable;
import net.minecraft.block.Block;
import net.minecraft.util.BlockRenderLayer;

/**
 * Author Seth
 * 4/9/2019 @ 1:54 PM.
 */
public class EventGetBlockLayer extends EventCancellable {

    private Block block;
    private BlockRenderLayer layer;

    public EventGetBlockLayer(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public BlockRenderLayer getLayer() {
        return layer;
    }

    public void setLayer(BlockRenderLayer layer) {
        this.layer = layer;
    }
}

